package ar.edu.unlu.bd2.modelo;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    public enum Estado { activo, inactivo }

    @Id
    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 120)
    private String apellido;

    @Column(name = "es_cuenta_corriente", nullable = false)
    private Boolean esCuentaCorriente = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16)
    private Estado estado = Estado.activo;

    // Relación 1..N con Factura (lado inverso; el dueño será Factura.cliente)
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private Set<Factura> facturas = new LinkedHashSet<>();

    public Cliente() { }

    public Cliente(Integer idCliente, String nombre, String apellido, Boolean esCuentaCorriente, Estado estado) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        if (esCuentaCorriente != null) this.esCuentaCorriente = esCuentaCorriente;
        if (estado != null) this.estado = estado;
    }

    // =================== Getters / Setters ===================

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Boolean getEsCuentaCorriente() { return esCuentaCorriente; }
    public void setEsCuentaCorriente(Boolean esCuentaCorriente) { this.esCuentaCorriente = esCuentaCorriente; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Set<Factura> getFacturas() { return facturas; }
    public void setFacturas(Set<Factura> facturas) { this.facturas = facturas; }

    // =================== Helpers para la relación ===================
    public void addFactura(Factura f) {
        if (f == null) return;
        facturas.add(f);
        f.setCliente(this);
    }

    public void removeFactura(Factura f) {
        if (f == null) return;
        facturas.remove(f);
        if (f.getCliente() == this) {
            f.setCliente(null);
        }
    }

    // =================== equals / hashCode / toString ===================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente other = (Cliente) o;
        // Para entidades con ID asignado manualmente, comparamos por ID si está seteado
        return idCliente != null && idCliente.equals(other.idCliente);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idCliente);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", esCuentaCorriente=" + esCuentaCorriente +
                ", estado=" + estado +
                '}';
    }
}