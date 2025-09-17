package ar.edu.unlu.bd2.modelo;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
/**
 * Clave primaria compuesta para DetalleFactura: (id_factura, id_producto).
 * Es @Embeddable y se usa desde DetalleFactura con @EmbeddedId.
 */
@Embeddable
public class DetalleFacturaId implements Serializable {

    @Column(name = "id_factura", nullable = false)
    private Integer idFactura;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    public DetalleFacturaId() { }

    public DetalleFacturaId(Integer idFactura, Integer idProducto) {
        this.idFactura = idFactura;
        this.idProducto = idProducto;
    }

    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleFacturaId)) return false;
        DetalleFacturaId that = (DetalleFacturaId) o;
        return Objects.equals(idFactura, that.idFactura)
                && Objects.equals(idProducto, that.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFactura, idProducto);
    }
}