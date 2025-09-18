package ar.edu.unlu.bd2;

import ar.edu.unlu.bd2.controller.ClienteController;
import ar.edu.unlu.bd2.controller.DetalleFacturaController;
import ar.edu.unlu.bd2.controller.FacturaController;
import ar.edu.unlu.bd2.controller.ProductoController;
import ar.edu.unlu.bd2.view.ClienteView;
import ar.edu.unlu.bd2.view.ProductoView;
import ar.edu.unlu.bd2.view.FacturaView;
import ar.edu.unlu.bd2.view.DetalleFacturaView;
import ar.edu.unlu.bd2.view.InputReader;

public class App {
    public static void main(String[] args) {
        registerShutdownHook();
        // Views sin inyección (cada view crea su controller interno)
        ClienteView clienteView = new ClienteView();
        ProductoView productoView = new ProductoView();
        FacturaView facturaView = new FacturaView();
        DetalleFacturaView detalleView = new DetalleFacturaView();

        String op;
        do {
            System.out.println("\n===== MENÚ PRINCIPAL =====");
            System.out.println("1) Clientes");
            System.out.println("2) Productos");
            System.out.println("3) Facturas");
            System.out.println("4) Detalles de Factura");
            System.out.println("0) Salir");
            op = InputReader.nextLine("> ");

            switch (op) {
                case "1" -> clienteView.menu();
                case "2" -> productoView.menu();
                case "3" -> facturaView.menu();
                case "4" -> detalleView.menu();
                case "0" -> System.out.println("Saliendo...");
                default  -> System.out.println("Opción inválida");
            }
        } while (!"0".equals(op));
    }

    /** Cierra los SessionFactory para que no queden hilos vivos (pool de conexiones). */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { ClienteController.shutdown(); } catch (Exception ignored) {}
            try { ProductoController.shutdown(); } catch (Exception ignored) {}
            try { FacturaController.shutdown(); } catch (Exception ignored) {}
            try { DetalleFacturaController.shutdown(); } catch (Exception ignored) {}
        }, "hibernate-shutdown-hook"));
    }

}
