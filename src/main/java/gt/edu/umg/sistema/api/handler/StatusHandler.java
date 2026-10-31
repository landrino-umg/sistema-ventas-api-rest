package gt.edu.umg.sistema.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gt.edu.umg.sistema.api.config.DatabaseConfig;
import gt.edu.umg.sistema.api.model.ApiResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatusHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (CorsHelper.esOptions(exchange)) {
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            CorsHelper.enviarJson(exchange, 405, ApiResponse.error("Método no permitido. Use GET."));
            return;
        }

        boolean dbOk = DatabaseConfig.checkConnection();

        Map<String, Object> statusData = new LinkedHashMap<>();
        statusData.put("servicio", "Sistema de Ventas - API REST");
        statusData.put("version", "1.0.0");
        statusData.put("estado", "OPERACIONAL");
        statusData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        statusData.put("baseDatosConectada", dbOk);
        statusData.put("baseDatosHost", DatabaseConfig.getHost());
        statusData.put("baseDatosNombre", DatabaseConfig.getDatabase());

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("GET /api/status", "Comprueba el estado del servicio y la base de datos");
        endpoints.put("GET /api/productos", "Lista el catálogo de productos con precios y existencias");
        endpoints.put("GET /api/pedidos", "Lista todas las órdenes de venta registradas");
        endpoints.put("GET /api/pedidos/{id}", "Obtiene el detalle de una orden por su ID");
        endpoints.put("POST /api/pedidos", "Crea una nueva Orden de Venta en la base de datos");
        endpoints.put("PUT /api/pedidos/{id}", "Edita o actualiza una Orden de Venta existente");
        endpoints.put("DELETE /api/pedidos/{id}", "Elimina una orden de venta");
        statusData.put("endpointsDisponibles", endpoints);

        int code = dbOk ? 200 : 503;
        CorsHelper.enviarJson(exchange, code, ApiResponse.ok(dbOk ? "API REST lista y conectada a MySQL" : "Error de conexión a MySQL", statusData));
    }
}
