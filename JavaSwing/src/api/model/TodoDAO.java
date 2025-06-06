package api.model;

import api.DB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {

    // Todo 추가
    public static int insertTodo(Todo todo) throws SQLException {
        String sql = "INSERT INTO todo (user_id, title, start_date, end_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, todo.getUserId());
            ps.setString(2, todo.getTitle());
            ps.setString(3, todo.getStartDate());
            ps.setString(4, todo.getEndDate());
            ps.setString(5, todo.getCreatedAt());
            ps.setString(6, todo.getUpdatedAt());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // 특정 날짜에 포함된 일정 모두 조회
    public static List<Todo> listByDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM todo WHERE user_id=? AND start_date<=? AND end_date>=? ORDER BY start_date, id";
        List<Todo> result = new ArrayList<>();
        String dateStr = date.toString();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, dateStr);
            ps.setString(3, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Todo todo = new Todo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                    );
                    result.add(todo);
                }
            }
        }
        return result;
    }

    // 모든 Todo 반환
    public static List<Todo> listAll(int userId) throws SQLException {
        String sql = "SELECT * FROM todo WHERE user_id=? ORDER BY start_date, id";
        List<Todo> result = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Todo todo = new Todo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                    );
                    result.add(todo);
                }
            }
        }
        return result;
    }

    // Todo 수정
    public static boolean updateTodo(Todo todo) throws SQLException {
        String sql = "UPDATE todo SET title=?, start_date=?, end_date=?, updated_at=? WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todo.getTitle());
            ps.setString(2, todo.getStartDate());
            ps.setString(3, todo.getEndDate());
            ps.setString(4, todo.getUpdatedAt());
            ps.setInt(5, todo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Todo 삭제
    public static boolean deleteTodo(int todoId) throws SQLException {
        String sql = "DELETE FROM todo WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, todoId);
            return ps.executeUpdate() > 0;
        }
    }
}
