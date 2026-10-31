package gt.edu.umg.sistema.api;

import com.sun.net.httpserver.HttpServer;
import gt.edu.umg.sistema.api.config.DatabaseConfig;
import gt.edu.umg.sistema.api.handler.PedidosHandler;
import gt.edu.umg.sistema.api.handler.ProductosHandler;
import gt.edu.umg.sistema.api.handler.StatusHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        } else {
            String portEnv = System.getenv("PORT");
            if (portEnv != null && !portEnv.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portEnv.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Registro de endpoints REST
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/productos", new ProductosHandler());
            server.createContext("/api/pedidos", new PedidosHandler());

            // Manejo concurrente con Pool de Hilos
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            boolean dbOk = DatabaseConfig.checkConnection();

            System.out.println("==================================================================");
            System.out.println("   UNIVERSIDAD MARIANO GÁLVEZ - PROGRAMACIÓN II");
            System.out.println("   SERVICIO API REST - SISTEMA DE VENTAS");
            System.out.println("==================================================================");
            System.out.println(" Servidor escuchando en: http://localhost:" + port);
            System.out.println(" Estado Base de Datos:   " + (dbOk ? "CONECTADA (MySQL en " + DatabaseConfig.getHost() + ")" : "ERROR DE CONEXIÓN"));
            System.out.println("------------------------------------------------------------------");
            System.out.println(" Endpoints disponibles:");
            System.out.println("  • GET    http://localhost:" + port + "/api/status");
            System.out.println("  • GET    http://localhost:" + port + "/api/productos");
            System.out.println("  • GET    http://localhost:" + port + "/api/pedidos");
            System.out.println("  • GET    http://localhost:" + port + "/api/pedidos/{id}");
            System.out.println("  • POST   http://localhost:" + port + "/api/pedidos        (Crear Orden)");
            System.out.println("  • PUT    http://localhost:" + port + "/api/pedidos/{id}   (Editar Orden)");
            System.out.println("  • DELETE http://localhost:" + port + "/api/pedidos/{id}   (Eliminar Orden)");
            System.out.println("------------------------------------------------------------------");
            System.out.println(" Para exponer a Internet con ngrok ejecute en otra terminal:");
            System.out.println("   ngrok http " + port);
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("Error fatal iniciando el servidor API REST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
