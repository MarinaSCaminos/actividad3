package ar.edu.unlu.bd2.controller;

import ar.edu.unlu.bd2.modelo.Cliente;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Optional;
/**
 * CRUD de Cliente usando Hibernate puro y hibernate.cfg.xml.
 * Nota: Para simplificar y evitar dependencias extra, cada controller mantiene su propio SessionFactory estático.
 * En un refactor posterior se puede extraer a un HibernateUtil compartido.
 */
public class ClienteController {

    // ======= SessionFactory único por clase =======
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Carga hibernate.cfg.xml (ya mapea las entidades con <mapping class="..."/>)
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Propagamos con detalle si la configuración falla
            throw new ExceptionInInitializerError("Error creando SessionFactory: " + ex.getMessage());
        }
    }

    // ======= Métodos CRUD =======

    public Cliente crear(Cliente cliente) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            session.persist(cliente);
            tx.commit();
            return cliente;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo crear el cliente: " + e.getMessage(), e);
        }
    }

    public Optional<Cliente> obtenerPorId(Integer idCliente) {
        try (Session session = SESSION_FACTORY.openSession()) {
            Cliente c = session.get(Cliente.class, idCliente);
            return Optional.ofNullable(c);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el cliente id=" + idCliente + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Cliente> listarTodos() {
        try (Session session = SESSION_FACTORY.openSession()) {
            return session.createQuery("from Cliente c order by c.idCliente").list();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo listar clientes: " + e.getMessage(), e);
        }
    }

    public Cliente actualizar(Cliente cliente) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            // merge devuelve la instancia administrada; útil si 'cliente' vino detachado
            Cliente managed = (Cliente) session.merge(cliente);
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("No se pudo actualizar el cliente id=" + cliente.getIdCliente() + ": " + e.getMessage(), e);
        }
    }

    public boolean eliminar(Integer idCliente) {
        Transaction tx = null;
        try (Session session = SESSION_FACTORY.openSession()) {
            tx = session.beginTransaction();
            Cliente c = session.get(Cliente.class, idCliente);
            if (c == null) {
                tx.rollback();
                return false; // no existe
            }
            session.remove(c);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            // Si hay FK (p.ej., facturas del cliente), la BD puede bloquear el delete
            throw new RuntimeException(
                    "No se pudo eliminar el cliente id=" + idCliente +
                            ". Posible restricción por facturas asociadas. Detalle: " + e.getMessage(), e);
        }
    }

    // ======= Utilidades =======

    public boolean existe(Integer idCliente) {
        return obtenerPorId(idCliente).isPresent();
    }
}