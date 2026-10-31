package gt.edu.umg.sistema.api.model;

import java.util.ArrayList;
import java.util.List;

public class CrearPedidoRequest {
    private String nombreCliente;
    private String direccionEnvio;
    private String telefono;
    private String estado; // Opcional, por defecto "PENDIENTE"
    private Integer productoId; // Formato simple: producto único
    private Integer cantidad;   // Formato simple: cantidad
    private List<ItemPedidoRequest> items = new ArrayList<>();

    public CrearPedidoRequest() {}

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDireccionEnvio() {
        if (direccionEnvio == null || direccionEnvio.trim().isEmpty()) {
            return "Ciudad de Guatemala";
        }
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getTelefono() {
        if (telefono == null || telefono.trim().isEmpty()) {
            return "N/A";
        }
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public List<ItemPedidoRequest> getItems() {
        if ((items == null || items.isEmpty()) && productoId != null && cantidad != null) {
            items = new ArrayList<>();
            items.add(new ItemPedidoRequest(productoId, cantidad));
        }
        return items;
    }

    public void setItems(List<ItemPedidoRequest> items) {
        this.items = items;
    }
}
