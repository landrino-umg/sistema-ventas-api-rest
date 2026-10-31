package gt.edu.umg.sistema.api.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String DEFAULT_HOST = "159.54.148.6";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "sistema_ventas";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "4147";

    public static String getHost() {
        String env = System.getenv("DB_HOST");
        return (env != null && !env.trim().isEmpty()) ? env : DEFAULT_HOST;
    }

    public static String getPort() {
        String env = System.getenv("DB_PORT");
        return (env != null && !env.trim().isEmpty()) ? env : DEFAULT_PORT;
    }

    public static String getDatabase() {
        String env = System.getenv("DB_NAME");
        return (env != null && !env.trim().isEmpty()) ? env : DEFAULT_DB;
    }

    public static String getUser() {
        String env = System.getenv("DB_USER");
        return (env != null && !env.trim().isEmpty()) ? env : DEFAULT_USER;
    }

    public static String getPassword() {
        String env = System.getenv("DB_PASSWORD");
        return (env != null && !env.trim().isEmpty()) ? env : DEFAULT_PASS;
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado en classpath: " + e.getMessage());
        }
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    public static boolean checkConnection() {
        try (Connection cn = getConnection()) {
            return cn != null && !cn.isClosed();
        } catch (Exception e) {
            System.err.println("Error verificando conexión BD: " + e.getMessage());
            return false;
        }
    }
}
