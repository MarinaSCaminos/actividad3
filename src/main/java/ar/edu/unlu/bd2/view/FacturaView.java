package ar.edu.unlu.bd2.view;

import ar.edu.unlu.bd2.controller.FacturaController;
import ar.edu.unlu.bd2.modelo.DetalleFactura;
import ar.edu.unlu.bd2.modelo.Factura;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FacturaView {

    private final FacturaController fCtrl = new FacturaController();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void menu() {
        while (true) {
            System.out.println("\n--- Facturas ---");
            System.out.println("1) Crear factura para un cliente");
            System.out.println("2) Listar todas");
            System.out.println("3) Listar por cliente");
            System.out.println("4) Ver factura (encabezado + detalles)");
            System.out.println("5) Agregar detalle");
            System.out.println("6) Actualizar cantidad de un detalle");
            System.out.println("7) Reemplazar producto de un detalle");
            System.out.println("8) Eliminar detalle");
            System.out.println("9) Eliminar factura");
            System.out.println("0) Volver");

            int opt = InputReader.nextInt("Opción: ");
            try {
                switch (opt) {
                    case 1 -> crearFacturaParaCliente();
                    case 2 -> listarTodas();
                    case 3 -> listarPorCliente();
                    case 4 -> verFactura();
                    case 5 -> agregarDetalle();
                    case 6 -> actualizarCantidadDetalle();
                    case 7 -> reemplazarProductoDetalle();
                    case 8 -> eliminarDetalle();
                    case 9 -> eliminarFactura();
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

    private void crearFacturaParaCliente() {
        System.out.println("\n> Crear factura");
        int idCliente = InputReader.nextInt("ID de cliente: ");
        Factura f = fCtrl.crearParaCliente(idCliente);
        System.out.println("✔ Factura creada. ID = " + f.getIdFactura());
    }

    private void listarTodas() {
        System.out.println("\n> Listado de facturas");
        List<Factura> lista = fCtrl.listarTodas();
        printTablaFacturas(lista);
    }

    private void listarPorCliente() {
        System.out.println("\n> Listado por cliente");
        int idCliente = InputReader.nextInt("ID de cliente: ");
        List<Factura> lista = fCtrl.listarPorCliente(idCliente);
        printTablaFacturas(lista);
    }

    private void verFactura() {
        System.out.println("\n> Ver factura");
        int  id = InputReader.nextInt("ID de factura: ");
        Optional<Factura> of = fCtrl.obtenerPorId(id);
        if (of.isEmpty()) {
            System.out.println("No existe factura con id=" + id);
            return;
        }
        Factura f = of.get();
        printEncabezado(f);
        List<DetalleFactura> dets = fCtrl.listarDetalles(id);
        printDetalles(dets);
        System.out.println("TOTAL: " + (f.getTotal() == null ? "(BD no informó total aún)" : f.getTotal()));
    }

    private void agregarDetalle() {
        System.out.println("\n> Agregar detalle");
        int idFactura = InputReader.nextInt("ID de factura: ");
        int idProducto = InputReader.nextInt("ID de producto: ");
        int cantidad = InputReader.nextInt("Cantidad (>0): ");
        DetalleFactura d = fCtrl.agregarDetalle(idFactura, idProducto, cantidad);
        System.out.println("✔ Detalle agregado: factura=" + idFactura + ", producto=" + idProducto +
                ", cant=" + d.getCantidad());
    }

    private void actualizarCantidadDetalle() {
        System.out.println("\n> Actualizar cantidad de detalle");
        int idFactura = InputReader.nextInt("ID de factura: ");
        int idProducto = InputReader.nextInt("ID de producto (actual): ");
        int nuevaCantidad = InputReader.nextInt("Nueva cantidad (>0): ");
        DetalleFactura d = fCtrl.actualizarCantidad(idFactura, idProducto, nuevaCantidad);
        System.out.println("✔ Cantidad actualizada. Nuevo subtotal: " + d.getSubtotal());
    }

    private void reemplazarProductoDetalle() {
        System.out.println("\n> Reemplazar producto en detalle");
        int idFactura = InputReader.nextInt("ID de factura: ");
        int idProductoActual = InputReader.nextInt("ID de producto actual: ");
        int idProductoNuevo = InputReader.nextInt("ID de producto nuevo: ");
        int cantidadNueva = InputReader.nextInt("Cantidad para el nuevo producto (>0): ");
        DetalleFactura d = fCtrl.reemplazarProducto(idFactura, idProductoActual, idProductoNuevo, cantidadNueva);
        System.out.println("✔ Producto reemplazado. Nuevo prod=" + d.getProducto().getIdProducto()
                + ", cantidad=" + d.getCantidad());
    }

    private void eliminarDetalle() {
        System.out.println("\n> Eliminar detalle");
        int idFactura = InputReader.nextInt("ID de factura: ");
        int idProducto = InputReader.nextInt("ID de producto: ");
        boolean ok = fCtrl.eliminarDetalle(idFactura, idProducto);
        if (ok) System.out.println("✔ Detalle eliminado.");
        else System.out.println("No existe ese detalle en la factura.");
    }

    private void eliminarFactura() {
        System.out.println("\n> Eliminar factura");
        int id = InputReader.nextInt("ID de factura: ");
        boolean ok = fCtrl.eliminar(id);
        if (ok) System.out.println("✔ Factura eliminada.");
        else System.out.println("No existe factura con id=" + id);
    }

    // ======================= Helpers de impresión =======================

    private void printTablaFacturas(List<Factura> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        System.out.printf("%-8s %-10s %-18s %-14s%n",
                "ID", "ClienteID", "Fecha", "Total");
        System.out.println("---------------------------------------------------");
        for (Factura f : lista) {
            System.out.printf("%-8d %-10d %-18s %-14s%n",
                    f.getIdFactura(),
                    f.getCliente() != null ? f.getCliente().getIdCliente() : null,
                    f.getFecha() != null ? FMT.format(f.getFecha()) : "",
                    fmtMoney(f.getTotal()));
        }
    }

    private void printEncabezado(Factura f) {
        System.out.println("\nFactura #" + f.getIdFactura());
        System.out.println("Cliente ID: " + (f.getCliente() != null ? f.getCliente().getIdCliente() : null));
        System.out.println("Fecha: " + (f.getFecha() != null ? FMT.format(f.getFecha()) : ""));
        System.out.println("Total (BD): " + fmtMoney(f.getTotal()));
    }

    private void printDetalles(List<DetalleFactura> dets) {
        System.out.println("\nDetalles:");
        if (dets == null || dets.isEmpty()) {
            System.out.println("(sin detalles)");
            return;
        }
        System.out.printf("%-10s %-10s %-10s %-14s %-14s%n",
                "FacturaID", "ProductoID", "Cantidad", "PrecioUnit", "Subtotal");
        System.out.println("------------------------------------------------------------------");
        for (DetalleFactura d : dets) {
            System.out.printf("%-10d %-10d %-10d %-14s %-14s%n",
                    d.getFactura() != null ? d.getFactura().getIdFactura() : null,
                    d.getProducto() != null ? d.getProducto().getIdProducto() : null,
                    d.getCantidad(),
                    fmtMoney(d.getPrecioUnitario()),
                    fmtMoney(d.getSubtotal()));
        }
    }

    private String fmtMoney(BigDecimal v) {
        return v == null ? "(null)" : v.toPlainString();
    }
}
