package gt.edu.umg.sistema.api.controller;

import gt.edu.umg.sistema.api.dao.PedidoDAO;
import gt.edu.umg.sistema.api.model.ApiResponse;
import gt.edu.umg.sistema.api.model.CrearPedidoRequest;
import gt.edu.umg.sistema.api.model.Pedido;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pedido>>> listar() {
        try {
            List<Pedido> lista = pedidoDAO.listar();
            return ResponseEntity.ok(ApiResponse.ok("Listado de órdenes obtenido exitosamente", lista));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener listado de órdenes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pedido>> buscarPorId(@PathVariable int id) {
        try {
            Pedido p = pedidoDAO.buscarPorId(id);
            if (p != null) {
                return ResponseEntity.ok(ApiResponse.ok("Orden encontrada", p));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Orden con ID " + id + " no encontrada"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar orden: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Pedido>> crear(@RequestBody CrearPedidoRequest req) {
        try {
            Pedido pedidoCreado = pedidoDAO.crear(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Orden de Venta creada exitosamente en la base de datos", pedidoCreado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al registrar orden: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Pedido>> actualizar(@PathVariable int id, @RequestBody CrearPedidoRequest req) {
        try {
            Pedido pedidoActualizado = pedidoDAO.actualizar(id, req);
            return ResponseEntity.ok(ApiResponse.ok("Orden de Venta #" + id + " actualizada exitosamente", pedidoActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al actualizar orden: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int id) {
        try {
            boolean eliminado = pedidoDAO.eliminar(id);
            if (eliminado) {
                return ResponseEntity.ok(ApiResponse.ok("Orden #" + id + " eliminada exitosamente y stock restituido", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Orden #" + id + " no encontrada"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al eliminar orden: " + e.getMessage()));
        }
    }
}
