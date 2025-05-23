package api;

import java.sql.*;

public class DB {
	
	static final String URL = "jdbc:mysql://localhost:3306/javamodel";
    static final String USER = "root";
    static final String PASSWORD = "123456";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static int executeUpdate(String sql, Object[] params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQL 실행 실패: " + e.getMessage());
            return -1;
        }
    }
}
