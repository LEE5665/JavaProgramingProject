package api.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import api.DB;

public class CheckItemDAO {
	// 특정 todo에 해당하는 체크 리스트 모두 반환
    public static List<CheckItem> listByTodo(int todoId) {
        List<CheckItem> items = new ArrayList<>();
        String sql = "SELECT * FROM check_items WHERE todo_id=? ORDER BY seq";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, todoId);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                items.add(map(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    // 항목 추가
    public static void insert(CheckItem item) {
        String sql = "INSERT INTO check_items (todo_id, content, checked, type, seq) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, item.getTodoId());
            p.setString(2, item.getContent());
            p.setInt(3, item.isChecked() ? 1 : 0);
            p.setString(4, item.getType());
            p.setInt(5, item.getSeq());
            p.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 체크 항목 수정
    public static void toggleChecked(int id, boolean checked) {
        String sql = "UPDATE check_items SET checked=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, checked ? 1 : 0);
            p.setInt(2, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 항목 제거
    public static void delete(int id) {
        String sql = "DELETE FROM check_items WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 항목 수정
    public static void update(CheckItem item) {
        String sql = "UPDATE check_items SET content=?, checked=?, seq=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, item.getContent());
            p.setInt(2, item.isChecked() ? 1 : 0);
            p.setInt(3, item.getSeq());
            p.setInt(4, item.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // CheckItem 클래스로 변환
    private static CheckItem map(ResultSet r) throws SQLException {
        CheckItem item = new CheckItem();
        item.setId(r.getInt("id"));
        item.setTodoId(r.getInt("todo_id"));
        item.setContent(r.getString("content"));
        item.setChecked(r.getInt("checked") == 1);
        item.setType(r.getString("type"));
        item.setSeq(r.getInt("seq"));
        return item;
    }
}
