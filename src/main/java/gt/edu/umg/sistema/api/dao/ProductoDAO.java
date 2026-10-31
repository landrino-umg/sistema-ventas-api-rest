package gt.edu.umg.sistema.api.dao;

import gt.edu.umg.sistema.api.config.DatabaseConfig;
import gt.edu.umg.sistema.api.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.categoria_id, COALESCE(c.nombre, 'Sin Categoría') AS categoria_nombre, "
                   + "p.nombre, p.descripcion, p.precio, p.stock, p.sku "
                   + "FROM producto p "
                   + "LEFT JOIN categoria c ON p.categoria_id = c.id "
                   + "ORDER BY p.nombre ASC";

        try (Connection cn = DatabaseConfig.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto prod = new Producto(
                        rs.getInt("id"),
                        rs.getInt("categoria_id"),
                        rs.getString("categoria_nombre"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getString("sku")
                );
                lista.add(prod);
            }
        }
        return lista;
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id, p.categoria_id, COALESCE(c.nombre, 'Sin Categoría') AS categoria_nombre, "
                   + "p.nombre, p.descripcion, p.precio, p.stock, p.sku "
                   + "FROM producto p "
                   + "LEFT JOIN categoria c ON p.categoria_id = c.id "
                   + "WHERE p.id = ?";

        try (Connection cn = DatabaseConfig.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("id"),
                            rs.getInt("categoria_id"),
                            rs.getString("categoria_nombre"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("sku")
                    );
                }
            }
        }
        return null;
    }
}
