package ar.edu.unlu.bd2.view;


import ar.edu.unlu.bd2.controller.ClienteController;
import ar.edu.unlu.bd2.modelo.Cliente;

import java.util.List;
import java.util.Optional;

public class ClienteView {

    private final ClienteController controller = new ClienteController();

    public void menu() {
        while (true) {
            System.out.println("\n--- Clientes ---");
            System.out.println("1) Alta de cliente");
            System.out.println("2) Listar clientes");
            System.out.println("3) Buscar por ID");
            System.out.println("4) Actualizar cliente");
            System.out.println("5) Eliminar cliente");
            System.out.println("0) Volver");

            int opt = InputReader.nextInt("Opción: ");
            switch (opt) {
                case 1 -> alta();
                case 2 -> listar();
                case 3 -> buscarPorId();
                case 4 -> actualizar();
                case 5 -> eliminar();
                case 0 -> { return; }
                default -> System.out.println("Opción inválida.");
            }
            InputReader.pressEnterToContinue();
        }
    }

    // ======================= Acciones =======================

    private void alta() {
        System.out.println("\n> Alta de cliente");
        int id = InputReader.nextInt("ID (entero, no autoincremental): ");
        String nombre = InputReader.nextNonEmpty("Nombre: ");
        String apellido = InputReader.nextNonEmpty("Apellido: ");
        boolean cc = InputReader.nextBoolean("¿Es cuenta corriente?");
        Cliente.Estado estado = InputReader.nextEnum("Estado", Cliente.Estado.class);

        Cliente c = new Cliente(id, nombre, apellido, cc, estado);
        controller.crear(c);
        System.out.println("✔ Cliente creado.");
    }

    private void listar() {
        System.out.println("\n> Listado de clientes");
        List<Cliente> lista = controller.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        System.out.printf("%-10s %-15s %-15s %-8s %-10s%n",
                "ID", "Nombre", "Apellido", "CC", "Estado");
        System.out.println("------------------------------------------------------");
        for (Cliente c : lista) {
            System.out.printf("%-10d %-15s %-15s %-8s %-10s%n",
                    c.getIdCliente(),
                    c.getNombre(),
                    c.getApellido(),
                    Boolean.TRUE.equals(c.getEsCuentaCorriente()) ? "Sí" : "No",
                    c.getEstado());
        }
    }

    private void buscarPorId() {
        System.out.println("\n> Buscar cliente");
        int id = InputReader.nextInt("ID: ");
        Optional<Cliente> oc = controller.obtenerPorId(id);
        if (oc.isEmpty()) {
            System.out.println("No existe cliente con id=" + id);
            return;
        }
        printDetalle(oc.get());
    }

    private void actualizar() {
        System.out.println("\n> Actualizar cliente");
        int id = InputReader.nextInt("ID: ");
        Optional<Cliente> oc = controller.obtenerPorId(id);
        if (oc.isEmpty()) {
            System.out.println("No existe cliente con id=" + id);
            return;
        }
        Cliente c = oc.get();
        System.out.println("Valores actuales:");
        printDetalle(c);

        String nombre = InputReader.nextNonEmpty("Nuevo nombre: ");
        String apellido = InputReader.nextNonEmpty("Nuevo apellido: ");
        boolean cc = InputReader.nextBoolean("¿Es cuenta corriente?");
        Cliente.Estado estado = InputReader.nextEnum("Estado", Cliente.Estado.class);

        c.setNombre(nombre);
        c.setApellido(apellido);
        c.setEsCuentaCorriente(cc);
        c.setEstado(estado);

        controller.actualizar(c);
        System.out.println("✔ Cliente actualizado.");
    }

    private void eliminar() {
        System.out.println("\n> Eliminar cliente");
        int id = InputReader.nextInt("ID: ");
        boolean ok = controller.eliminar(id);
        if (ok) System.out.println("✔ Cliente eliminado.");
        else System.out.println("No existe cliente con id=" + id);
    }

    // ======================= Helpers =======================

    private void printDetalle(Cliente c) {
        System.out.println("ID: " + c.getIdCliente());
        System.out.println("Nombre: " + c.getNombre());
        System.out.println("Apellido: " + c.getApellido());
        System.out.println("Cuenta Corriente: " + (Boolean.TRUE.equals(c.getEsCuentaCorriente()) ? "Sí" : "No"));
        System.out.println("Estado: " + c.getEstado());
        System.out.println("Facturas asociadas: " + (c.getFacturas() == null ? 0 : c.getFacturas().size()));
    }
}