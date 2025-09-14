package ar.edu.unlu.bd2.controller;

import ar.edu.unlu.bd2.modelo.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Optional;

public class FacturaController {

    // ======= SessionFactory único por clase (simple, estilo del profe) =======
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Error creando SessionFactory: " + ex.getMessage());
        }
    }

    // ======= CRUD Factura =======

    /** Crea una factura vacía para un cliente existente. */
    public Factura crearParaCliente(Integer idCliente) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            Cliente cliente = session.get(Cliente.class, idCliente);
            if (cliente == null) {
                tx.rollback();
                throw new IllegalArgumentException("Cliente inexistente id=" + idCliente);
            }

            Factura f = new Factura(cliente);
            session.persist(f);

            tx.commit();
            return f;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo crear la factura para cliente " + idCliente + ": " + e.getMessage(), e);
        }
    }

    public Optional<Factura> obtenerPorId(Long idFactura) {
        try (Session session = SESSION_FACTORY.openSession()) {
            Factura f = session.get(Factura.class, idFactura);
            return Optional.ofNullable(f);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener la factura id=" + idFactura + ": " + e.getMessage(), e);
        }
    }

    public List<Factura> listarTodas() {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery("from Factura f order by f.idFactura", Factura.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar facturas: " + e.getMessage(), e);
        }
    }

    public List<Factura> listarPorCliente(Integer idCliente) {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery(
                            "from Factura f where f.cliente.idCliente = :cid order by f.idFactura",
                            Factura.class)
                    .setParameter("cid", idCliente)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar facturas del cliente " + idCliente + ": " + e.getMessage(), e);
        }
    }

    /** Elimina una factura (si hay detalles, dependerá de FK/orphanRemoval). */
    public boolean eliminar(Long idFactura) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Factura f = session.get(Factura.class, idFactura);
            if (f == null) {
                tx.rollback();
                return false;
            }
            session.remove(f);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo eliminar la factura id=" + idFactura + ": " + e.getMessage(), e);
        }
    }

    // ======= Operaciones con Detalles =======

    /** Agrega un detalle (producto+cantidad) a una factura existente. */
    public DetalleFactura agregarDetalle(Long idFactura, Long idProducto, int cantidad) {
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

            // Si ya existe el mismo (id_factura, id_producto) evitamos duplicar
            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            DetalleFactura existente = session.get(DetalleFactura.class, pk);
            if (existente != null) {
                tx.rollback();
                throw new IllegalStateException("La factura ya tiene un detalle para el producto " + idProducto);
            }

            DetalleFactura det = new DetalleFactura(factura, producto, cantidad);
            session.persist(det); // triggers de BD calcularán precio_unitario, subtotal y actualizarán stock

            tx.commit();
            return det;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo agregar detalle a factura=" + idFactura + " producto=" + idProducto + ": " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza la cantidad de un detalle.
     * Para imitar el enfoque del profe (SP que hace DELETE + INSERT para recalcular triggers),
     * implementamos como "reemplazo": borramos y volvemos a insertar con la nueva cantidad.
     */
    public DetalleFactura actualizarCantidad(Long idFactura, Long idProducto, int nuevaCantidad) {
        if (nuevaCantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser > 0");

        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            DetalleFacturaId pk = new DetalleFacturaId(idFactura, idProducto);
            DetalleFactura existente = session.get(DetalleFactura.class, pk);
            if (existente == null) {
                tx.rollback();
                throw new IllegalArgumentException("No existe detalle (factura=" + idFactura + ", producto=" + idProducto + ")");
            }

            // Guardamos referencias antes de borrar
            Factura factura = existente.getFactura();
            Producto producto = existente.getProducto();

            session.remove(existente); // triggers devuelven stock y recalculan total

            DetalleFactura nuevo = new DetalleFactura(factura, producto, nuevaCantidad);
            session.persist(nuevo);    // triggers descuentan y recalculan

            tx.commit();
            return nuevo;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo actualizar cantidad del detalle (factura=" + idFactura + ", producto=" + idProducto + "): " + e.getMessage(), e);
        }
    }

    /**
     * Reemplaza el producto del detalle (DELETE del detalle anterior + INSERT del nuevo).
     * Equivalente conceptual al SP del profe.
     */
    public DetalleFactura reemplazarProducto(Long idFactura, Long idProductoActual, Long idProductoNuevo, int cantidadNueva) {
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

            Factura factura = viejo.getFactura();
            Producto nuevoProd = session.get(Producto.class, idProductoNuevo);
            if (nuevoProd == null) {
                tx.rollback();
                throw new IllegalArgumentException("Producto nuevo inexistente id=" + idProductoNuevo);
            }
            if (Boolean.FALSE.equals(nuevoProd.getActivo())) {
                tx.rollback();
                throw new IllegalStateException("Producto nuevo inactivo id=" + idProductoNuevo);
            }

            // Si ya existe un detalle con el producto nuevo, evitamos duplicar
            DetalleFacturaId pkNuevo = new DetalleFacturaId(idFactura, idProductoNuevo);
            if (session.get(DetalleFactura.class, pkNuevo) != null) {
                tx.rollback();
                throw new IllegalStateException("La factura ya tiene un detalle para el producto nuevo " + idProductoNuevo);
            }

            session.remove(viejo); // triggers devuelven stock y recalculan
            DetalleFactura nuevo = new DetalleFactura(factura, nuevoProd, cantidadNueva);
            session.persist(nuevo); // triggers descuentan y recalculan

            tx.commit();
            return nuevo;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(
                    "No se pudo reemplazar detalle (factura=" + idFactura + ", prodViejo=" + idProductoActual + ", prodNuevo=" + idProductoNuevo + "): " + e.getMessage(), e);
        }
    }

    /** Elimina un detalle (factura, producto). */
    public boolean eliminarDetalle(Long idFactura, Long idProducto) {
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

    /** Lista los detalles de una factura. */
    public List<DetalleFactura> listarDetalles(Long idFactura) {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery(
                            "from DetalleFactura d where d.factura.idFactura = :fid order by d.producto.idProducto",
                            DetalleFactura.class)
                    .setParameter("fid", idFactura)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar los detalles de la factura " + idFactura + ": " + e.getMessage(), e);
        }
    }
}