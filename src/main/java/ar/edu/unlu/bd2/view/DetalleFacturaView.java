package ar.edu.unlu.bd2.view;


import ar.edu.unlu.bd2.controller.DetalleFacturaController;
import ar.edu.unlu.bd2.modelo.DetalleFactura;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DetalleFacturaView {

    private final DetalleFacturaController controller = new DetalleFacturaController();

    public void menu() {
        while (true) {
            System.out.println("\n--- Detalles de Factura ---");
            System.out.println("1) Crear detalle");
            System.out.println("2) Obtener detalle por (factura, producto)");
            System.out.println("3) Listar detalles por factura");
            System.out.println("4) Actualizar cantidad (DELETE + INSERT)");
            System.out.println("5) Reemplazar producto (DELETE + INSERT)");
            System.out.println("6) Eliminar detalle");
            System.out.println("0) Volver");

            int opt = InputReader.nextInt("Opción: ");
            try {
                switch (opt) {
                    case 1 -> crear();
                    case 2 -> obtener();
                    case 3 -> listarPorFactura();
                    case 4 -> actualizarCantidad();
                    case 5 -> reemplazarProducto();
                    case 6 -> eliminar();
                    case 0 -> { return; }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("✖ " + e.getMessage());
            }
            InputReader.pressEnterToContinue();
        }
    }

    // ======================= Acciones =======================

    private void crear() {
        System.out.println("\n> Crear detalle");
        long idFactura = InputReader.nextLong("ID de factura: ");
        long idProducto = InputReader.nextLong("ID de producto: ");
        int cantidad = InputReader.nextInt("Cantidad (>0): ");
        DetalleFactura d = controller.crear(idFactura, idProducto, cantidad);
        System.out.println("✔ Detalle creado. Factura=" + idFactura + " Producto=" + idProducto +
                " Cant=" + d.getCantidad());
        printUnaLinea(d);
    }

    private void obtener() {
        System.out.println("\n> Obtener detalle");
        long idFactura = InputReader.nextLong("ID de factura: ");
        long idProducto = InputReader.nextLong("ID de producto: ");
        Optional<DetalleFactura> od = controller.obtener(idFactura, idProducto);
        if (od.isEmpty()) {
            System.out.println("No existe detalle (factura=" + idFactura + ", producto=" + idProducto + ").");
            return;
        }
        printDetalle(od.get());
    }

    private void listarPorFactura() {
        System.out.println("\n> Listar detalles por factura");
        long idFactura = InputReader.nextLong("ID de factura: ");
        List<DetalleFactura> dets = controller.listarPorFactura(idFactura);
        printTabla(dets);
    }

    private void actualizarCantidad() {
        System.out.println("\n> Actualizar cantidad");
        long idFactura = InputReader.nextLong("ID de factura: ");
        long idProducto = InputReader.nextLong("ID de producto (actual): ");
        int nuevaCantidad = InputReader.nextInt("Nueva cantidad (>0): ");
        DetalleFactura d = controller.actualizarCantidad(idFactura, idProducto, nuevaCantidad);
        System.out.println("✔ Cantidad actualizada.");
        printUnaLinea(d);
    }

    private void reemplazarProducto() {
        System.out.println("\n> Reemplazar producto");
        long idFactura = InputReader.nextLong("ID de factura: ");
        long idProductoActual = InputReader.nextLong("ID de producto actual: ");
        long idProductoNuevo = InputReader.nextLong("ID de producto nuevo: ");
        int cantidadNueva = InputReader.nextInt("Cantidad para el nuevo producto (>0): ");
        DetalleFactura d = controller.reemplazarProducto(idFactura, idProductoActual, idProductoNuevo, cantidadNueva);
        System.out.println("✔ Producto reemplazado.");
        printUnaLinea(d);
    }

    private void eliminar() {
        System.out.println("\n> Eliminar detalle");
        long idFactura = InputReader.nextLong("ID de factura: ");
        long idProducto = InputReader.nextLong("ID de producto: ");
        boolean ok = controller.eliminar(idFactura, idProducto);
        if (ok) System.out.println("✔ Detalle eliminado.");
        else System.out.println("No existe ese detalle.");
    }

    // ======================= Helpers de impresión =======================

    private void printTabla(List<DetalleFactura> dets) {
        if (dets == null || dets.isEmpty()) {
            System.out.println("(sin detalles)");
            return;
        }
        System.out.printf("%-10s %-10s %-10s %-14s %-14s%n",
                "FacturaID", "ProductoID", "Cantidad", "PrecioUnit", "Subtotal");
        System.out.println("------------------------------------------------------------------");
        for (DetalleFactura d : dets) {
            printLineaTabla(d);
        }
    }

    private void printUnaLinea(DetalleFactura d) {
        System.out.printf("%-10s %-10s %-10s %-14s %-14s%n",
                "FacturaID", "ProductoID", "Cantidad", "PrecioUnit", "Subtotal");
        System.out.println("------------------------------------------------------------------");
        printLineaTabla(d);
    }

    private void printLineaTabla(DetalleFactura d) {
        Long fid = d.getFactura() != null ? d.getFactura().getIdFactura() : null;
        Long pid = d.getProducto() != null ? d.getProducto().getIdProducto() : null;
        System.out.printf("%-10d %-10d %-10d %-14s %-14s%n",
                fid, pid, d.getCantidad(), fmtMoney(d.getPrecioUnitario()), fmtMoney(d.getSubtotal()));
    }

    private void printDetalle(DetalleFactura d) {
        System.out.println("FacturaID: " + (d.getFactura() != null ? d.getFactura().getIdFactura() : null));
        System.out.println("ProductoID: " + (d.getProducto() != null ? d.getProducto().getIdProducto() : null));
        System.out.println("Cantidad: " + d.getCantidad());
        System.out.println("Precio unitario (BD): " + fmtMoney(d.getPrecioUnitario()));
        System.out.println("Subtotal (BD): " + fmtMoney(d.getSubtotal()));
    }

    private String fmtMoney(BigDecimal v) {
        return v == null ? "(null)" : v.toPlainString();
    }
}
