package ar.edu.unlu.bd2.modelo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT en MySQL
    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    // Relación 1..N con DetalleFactura (lado inverso; el dueño será DetalleFactura.producto)
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private Set<DetalleFactura> detalles = new LinkedHashSet<>();

    public Producto() { }

    public Producto(String nombre, BigDecimal precioUnitario, Integer stock, Boolean activo) {
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        if (stock != null) this.stock = stock;
        if (activo != null) this.activo = activo;
    }

    // =================== Getters / Setters ===================

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Set<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(Set<DetalleFactura> detalles) { this.detalles = detalles; }

    // =================== Helpers relación ===================
    public void addDetalle(DetalleFactura d) {
        if (d == null) return;
        detalles.add(d);
        d.setProducto(this);
    }

    public void removeDetalle(DetalleFactura d) {
        if (d == null) return;
        detalles.remove(d);
        if (d.getProducto() == this) {
            d.setProducto(null);
        }
    }

    // =================== equals / hashCode / toString ===================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto other = (Producto) o;
        return idProducto != null && idProducto.equals(other.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idProducto);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", precioUnitario=" + precioUnitario +
                ", stock=" + stock +
                ", activo=" + activo +
                '}';
    }
}