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
    private Long idFactura;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    public DetalleFacturaId() { }

    public DetalleFacturaId(Long idFactura, Long idProducto) {
        this.idFactura = idFactura;
        this.idProducto = idProducto;
    }

    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

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