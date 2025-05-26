package api.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import api.DB;

public class TodoDAO {
    public static List<Todo> listAll(int userId) {
        String sql = "SELECT * FROM todos WHERE user_id=? ORDER BY start_date, end_date, seq";
        List<Todo> list = new ArrayList<>();
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    list.add(map(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Todo> listByDate(int userId, LocalDate date) {
        List<Todo> list = new ArrayList<>();
        String sql = "SELECT * FROM todos WHERE user_id=? AND start_date<=? AND end_date>=? ORDER BY start_date, end_date, id";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, date.toString());
            p.setString(3, date.toString());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<LocalDate> listAllDates(int userId) {
        List<LocalDate> dateList = new ArrayList<>();
        String sql = "SELECT DISTINCT start_date FROM todos WHERE user_id=? ORDER BY start_date ASC";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) dateList.add(LocalDate.parse(r.getString("start_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dateList;
    }

    public static List<Todo> listByPeriod(int userId, LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM todos WHERE user_id=? AND start_date >= ? AND end_date <= ? ORDER BY start_date, seq";
        List<Todo> list = new ArrayList<>();
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, from.toString());
            p.setString(3, to.toString());
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    list.add(map(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Todo insert(Todo t) {
        int nextSeq = nextSeq(t.getParentId());
        String sql = "INSERT INTO todos (user_id, parent_id, depth, title, start_date, end_date, completed, seq) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, t.getUserId());
            if (t.getParentId() == null)
                p.setNull(2, Types.INTEGER);
            else
                p.setInt(2, t.getParentId());
            p.setInt(3, t.getDepth());
            p.setString(4, t.getTitle());
            p.setString(5, t.getStartDate().toString());
            p.setString(6, t.getEndDate().toString());
            p.setInt(7, t.isCompleted() ? 1 : 0);
            p.setInt(8, nextSeq);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next())
                    t.setId(r.getInt(1));
            }
            t.setSeq(nextSeq);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static void update(Todo t) {
        String sql = "UPDATE todos SET title=?, start_date=?, end_date=?, completed=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, t.getTitle());
            p.setString(2, t.getStartDate().toString());
            p.setString(3, t.getEndDate().toString());
            p.setInt(4, t.isCompleted() ? 1 : 0);
            p.setInt(5, t.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void toggleCompleted(int id, boolean c) {
        String sql = "UPDATE todos SET completed=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(int id) {
        String sql = "DELETE FROM todos WHERE id=? OR parent_id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.setInt(2, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void move(int id, Integer parentId, int depth, int newSeq) {
        shiftSeq(parentId, newSeq);
        String sql = "UPDATE todos SET parent_id=?, depth=?, seq=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (parentId == null)
                p.setNull(1, Types.INTEGER);
            else
                p.setInt(1, parentId);
            p.setInt(2, depth);
            p.setInt(3, newSeq);
            p.setInt(4, id);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void shiftSeq(Integer parentId, int fromSeq) {
        String sql;
        if (parentId == null) {
            sql = "UPDATE todos SET seq = seq + 1 WHERE parent_id IS NULL AND seq >= ?";
        } else {
            sql = "UPDATE todos SET seq = seq + 1 WHERE parent_id = ? AND seq >= ?";
        }
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (parentId == null) {
                p.setInt(1, fromSeq);
            } else {
                p.setInt(1, parentId);
                p.setInt(2, fromSeq);
            }
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int nextSeq(Integer parentId) {
        String sql = parentId == null ? "SELECT COALESCE(MAX(seq),0)+1 FROM todos WHERE parent_id IS NULL"
                : "SELECT COALESCE(MAX(seq),0)+1 FROM todos WHERE parent_id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (parentId != null)
                p.setInt(1, parentId);
            try (ResultSet r = p.executeQuery()) {
                if (r.next())
                    return r.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static List<Todo> listByEndDate(int userId, LocalDate date) {
        List<Todo> list = new ArrayList<>();
        String sql = "SELECT * FROM todos WHERE user_id = ? AND start_date <= ? AND end_date >= ? ORDER BY end_date, seq";
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, date.toString());
            p.setString(3, date.toString());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    public static List<LocalDate> listAllEndDates(int userId) {
        List<LocalDate> dateList = new ArrayList<>();
        String sql = "SELECT DISTINCT end_date FROM todos WHERE user_id=? ORDER BY end_date ASC";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    dateList.add(LocalDate.parse(r.getString("end_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dateList;
    }

    private static Todo map(ResultSet r) throws SQLException {
        return new Todo(
            r.getInt("id"),
            r.getInt("user_id"),
            r.getObject("parent_id") == null ? null : r.getInt("parent_id"),
            r.getInt("depth"),
            r.getString("title"),
            
            LocalDate.parse(r.getString("start_date")),
            LocalDate.parse(r.getString("end_date")),
            r.getInt("completed") == 1,
            r.getInt("seq")
        );
    }
    
}
