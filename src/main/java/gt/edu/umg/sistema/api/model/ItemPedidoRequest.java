package gt.edu.umg.sistema.api.model;

public class ItemPedidoRequest {
    private int productoId;
    private int cantidad;

    public ItemPedidoRequest() {}

    public ItemPedidoRequest(int productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
