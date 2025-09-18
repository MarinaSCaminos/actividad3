package ar.edu.unlu.bd2.controller;

import ar.edu.unlu.bd2.modelo.DetalleFactura;
import ar.edu.unlu.bd2.modelo.DetalleFacturaId;
import ar.edu.unlu.bd2.modelo.Factura;
import ar.edu.unlu.bd2.modelo.Producto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Optional;

public class DetalleFacturaController {

    // ======= SessionFactory único por clase (mismo criterio que los otros controllers) =======
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Error creando SessionFactory: " + ex.getMessage());
        }
    }

    // ======= CRUD / Operaciones principales =======

    /** Crea un detalle para (idFactura, idProducto) con 'cantidad'. Triggers de BD calculan precio y subtotal. */
    public DetalleFactura crear(Integer idFactura, Integer idProducto, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");

        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            Factura factura = session.get(Factura.class, idFactura);
            if (factura == null) {
                tx.rollback();
                throw new IllegalArgumentException("Factura inexistente id=" + idFactura);
            }

            Producto producto = session.get(Producto.class, idProducto);
            if (producto == null) {
                tx.rollback();
                throw new IllegalArgumentException("Producto inexistente id=" + idProducto);
            }
            if (Boolean.FALSE.equals(producto.getActivo())) {
                tx.rollback();
                throw new IllegalStateException("Producto inactivo id=" + idProducto);
            }

            // Evitar duplicado del par (factura, producto)
            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            if (session.get(DetalleFactura.class, pk) != null) {
                tx.rollback();
                throw new IllegalStateException("Ya existe un detalle para ese producto en la factura");
            }

            DetalleFactura det = new DetalleFactura(factura, producto, cantidad);
            session.persist(det);

            // <-- IMPORTANTE: traer campos calculados por triggers (precio_unitario, subtotal)
            session.flush();
            session.refresh(det);

            tx.commit();
            return det;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo crear el detalle (factura=" + idFactura + ", producto=" + idProducto + "): " + e.getMessage(), e);
        }
    }

    /** Obtiene un detalle por clave compuesta. */
    public Optional<DetalleFactura> obtener(Integer idFactura, Integer idProducto) {
        try (Session session = SESSION_FACTORY.openSession()) {
            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            DetalleFactura d = session.get(DetalleFactura.class, pk);
            return Optional.ofNullable(d);
        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo obtener el detalle (factura=" + idFactura + ", producto=" + idProducto + "): " + e.getMessage(), e);
        }
    }

    /** Lista todos los detalles de una factura (con producto y factura inicializados). */
    public List<DetalleFactura> listarPorFactura(Integer idFactura) {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery(
                            "select d from DetalleFactura d " +
                                    "join fetch d.producto " +
                                    "join fetch d.factura " +
                                    "where d.factura.idFactura = :fid " +
                                    "order by d.producto.idProducto",
                            DetalleFactura.class)
                    .setParameter("fid", idFactura)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar los detalles de la factura " + idFactura + ": " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza la cantidad del detalle. Implementado como DELETE + INSERT para
     * respetar el flujo de triggers del profe (recalcula stock, subtotal y total).
     */
    public DetalleFactura actualizarCantidad(Integer idFactura, Integer idProducto, int nuevaCantidad) {
        if (nuevaCantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");

        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            DetalleFactura existente = session.get(DetalleFactura.class, pk);
            if (existente == null) {
                tx.rollback();
                throw new IllegalArgumentException("Detalle inexistente (factura=" + idFactura + ", producto=" + idProducto + ")");
            }

            Factura factura = existente.getFactura();
            Producto producto = existente.getProducto();

            session.remove(existente);   // triggers devuelven stock y recalculan total

            DetalleFactura nuevo = new DetalleFactura(factura, producto, nuevaCantidad);
            session.persist(nuevo);

            // refrescar para ver valores calculados por triggers
            session.flush();
            session.refresh(nuevo);

            tx.commit();
            return nuevo;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo actualizar la cantidad (factura=" + idFactura + ", producto=" + idProducto + "): " + e.getMessage(), e);
        }
    }

    /**
     * Reemplaza el producto de un detalle por otro (DELETE del viejo + INSERT del nuevo).
     * Evita duplicar si ya existe un detalle con el producto nuevo.
     */
    public DetalleFactura reemplazarProducto(Integer idFactura, Integer idProductoActual, Integer idProductoNuevo, int cantidadNueva) {
        if (cantidadNueva <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");

        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            DetalleFacturaId pkViejo = new DetalleFacturaId(idFactura, idProductoActual);
            DetalleFactura viejo = session.get(DetalleFactura.class, pkViejo);
            if (viejo == null) {
                tx.rollback();
                throw new IllegalArgumentException("No existe detalle a reemplazar (factura=" + idFactura + ", producto=" + idProductoActual + ")");
            }

            // Evitar duplicados con el nuevo producto
            DetalleFacturaId pkNuevo = new DetalleFacturaId(idFactura, idProductoNuevo);
            if (session.get(DetalleFactura.class, pkNuevo) != null) {
                tx.rollback();
                throw new IllegalStateException("La factura ya tiene un detalle para el producto nuevo " + idProductoNuevo);
            }

            Producto nuevoProd = session.get(Producto.class, idProductoNuevo);
            if (nuevoProd == null) {
                tx.rollback();
                throw new IllegalArgumentException("Producto nuevo inexistente id=" + idProductoNuevo);
            }
            if (Boolean.FALSE.equals(nuevoProd.getActivo())) {
                tx.rollback();
                throw new IllegalStateException("Producto nuevo inactivo id=" + idProductoNuevo);
            }

            Factura factura = viejo.getFactura();

            session.remove(viejo); // triggers devuelven stock y recalculan
            DetalleFactura nuevo = new DetalleFactura(factura, nuevoProd, cantidadNueva);
            session.persist(nuevo); // triggers descuentan y recalculan

            // refrescar para ver valores calculados
            session.flush();
            session.refresh(nuevo);

            tx.commit();
            return nuevo;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo reemplazar el producto del detalle (factura=" + idFactura + ", prodViejo=" + idProductoActual + ", prodNuevo=" + idProductoNuevo + "): " + e.getMessage(), e);
        }
    }

    /** Elimina un detalle (factura, producto). */
    public boolean eliminar(Integer idFactura, Integer idProducto) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            DetalleFactura det = session.get(DetalleFactura.class, pk);
            if (det == null) {
                tx.rollback();
                return false;
            }

            session.remove(det); // triggers devuelven stock y recalculan total
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo eliminar el detalle (factura=" + idFactura + ", producto=" + idProducto + "): " + e.getMessage(), e);
        }
    }

    public static void shutdown() {
        try {
            if (SESSION_FACTORY != null) SESSION_FACTORY.close();
        } catch (Exception ignore) {}
    }
}
