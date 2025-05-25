package api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import api.DB;

public class TodoDAO {

	public static List<Todo> listByDate(int userId, LocalDate date) {
		String sql = "SELECT * FROM todos WHERE user_id=? AND todo_date=? ORDER BY depth, seq";
		List<Todo> list = new ArrayList<>();
		try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
			p.setInt(1, userId);
			p.setString(2, date.toString());
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
		String sql = "INSERT INTO todos (user_id,parent_id,depth,title,note,todo_date,start_time,end_time,completed,seq) VALUES (?,?,?,?,?,?,?,?,?,?)";
		try (Connection c = DB.getConnection();
				PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			p.setInt(1, t.getUserId());
			if (t.getParentId() == null)
				p.setNull(2, Types.INTEGER);
			else
				p.setInt(2, t.getParentId());
			p.setInt(3, t.getDepth());
			p.setString(4, t.getTitle());
			p.setString(5, t.getNote());
			p.setString(6, t.getDate().toString());
			p.setString(7, t.getStartTime() == null ? null : t.getStartTime().toString());
			p.setString(8, t.getEndTime() == null ? null : t.getEndTime().toString());
			p.setInt(9, t.isCompleted() ? 1 : 0);
			p.setInt(10, nextSeq);
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

	public static void toggleCompleted(int id, boolean c) {
		exec("UPDATE todos SET completed=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", ps -> {
			ps.setInt(1, c ? 1 : 0);
			ps.setInt(2, id);
		});
	}

	public static void delete(int id) {
		exec("DELETE FROM todos WHERE id=? OR parent_id=?", ps -> {
			ps.setInt(1, id);
			ps.setInt(2, id);
		});
	}

	public static void update(Todo t) {
		String sql = "UPDATE todos SET title=?, note=?, todo_date=?, start_time=?, end_time=?, completed=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
		exec(sql, ps -> {
			ps.setString(1, t.getTitle());
			ps.setString(2, t.getNote());
			ps.setString(3, t.getDate().toString());
			ps.setString(4, t.getStartTime() == null ? null : t.getStartTime().toString());
			ps.setString(5, t.getEndTime() == null ? null : t.getEndTime().toString());
			ps.setInt(6, t.isCompleted() ? 1 : 0);
			ps.setInt(7, t.getId());
		});
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

	private static void exec(String sql, Prep f) {
		try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
			f.apply(p);
			p.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Todo map(ResultSet r) throws SQLException {
		return new Todo(r.getInt("id"), r.getInt("user_id"),
				r.getObject("parent_id") == null ? null : r.getInt("parent_id"), r.getInt("depth"),
				r.getString("title"), r.getString("note"), LocalDate.parse(r.getString("todo_date")),
				r.getString("start_time") == null ? null : LocalTime.parse(r.getString("start_time")),
				r.getString("end_time") == null ? null : LocalTime.parse(r.getString("end_time")),
				r.getInt("completed") == 1, r.getInt("seq"));
	}

	private interface Prep {
		void apply(PreparedStatement p) throws SQLException;
	}
}
