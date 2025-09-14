package ar.edu.unlu.bd2.view;


import ar.edu.unlu.bd2.controller.ProductoController;
import ar.edu.unlu.bd2.modelo.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoView {

    private final ProductoController controller = new ProductoController();

    public void menu() {
        while (true) {
            System.out.println("\n--- Productos ---");
            System.out.println("1) Alta de producto");
            System.out.println("2) Listar todos");
            System.out.println("3) Listar activos");
            System.out.println("4) Buscar por ID");
            System.out.println("5) Cambiar precio");
            System.out.println("6) Ajustar stock (+/-)");
            System.out.println("7) Activar / Desactivar");
            System.out.println("8) Eliminar");
            System.out.println("0) Volver");

            int opt = InputReader.nextInt("Opción: ");
            switch (opt) {
                case 1 -> alta();
                case 2 -> listarTodos();
                case 3 -> listarActivos();
                case 4 -> buscarPorId();
                case 5 -> cambiarPrecio();
                case 6 -> ajustarStock();
                case 7 -> activarDesactivar();
                case 8 -> eliminar();
                case 0 -> { return; }
                default -> System.out.println("Opción inválida.");
            }
            InputReader.pressEnterToContinue();
        }
    }

    // ======================= Acciones =======================

    private void alta() {
        System.out.println("\n> Alta de producto");
        String nombre = InputReader.nextNonEmpty("Nombre: ");
        BigDecimal precio = InputReader.nextPositiveMoney("Precio unitario: ");
        int stock = InputReader.nextIntInRange("Stock inicial (>=0): ", 0, Integer.MAX_VALUE);
        boolean activo = InputReader.nextBoolean("¿Activo?");

        Producto p = new Producto(nombre, precio, stock, activo);
        controller.crear(p);
        System.out.println("✔ Producto creado con ID: " + p.getIdProducto());
    }

    private void listarTodos() {
        System.out.println("\n> Listado de productos (todos)");
        List<Producto> lista = controller.listarTodos();
        printTabla(lista);
    }

    private void listarActivos() {
        System.out.println("\n> Listado de productos (activos)");
        List<Producto> lista = controller.listarActivos();
        printTabla(lista);
    }

    private void buscarPorId() {
        System.out.println("\n> Buscar producto");
        long id = InputReader.nextLong("ID: ");
        Optional<Producto> op = controller.obtenerPorId(id);
        if (op.isEmpty()) {
            System.out.println("No existe producto con id=" + id);
            return;
        }
        printDetalle(op.get());
    }

    private void cambiarPrecio() {
        System.out.println("\n> Cambiar precio");
        long id = InputReader.nextLong("ID: ");
        BigDecimal nuevo = InputReader.nextPositiveMoney("Nuevo precio: ");
        boolean ok = controller.actualizarPrecio(id, nuevo);
        if (ok) System.out.println("✔ Precio actualizado.");
        else System.out.println("No existe producto con id=" + id);
    }

    private void ajustarStock() {
        System.out.println("\n> Ajustar stock (+/-)");
        long id = InputReader.nextLong("ID: ");
        int delta = InputReader.nextInt("Delta (ej: +5 o -3): ");
        try {
            Integer nuevo = controller.ajustarStock(id, delta);
            System.out.println("✔ Stock resultante: " + nuevo);
        } catch (RuntimeException e) {
            System.out.println("✖ " + e.getMessage());
        }
    }

    private void activarDesactivar() {
        System.out.println("\n> Activar / Desactivar producto");
        long id = InputReader.nextLong("ID: ");
        boolean activo = InputReader.nextBoolean("¿Activar?");
        boolean ok = controller.activarDesactivar(id, activo);
        if (ok) System.out.println("✔ Estado actualizado.");
        else System.out.println("No existe producto con id=" + id);
    }

    private void eliminar() {
        System.out.println("\n> Eliminar producto");
        long id = InputReader.nextLong("ID: ");
        boolean ok = controller.eliminar(id);
        if (ok) System.out.println("✔ Producto eliminado.");
        else System.out.println("No existe producto con id=" + id);
    }

    // ======================= Helpers =======================

    private void printTabla(List<Producto> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        System.out.printf("%-8s %-22s %-14s %-8s %-8s%n",
                "ID", "Nombre", "Precio", "Stock", "Activo");
        System.out.println("-----------------------------------------------------------");
        for (Producto p : lista) {
            System.out.printf("%-8d %-22s %-14s %-8d %-8s%n",
                    p.getIdProducto(),
                    p.getNombre(),
                    p.getPrecioUnitario(),
                    p.getStock(),
                    Boolean.TRUE.equals(p.getActivo()) ? "Sí" : "No");
        }
    }

    private void printDetalle(Producto p) {
        System.out.println("ID: " + p.getIdProducto());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecioUnitario());
        System.out.println("Stock: " + p.getStock());
        System.out.println("Activo: " + (Boolean.TRUE.equals(p.getActivo()) ? "Sí" : "No"));
        System.out.println("Detalles de factura asociados: " + (p.getDetalles() == null ? 0 : p.getDetalles().size()));
    }
}
