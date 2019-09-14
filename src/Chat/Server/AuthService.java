package Chat.Server;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickLoginAndPass(String login, String pass) {
        String sql = String.format("SELECT nickname FROM users\n" +
                "WHERE login = '%s'\n" +
                "AND password = '%s'", login, pass);
        try {
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
