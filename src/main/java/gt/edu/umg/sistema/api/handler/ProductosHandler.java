package gt.edu.umg.sistema.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gt.edu.umg.sistema.api.dao.ProductoDAO;
import gt.edu.umg.sistema.api.model.ApiResponse;
import gt.edu.umg.sistema.api.model.Producto;
import java.io.IOException;
import java.util.List;

public class ProductosHandler implements HttpHandler {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CorsHelper.esOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            try {
                String path = exchange.getRequestURI().getPath(); // Ej: /api/productos o /api/productos/5
                String[] parts = path.split("/");

                if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                    // Buscar por ID: /api/productos/{id}
                    try {
                        int id = Integer.parseInt(parts[3].trim());
                        Producto p = productoDAO.buscarPorId(id);
                        if (p != null) {
                            CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Producto encontrado", p));
                        } else {
                            CorsHelper.enviarJson(exchange, 404, ApiResponse.error("Producto con ID " + id + " no encontrado"));
                        }
                    } catch (NumberFormatException e) {
                        CorsHelper.enviarJson(exchange, 400, ApiResponse.error("El ID debe ser un número entero"));
                    }
                } else {
                    // Listar todos
                    List<Producto> lista = productoDAO.listar();
                    CorsHelper.enviarJson(exchange, 200, ApiResponse.ok("Catálogo de productos obtenido exitosamente", lista));
                }
            } catch (Exception e) {
                System.err.println("Error en ProductosHandler: " + e.getMessage());
                CorsHelper.enviarJson(exchange, 500, ApiResponse.error("Error consultando productos: " + e.getMessage()));
            }
        } else {
            CorsHelper.enviarJson(exchange, 405, ApiResponse.error("Método no permitido. Use GET."));
        }
    }
}
