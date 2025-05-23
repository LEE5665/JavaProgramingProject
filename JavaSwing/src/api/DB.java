package api;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import api.model.Todos;

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
    
    public static List<Todos> loadTodos(int userId) {
        List<Todos> list = new ArrayList<>();
        String sql = "SELECT * FROM todos WHERE user_id = ? ORDER BY `index` ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Todos(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("content"),
                    rs.getInt("index"),
                    rs.getDate("created_at").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 메모 추가
    public static int insertTodo(int userId, String content, int orderIndex) {
        String sql = "INSERT INTO todos (user_id, content, `index`) VALUES (?, ?, ?)";
        return executeUpdate(sql, new Object[]{userId, content, orderIndex});
    }

    // 메모 순서 변경
    public static int updateOrder(int todoId, int newIndex) {
        String sql = "UPDATE todos SET `index` = ? WHERE id = ?";
        return executeUpdate(sql, new Object[]{newIndex, todoId});
    }
    
    public static int login(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
