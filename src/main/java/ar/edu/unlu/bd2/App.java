package ar.edu.unlu.bd2;


import ar.edu.unlu.bd2.view.*;

/**
 * Menú principal de la app CLI.
 * Orquesta los submenús de Clientes, Productos, Facturas y Detalles.
 */
public class App {

    public static void main(String[] args) {
        new App().run();
    }

    private void run() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("  ORM Prueba - Hibernate (CLI) ");
            System.out.println("===============================");
            System.out.println("1) Clientes");
            System.out.println("2) Productos");
            System.out.println("3) Facturas");
            System.out.println("4) Detalles de Factura");
            System.out.println("0) Salir");

            int opt = InputReader.nextInt("Opción: ");

            try {
                switch (opt) {
                    case 1 -> new ClienteView().menu();
                    case 2 -> new ProductoView().menu();
                    case 3 -> new FacturaView().menu();
                    case 4 -> new DetalleFacturaView().menu();
                    case 0 -> {
                        System.out.println("¡Hasta luego!");
                        return;
                    }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (RuntimeException e) {
                // Muestra errores de negocio/BD sin romper la app
                System.err.println("Error: " + e.getMessage());
            }

            InputReader.pressEnterToContinue();
        }
    }
}