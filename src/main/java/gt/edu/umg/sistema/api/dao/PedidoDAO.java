package gt.edu.umg.sistema.api.dao;

import gt.edu.umg.sistema.api.config.DatabaseConfig;
import gt.edu.umg.sistema.api.model.CrearPedidoRequest;
import gt.edu.umg.sistema.api.model.DetallePedido;
import gt.edu.umg.sistema.api.model.ItemPedidoRequest;
import gt.edu.umg.sistema.api.model.Pedido;
import gt.edu.umg.sistema.api.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();

    public Pedido crear(CrearPedidoRequest req) throws SQLException, IllegalArgumentException {
        // Validaciones básicas de negocio
        if (req.getNombreCliente() == null || req.getNombreCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente o comprador es obligatorio.");
        }
        if (req.getDireccionEnvio() == null || req.getDireccionEnvio().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria.");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe incluir al menos un producto.");
        }

        Connection cn = DatabaseConfig.getConnection();
        boolean autoCommitOriginal = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false); // Transacción para garantizar atomicidad

            // 1. Obtener o registrar al Cliente
            int clienteId = obtenerOCrearCliente(cn, req.getNombreCliente().trim(), req.getTelefono());

            // 2. Registrar la Dirección de Entrega
            int direccionId = registrarDireccionEnvio(cn, clienteId, req.getDireccionEnvio().trim());

            // 3. Validar productos, verificar existencias y calcular total
            double totalCalculado = 0.0;
            List<DetallePedido> detallesPreGuardado = new ArrayList<>();

            for (ItemPedidoRequest item : req.getItems()) {
                if (item.getCantidad() <= 0) {
                    throw new IllegalArgumentException("La cantidad para cada producto debe ser mayor a 0.");
                }

                Producto p = productoDAO.buscarPorId(item.getProductoId());
                if (p == null) {
                    throw new IllegalArgumentException("El producto con ID " + item.getProductoId() + " no existe.");
                }

                if (p.getStock() < item.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto '" + p.getNombre()
                            + "'. Disponibles: " + p.getStock() + ", solicitados: " + item.getCantidad());
                }

                double subtotal = Math.round(item.getCantidad() * p.getPrecio() * 100.0) / 100.0;
                totalCalculado += subtotal;

                DetallePedido d = new DetallePedido();
                d.setProductoId(p.getId());
                d.setProductoNombre(p.getNombre());
                d.setCantidad(item.getCantidad());
                d.setPrecio(p.getPrecio());
                d.setSubtotal(subtotal);
                detallesPreGuardado.add(d);
            }

            totalCalculado = Math.round(totalCalculado * 100.0) / 100.0;

            // 4. Insertar Pedido
            String estadoInicial = (req.getEstado() != null && !req.getEstado().trim().isEmpty())
                    ? req.getEstado().trim().toUpperCase() : "PENDIENTE";

            int pedidoId = 0;
            String sqlPedido = "INSERT INTO pedido (cliente_id, direccion_envio_id, estado, total) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, clienteId);
                ps.setInt(2, direccionId);
                ps.setString(3, estadoInicial);
                ps.setDouble(4, totalCalculado);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedidoId = rs.getInt(1);
                    }
                }
            }

            if (pedidoId <= 0) {
                throw new SQLException("No se pudo obtener el ID autogenerado del pedido.");
            }

            // 5. Insertar DetallePedido y descontar stock
            String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlDescontarStock = "UPDATE producto SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement psDet = cn.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psStock = cn.prepareStatement(sqlDescontarStock)) {

                for (DetallePedido d : detallesPreGuardado) {
                    d.setPedidoId(pedidoId);
                    psDet.setInt(1, pedidoId);
                    psDet.setInt(2, d.getProductoId());
                    psDet.setInt(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecio());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.executeUpdate();

                    try (ResultSet rsDet = psDet.getGeneratedKeys()) {
                        if (rsDet.next()) {
                            d.setId(rsDet.getInt(1));
                        }
                    }

                    // Descontar inventario
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getProductoId());
                    psStock.executeUpdate();
                }
            }

            cn.commit(); // Confirmar transacción en la base de datos

            // 6. Retornar el Pedido completo
            Pedido pedidoCreado = new Pedido();
            pedidoCreado.setId(pedidoId);
            pedidoCreado.setClienteId(clienteId);
            pedidoCreado.setClienteNombre(req.getNombreCliente().trim());
            pedidoCreado.setDireccionEnvioId(direccionId);
            pedidoCreado.setDireccionTexto(req.getDireccionEnvio().trim());
            pedidoCreado.setTelefonoContacto(req.getTelefono());
            pedidoCreado.setEstado(estadoInicial);
            pedidoCreado.setTotal(totalCalculado);
            pedidoCreado.setDetalles(detallesPreGuardado);
            return pedidoCreado;

        } catch (Exception e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(autoCommitOriginal);
            cn.close();
        }
    }

    public List<Pedido> listar() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.cliente_id, COALESCE(c.nombre, 'Consumidor Final') AS cliente_nombre, "
                   + "p.direccion_envio_id, COALESCE(d.calle, 'Sin dirección registrada') AS direccion_texto, "
                   + "COALESCE(c.telefono, '') AS telefono_contacto, "
                   + "DATE_FORMAT(p.fecha, '%Y-%m-%d %H:%i:%s') AS fecha_str, p.estado, p.total "
                   + "FROM pedido p "
                   + "LEFT JOIN cliente c ON p.cliente_id = c.id "
                   + "LEFT JOIN direccion_envio d ON p.direccion_envio_id = d.id "
                   + "ORDER BY p.id DESC";

        try (Connection cn = DatabaseConfig.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id"));
                p.setClienteId(rs.getInt("cliente_id"));
                p.setClienteNombre(rs.getString("cliente_nombre"));
                p.setDireccionEnvioId(rs.getInt("direccion_envio_id"));
                p.setDireccionTexto(rs.getString("direccion_texto"));
                p.setTelefonoContacto(rs.getString("telefono_contacto"));
                p.setFecha(rs.getString("fecha_str"));
                p.setEstado(rs.getString("estado"));
                p.setTotal(rs.getDouble("total"));
                cargarDetalles(cn, p);
                lista.add(p);
            }
        }
        return lista;
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id, p.cliente_id, COALESCE(c.nombre, 'Consumidor Final') AS cliente_nombre, "
                   + "p.direccion_envio_id, COALESCE(d.calle, 'Sin dirección registrada') AS direccion_texto, "
                   + "COALESCE(c.telefono, '') AS telefono_contacto, "
                   + "DATE_FORMAT(p.fecha, '%Y-%m-%d %H:%i:%s') AS fecha_str, p.estado, p.total "
                   + "FROM pedido p "
                   + "LEFT JOIN cliente c ON p.cliente_id = c.id "
                   + "LEFT JOIN direccion_envio d ON p.direccion_envio_id = d.id "
                   + "WHERE p.id = ?";

        try (Connection cn = DatabaseConfig.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pedido p = new Pedido();
                    p.setId(rs.getInt("id"));
                    p.setClienteId(rs.getInt("cliente_id"));
                    p.setClienteNombre(rs.getString("cliente_nombre"));
                    p.setDireccionEnvioId(rs.getInt("direccion_envio_id"));
                    p.setDireccionTexto(rs.getString("direccion_texto"));
                    p.setTelefonoContacto(rs.getString("telefono_contacto"));
                    p.setFecha(rs.getString("fecha_str"));
                    p.setEstado(rs.getString("estado"));
                    p.setTotal(rs.getDouble("total"));
                    cargarDetalles(cn, p);
                    return p;
                }
            }
        }
        return null;
    }

    public Pedido actualizar(int id, CrearPedidoRequest req) throws SQLException, IllegalArgumentException {
        Pedido existente = buscarPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("No se encontró ninguna orden con el ID " + id);
        }

        Connection cn = DatabaseConfig.getConnection();
        boolean autoCommitOriginal = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);

            // Actualizar datos del cliente o dirección si aplican
            if (req.getNombreCliente() != null && !req.getNombreCliente().trim().isEmpty()) {
                try (PreparedStatement psCli = cn.prepareStatement("UPDATE cliente SET nombre = ?, telefono = ? WHERE id = ?")) {
                    psCli.setString(1, req.getNombreCliente().trim());
                    psCli.setString(2, req.getTelefono() != null ? req.getTelefono().trim() : "");
                    psCli.setInt(3, existente.getClienteId());
                    psCli.executeUpdate();
                }
            }

            if (req.getDireccionEnvio() != null && !req.getDireccionEnvio().trim().isEmpty() && existente.getDireccionEnvioId() != null) {
                try (PreparedStatement psDir = cn.prepareStatement("UPDATE direccion_envio SET calle = ? WHERE id = ?")) {
                    psDir.setString(1, req.getDireccionEnvio().trim());
                    psDir.setInt(2, existente.getDireccionEnvioId());
                    psDir.executeUpdate();
                }
            }

            // Si se envían nuevos ítems, devolver stock viejo, limpiar detalles y aplicar los nuevos
            double totalFinal = existente.getTotal();
            List<DetallePedido> nuevosDetalles = existente.getDetalles();

            if (req.getItems() != null && !req.getItems().isEmpty()) {
                // Devolver stock de detalles anteriores
                try (PreparedStatement psDev = cn.prepareStatement("UPDATE producto SET stock = stock + ? WHERE id = ?")) {
                    for (DetallePedido dViejo : existente.getDetalles()) {
                        psDev.setInt(1, dViejo.getCantidad());
                        psDev.setInt(2, dViejo.getProductoId());
                        psDev.executeUpdate();
                    }
                }

                // Borrar detalles anteriores
                try (PreparedStatement psDel = cn.prepareStatement("DELETE FROM detalle_pedido WHERE pedido_id = ?")) {
                    psDel.setInt(1, id);
                    psDel.executeUpdate();
                }

                // Validar y aplicar nuevos detalles
                totalFinal = 0.0;
                nuevosDetalles = new ArrayList<>();
                String sqlIns = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio, subtotal) VALUES (?, ?, ?, ?, ?)";
                String sqlDes = "UPDATE producto SET stock = stock - ? WHERE id = ?";

                try (PreparedStatement psIns = cn.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement psDes = cn.prepareStatement(sqlDes)) {

                    for (ItemPedidoRequest item : req.getItems()) {
                        Producto prod = productoDAO.buscarPorId(item.getProductoId());
                        if (prod == null) {
                            throw new IllegalArgumentException("Producto ID " + item.getProductoId() + " no encontrado.");
                        }
                        if (prod.getStock() < item.getCantidad()) {
                            throw new IllegalArgumentException("Stock insuficiente para: " + prod.getNombre());
                        }

                        double sub = Math.round(item.getCantidad() * prod.getPrecio() * 100.0) / 100.0;
                        totalFinal += sub;

                        psIns.setInt(1, id);
                        psIns.setInt(2, prod.getId());
                        psIns.setInt(3, item.getCantidad());
                        psIns.setDouble(4, prod.getPrecio());
                        psIns.setDouble(5, sub);
                        psIns.executeUpdate();

                        DetallePedido nuevoD = new DetallePedido();
                        nuevoD.setPedidoId(id);
                        nuevoD.setProductoId(prod.getId());
                        nuevoD.setProductoNombre(prod.getNombre());
                        nuevoD.setCantidad(item.getCantidad());
                        nuevoD.setPrecio(prod.getPrecio());
                        nuevoD.setSubtotal(sub);

                        try (ResultSet rsK = psIns.getGeneratedKeys()) {
                            if (rsK.next()) {
                                nuevoD.setId(rsK.getInt(1));
                            }
                        }
                        nuevosDetalles.add(nuevoD);

                        psDes.setInt(1, item.getCantidad());
                        psDes.setInt(2, prod.getId());
                        psDes.executeUpdate();
                    }
                }
            }

            totalFinal = Math.round(totalFinal * 100.0) / 100.0;
            String nuevoEstado = (req.getEstado() != null && !req.getEstado().trim().isEmpty())
                    ? req.getEstado().trim().toUpperCase() : existente.getEstado();

            try (PreparedStatement psPed = cn.prepareStatement("UPDATE pedido SET estado = ?, total = ? WHERE id = ?")) {
                psPed.setString(1, nuevoEstado);
                psPed.setDouble(2, totalFinal);
                psPed.setInt(3, id);
                psPed.executeUpdate();
            }

            cn.commit();
            return buscarPorId(id);

        } catch (Exception e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(autoCommitOriginal);
            cn.close();
        }
    }

    public boolean eliminar(int id) throws SQLException {
        Pedido p = buscarPorId(id);
        if (p == null) {
            return false;
        }

        try (Connection cn = DatabaseConfig.getConnection()) {
            // Devolver stock de los productos
            try (PreparedStatement psStock = cn.prepareStatement("UPDATE producto SET stock = stock + ? WHERE id = ?")) {
                for (DetallePedido d : p.getDetalles()) {
                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getProductoId());
                    psStock.executeUpdate();
                }
            }

            try (PreparedStatement psDelDet = cn.prepareStatement("DELETE FROM detalle_pedido WHERE pedido_id = ?")) {
                psDelDet.setInt(1, id);
                psDelDet.executeUpdate();
            }

            try (PreparedStatement psDel = cn.prepareStatement("DELETE FROM pedido WHERE id = ?")) {
                psDel.setInt(1, id);
                return psDel.executeUpdate() > 0;
            }
        }
    }

    private void cargarDetalles(Connection cn, Pedido p) {
        String sql = "SELECT dp.id, dp.pedido_id, dp.producto_id, COALESCE(pr.nombre, 'Producto Desconocido') AS producto_nombre, "
                   + "dp.cantidad, dp.precio, dp.subtotal "
                   + "FROM detalle_pedido dp "
                   + "LEFT JOIN producto pr ON dp.producto_id = pr.id "
                   + "WHERE dp.pedido_id = ? ORDER BY dp.id ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            try (ResultSet rs = ps.executeQuery()) {
                p.getDetalles().clear();
                while (rs.next()) {
                    DetallePedido det = new DetallePedido(
                            rs.getInt("id"),
                            rs.getInt("pedido_id"),
                            rs.getInt("producto_id"),
                            rs.getString("producto_nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio"),
                            rs.getDouble("subtotal")
                    );
                    p.getDetalles().add(det);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando detalles del pedido " + p.getId() + ": " + e.getMessage());
        }
    }

    private int obtenerOCrearCliente(Connection cn, String nombre, String telefono) throws SQLException {
        String sqlBuscar = "SELECT id FROM cliente WHERE LOWER(nombre) = LOWER(?) LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Crear nuevo cliente invitado
        String correoGenerado = nombre.toLowerCase().replaceAll("[^a-zA-Z0-9]", "") + "@movil.com";
        String sqlCrear = "INSERT INTO cliente (nombre, correo, telefono, estado) VALUES (?, ?, ?, 'ACTIVO')";
        try (PreparedStatement ps = cn.prepareStatement(sqlCrear, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, correoGenerado);
            ps.setString(3, telefono != null ? telefono.trim() : "");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1; // Fallback a Consumidor Final si fallara
    }

    private int registrarDireccionEnvio(Connection cn, int clienteId, String direccion) throws SQLException {
        String sql = "INSERT INTO direccion_envio (cliente_id, calle, ciudad, pais) VALUES (?, ?, 'Guatemala', 'Guatemala')";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clienteId);
            ps.setString(2, direccion);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }
}
