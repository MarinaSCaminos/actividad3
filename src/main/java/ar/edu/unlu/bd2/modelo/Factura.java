package ar.edu.unlu.bd2.modelo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGINT AUTO_INCREMENT en MySQL
    @Column(name = "id_factura", nullable = false)
    private Long idFactura;

    // Muchas facturas pertenecen a un cliente
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Usamos LocalDateTime; Hibernate lo mapea a DATETIME/TIMESTAMP
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // El total lo recalculan triggers en BD; lo marcamos solo-lectura desde JPA
    @Column(name = "total", precision = 12, scale = 2, nullable = false, insertable = false, updatable = false)
    private BigDecimal total;

    // Relación 1..N con DetalleFactura (lado inverso; dueño = DetalleFactura.factura)
    @OneToMany(mappedBy = "factura", fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            orphanRemoval = true)
    private Set<DetalleFactura> detalles = new LinkedHashSet<>();

    public Factura() { }

    public Factura(Cliente cliente) {
        this.cliente = cliente;
    }

    // =================== Getters / Setters ===================

    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    // sin setter de total: lo mantiene la BD por triggers

    public Set<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(Set<DetalleFactura> detalles) { this.detalles = detalles; }

    // =================== Helpers relación ===================
    public void addDetalle(DetalleFactura d) {
        if (d == null) return;
        detalles.add(d);
        d.setFactura(this);
    }

    public void removeDetalle(DetalleFactura d) {
        if (d == null) return;
        detalles.remove(d);
        if (d.getFactura() == this) {
            d.setFactura(null);
        }
    }

    // =================== equals / hashCode / toString ===================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura other = (Factura) o;
        return idFactura != null && idFactura.equals(other.idFactura);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idFactura);
    }

    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", cliente=" + (cliente != null ? cliente.getIdCliente() : null) +
                ", fecha=" + fecha +
                ", total=" + total +
                ", detalles=" + detalles.size() +
                '}';
    }
}