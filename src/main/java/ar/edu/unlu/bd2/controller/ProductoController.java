package ar.edu.unlu.bd2.controller;

import ar.edu.unlu.bd2.modelo.Producto;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    // ======= SessionFactory único por clase (simple, estilo del profe) =======
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Error creando SessionFactory: " + ex.getMessage());
        }
    }

    // ======= CRUD =======

    public Producto crear(Producto producto) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            session.persist(producto);
            tx.commit();
            return producto;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo crear el producto: " + e.getMessage(), e);
        }
    }

    public Optional<Producto> obtenerPorId(Long idProducto) {
        try (Session session = SESSION_FACTORY.openSession()) {
            Producto p = session.get(Producto.class, idProducto);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el producto id=" + idProducto + ": " + e.getMessage(), e);
        }
    }

    public List<Producto> listarTodos() {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery("from Producto p order by p.idProducto", Producto.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo listar productos: " + e.getMessage(), e);
        }
    }

    public List<Producto> listarActivos() {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery(
                            "from Producto p where p.activo = true order by p.idProducto",
                            Producto.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo listar productos activos: " + e.getMessage(), e);
        }
    }

    public Producto actualizar(Producto producto) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Producto managed = session.merge(producto);
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo actualizar el producto id=" + producto.getIdProducto() + ": " + e.getMessage(), e);
        }
    }

    public boolean eliminar(Long idProducto) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Producto p = session.get(Producto.class, idProducto);
            if (p == null) {
                tx.rollback();
                return false;
            }
            session.remove(p);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            // Si hay detalles de factura que referencian este producto, la BD impedirá el delete.
            throw new RuntimeException(
                    "No se pudo eliminar el producto id=" + idProducto +
                            ". Posible restricción por detalles de factura asociados. Detalle: " + e.getMessage(), e);
        }
    }

    // ======= Operaciones específicas =======

    public boolean activarDesactivar(Long idProducto, boolean activo) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Producto p = session.get(Producto.class, idProducto);
            if (p == null) {
                tx.rollback();
                return false;
            }
            p.setActivo(activo);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo cambiar el estado del producto id=" + idProducto + ": " + e.getMessage(), e);
        }
    }

    public boolean actualizarPrecio(Long idProducto, BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser nulo ni negativo");
        }
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Producto p = session.get(Producto.class, idProducto);
            if (p == null) {
                tx.rollback();
                return false;
            }
            p.setPrecioUnitario(nuevoPrecio);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo actualizar el precio del producto id=" + idProducto + ": " + e.getMessage(), e);
        }
    }

    /**
     * Ajusta el stock del producto sumando 'delta' (puede ser negativo).
     * NOTA: En el flujo real, los triggers de BD actualizan stock al insertar/borrar detalles de factura.
     * Este método es útil para correcciones manuales.
     */
    public Integer ajustarStock(Long idProducto, int delta) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();

            // Bloqueo pesimista para evitar carreras al ajustar stock manualmente
            Producto p = session.get(Producto.class, idProducto, new LockOptions(LockMode.PESSIMISTIC_WRITE));
            if (p == null) {
                tx.rollback();
                throw new IllegalArgumentException("Producto inexistente id=" + idProducto);
            }

            int nuevo = (p.getStock() == null ? 0 : p.getStock()) + delta;
            if (nuevo < 0) {
                tx.rollback();
                throw new IllegalStateException("Stock resultante negativo (" + nuevo + ") para id=" + idProducto);
            }
            p.setStock(nuevo);

            tx.commit();
            return nuevo;
        } catch (RuntimeException e) {
            // No volver a envolver para que se conserven mensajes claros (IllegalArgument/IllegalState)
            throw e;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo ajustar el stock del producto id=" + idProducto + ": " + e.getMessage(), e);
        }
    }
}