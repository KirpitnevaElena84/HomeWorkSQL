package ru.netology;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import ru.netology.data.DataHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLHelper {

    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private SQLHelper() {
    }

    private static Connection getConn() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("db.url", "jdbc:mysql://localhost:3306/app"),
                System.getProperty("db.user", "app"),
                System.getProperty("db.password", "pass"));
    }

    @SneakyThrows(SQLException.class)
    public static DataHelper.VerificationCode getVerificationCode() {
        var sql = """
                SELECT ac.code
                FROM auth_codes ac
                JOIN users u ON ac.user_id = u.id
                WHERE u.login = ?
                ORDER BY ac.created DESC, ac.id DESC
                LIMIT 1
                """;
        try (var conn = getConn()) {
            var login = DataHelper.getAuthInfoWithTestData().getLogin();
            String code = QUERY_RUNNER.query(conn, sql, new ScalarHandler<>(), login);
            return new DataHelper.VerificationCode(code);
        }
    }

    @SneakyThrows(SQLException.class)
    public static String getUserStatus(String login) {
        var sql = "SELECT status FROM users WHERE login = ?";
        try (var conn = getConn()) {
            return QUERY_RUNNER.query(conn, sql, new ScalarHandler<>(), login);
        }
    }

    @SneakyThrows(SQLException.class)
    public static void cleanDatabase() {
        try (var conn = getConn()) {
            QUERY_RUNNER.execute(conn, "DELETE FROM auth_codes");
            QUERY_RUNNER.execute(conn, "DELETE FROM card_transactions");
            QUERY_RUNNER.execute(conn, "DELETE FROM cards");
            QUERY_RUNNER.execute(conn, "DELETE FROM users");
        }
    }

    @SneakyThrows(SQLException.class)
    public static void cleanAuthCodes() {
        try (var conn = getConn()) {
            QUERY_RUNNER.execute(conn, "DELETE FROM auth_codes");
        }
    }
}
