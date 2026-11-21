package ru.netology.data;

import java.sql.*;

public class DataHelper {
    private final Connection connection;

    public DataHelper() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");

        if (url == null || user == null || pass == null) {
            throw new IllegalStateException("DB env vars not set: DB_URL, DB_USER, DB_PASS");
        }
        this.connection = DriverManager.getConnection(url, user, pass);
    }

    public void clearTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            // Замени имена таблиц на те, что в schema.sql
            stmt.execute("TRUNCATE TABLE codes");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    public String getCodeForUser(String login) throws SQLException {
        // Подставь реальные имена таблицы и колонки
        String sql = "SELECT code FROM codes WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("code");
                }
            }
        }
        return null;
    }

    public boolean isUserBlocked(String login) throws SQLException {
        // Пример проверки блокировки: если есть флаг или счётчик
        // Подставь свою логику из схемы БД
        String sql = "SELECT locked FROM users WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Boolean locked = rs.getObject("locked", Boolean.class);
                    return locked != null && locked;
                }
            }
        }
        return false;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
