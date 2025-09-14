package ar.edu.unlu.bd2.modelo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;


/**
 * Detalle de factura con clave compuesta (id_factura, id_producto).
 * Los valores de precio_unitario y subtotal los mantiene la BD (triggers),
 * por eso están marcados como solo lectura desde JPA.
 */
@Entity
@Table(name = "factura_detalle") // Cambiá si tu tabla se llama distinto
public class DetalleFactura {

    @EmbeddedId
    private DetalleFacturaId id = new DetalleFacturaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idFactura") // vincula id.idFactura con la FK
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idProducto") // vincula id.idProducto con la FK
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // Mantenidos por triggers en BD (solo lectura desde JPA)
    @Column(name = "precio_unitario", precision = 12, scale = 2,
            nullable = false, insertable = false, updatable = false)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 12, scale = 2,
            nullable = false, insertable = false, updatable = false)
    private BigDecimal subtotal;

    public DetalleFactura() { }

    public DetalleFactura(Factura factura, Producto producto, Integer cantidad) {
        this.factura = factura;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    // =================== Getters / Setters ===================

    public DetalleFacturaId getId() { return id; }
    public void setId(DetalleFacturaId id) { this.id = id; }

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; } // solo lectura
    public BigDecimal getSubtotal() { return subtotal; }             // solo lectura

    // =================== equals / hashCode / toString ===================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleFactura)) return false;
        DetalleFactura other = (DetalleFactura) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "DetalleFactura{" +
                "id=" + id +
                ", factura=" + (factura != null ? factura.getIdFactura() : null) +
                ", producto=" + (producto != null ? producto.getIdProducto() : null) +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}