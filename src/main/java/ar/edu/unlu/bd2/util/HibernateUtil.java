package ar.edu.unlu.bd2.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    // SessionFactory único de la aplicación
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() { }

    private static SessionFactory buildSessionFactory() {
        try {
            // Lee src/main/resources/hibernate.cfg.xml
            Configuration cfg = new Configuration().configure("hibernate.cfg.xml");

            SessionFactory sf = cfg.buildSessionFactory();

            // Cierre ordenado al finalizar la JVM
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (sf != null && !sf.isClosed()) sf.close();
                } catch (Throwable ignored) {}
            }));

            return sf;
        } catch (Throwable ex) {
            System.err.println("Error construyendo SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Obtiene el SessionFactory global */
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    /** Cierra explícitamente (por si querés llamar al salir del menú) */
    public static void shutdown() {
        try {
            if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
                SESSION_FACTORY.close();
            }
        } catch (Throwable ignored) {}
    }
}