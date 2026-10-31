package gt.edu.umg.sistema.api.controller;

import gt.edu.umg.sistema.api.dao.ProductoDAO;
import gt.edu.umg.sistema.api.model.ApiResponse;
import gt.edu.umg.sistema.api.model.Producto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Producto>>> listar() {
        try {
            List<Producto> lista = productoDAO.listar();
            return ResponseEntity.ok(ApiResponse.ok("Catálogo de productos obtenido exitosamente", lista));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error consultando productos: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> buscarPorId(@PathVariable int id) {
        try {
            Producto p = productoDAO.buscarPorId(id);
            if (p != null) {
                return ResponseEntity.ok(ApiResponse.ok("Producto encontrado", p));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Producto con ID " + id + " no encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error consultando producto: " + e.getMessage()));
        }
    }
}
