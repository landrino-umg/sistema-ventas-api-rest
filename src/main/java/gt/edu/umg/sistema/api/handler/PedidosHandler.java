package gt.edu.umg.sistema.api.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gt.edu.umg.sistema.api.dao.PedidoDAO;
import gt.edu.umg.sistema.api.model.ApiResponse;
import gt.edu.umg.sistema.api.model.CrearPedidoRequest;
import gt.edu.umg.sistema.api.model.Pedido;
import java.io.IOException;
import java.util.List;

public class PedidosHandler implements HttpHandler {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CorsHelper.esOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath(); // /api/pedidos o /api/pedidos/{id}
        String[] parts = path.split("/");

        Integer idParam = null;
        if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
            try {
                idParam = Integer.parseInt(parts[3].trim());
            } catch (NumberFormatException e) {
                CorsHelper.enviarJson(exchange, 400, ApiResponse.error("El ID de la orden debe ser un número entero válido"));
                return;
            }
        }

        try {
            switch (method) {
                case "GET":
                    if (idParam != null) {
                        Pedido p = pedidoDAO.buscarPorId(idParam);
                        if (p != null) {
                            CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Orden encontrada", p));
                        } else {
                            CorsHelper.enviarJson(exchange, 404, ApiResponse.error("Orden con ID " + idParam + " no encontrada"));
                        }
                    } else {
                        List<Pedido> lista = pedidoDAO.listar();
                        CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Listado de órdenes obtenido exitosamente", lista));
                    }
                    break;

                case "POST":
                    // Crear nueva Orden de Venta
                    String bodyPost = CorsHelper.leerCuerpoString(exchange);
                    if (bodyPost.trim().isEmpty()) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("El cuerpo de la solicitud (JSON) no puede estar vacío"));
                        return;
                    }

                    CrearPedidoRequest reqPost;
                    try {
                        reqPost = CorsHelper.getGson().fromJson(bodyPost, CrearPedidoRequest.class);
                    } catch (JsonSyntaxException e) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("Formato JSON inválido: " + e.getMessage()));
                        return;
                    }

                    Pedido pedidoCreado = pedidoDAO.crear(reqPost);
                    CorsHelper.enviarJson(exchange, 201, ApiResponse.ok("Orden de Venta creada exitosamente en la base de datos", pedidoCreado));
                    break;

                case "PUT":
                    // Editar/actualizar Orden de Venta existente
                    if (idParam == null) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("Debe especificar el ID de la orden a editar en la URL: /api/pedidos/{id}"));
                        return;
                    }

                    String bodyPut = CorsHelper.leerCuerpoString(exchange);
                    if (bodyPut.trim().isEmpty()) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("El cuerpo de la solicitud (JSON) no puede estar vacío"));
                        return;
                    }

                    CrearPedidoRequest reqPut;
                    try {
                        reqPut = CorsHelper.getGson().fromJson(bodyPut, CrearPedidoRequest.class);
                    } catch (JsonSyntaxException e) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("Formato JSON inválido: " + e.getMessage()));
                        return;
                    }

                    Pedido pedidoActualizado = pedidoDAO.actualizar(idParam, reqPut);
                    CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Orden de Venta #" + idParam + " actualizada exitosamente", pedidoActualizado));
                    break;

                case "DELETE":
                    if (idParam == null) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("Debe especificar el ID de la orden a eliminar en la URL: /api/pedidos/{id}"));
                        return;
                    }

                    boolean eliminado = pedidoDAO.eliminar(idParam);
                    if (eliminado) {
                        CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Orden #" + idParam + " eliminada exitosamente", null));
                    } else {
                        CorsHelper.enviarJson(exchange, 404, ApiResponse.error("Orden #" + idParam + " no encontrada"));
                    }
                    break;

                default:
                    CorsHelper.enviarJson(exchange, 405, ApiResponse.error("Método no permitido. Use GET, POST, PUT o DELETE."));
                    break;
            }
        } catch (IllegalArgumentException e) {
            CorsHelper.enviarJson(exchange, 400, ApiResponse.error("Validación de datos: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error procesando solicitud en PedidosHandler: " + e.getMessage());
            e.printStackTrace();
            CorsHelper.enviarJson(exchange, 500, ApiResponse.error("Error interno del servidor: " + e.getMessage()));
        }
    }
}
