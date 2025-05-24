package api;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import api.model.Memo;

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
    
    public static List<Memo> loadMemo(int userId) {
        List<Memo> list = new ArrayList<>();
        String sql = "SELECT * FROM memo WHERE user_id = ? ORDER BY `index` ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Memo(
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
    public static int insertMemo(int userId, String content, int orderIndex) {
        String sql = "INSERT INTO memo (user_id, content, `index`) VALUES (?, ?, ?)";
        return executeUpdate(sql, new Object[]{userId, content, orderIndex});
    }
    
    public static int updateMemoContent(int memoId, String content) {
        String sql = "UPDATE memo SET content = ? WHERE id = ?";
        return executeUpdate(sql, new Object[]{content, memoId});
    }

    public static int deleteMemo(int memoId) {
        String sql = "DELETE FROM memo WHERE id = ?";
        return executeUpdate(sql, new Object[]{memoId});
    }

    // 메모 순서 변경
    public static int updateOrder(int todoId, int newIndex) {
        String sql = "UPDATE memo SET `index` = ? WHERE id = ?";
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
