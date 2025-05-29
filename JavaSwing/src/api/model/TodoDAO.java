package api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import api.DB;

public class TodoDAO {
	public int insertTodo(Todo todo) throws SQLException {
		if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
			todo.setTitle("Untitled");
		}
		LocalDate start = LocalDate.parse(todo.getStartDate());
		LocalDate end = LocalDate.parse(todo.getEndDate());
		if (start.isAfter(end)) {
			todo.setStartDate(end.toString());
			todo.setEndDate(start.toString());
		}
		String sql = "INSERT INTO todo (user_id, title, start_date, end_date, completed, seq, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, todo.getUserId());
			ps.setString(2, todo.getTitle());
			ps.setString(3, todo.getStartDate());
			ps.setString(4, todo.getEndDate());
			ps.setInt(5, todo.getCompleted());
			ps.setInt(6, todo.getSeq());
			ps.setString(7, todo.getCreatedAt());
			ps.setString(8, todo.getUpdatedAt());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
			}
		}
		return -1;
	}

	public boolean updateTodo(Todo todo) throws SQLException {
		if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
			todo.setTitle("Untitled");
		}
		LocalDate start = LocalDate.parse(todo.getStartDate());
		LocalDate end = LocalDate.parse(todo.getEndDate());
		if (start.isAfter(end)) {
			todo.setStartDate(end.toString());
			todo.setEndDate(start.toString());
		}
		String sql = "UPDATE todo SET title=?, start_date=?, end_date=?, completed=?, seq=?, updated_at=? WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, todo.getTitle());
			ps.setString(2, todo.getStartDate());
			ps.setString(3, todo.getEndDate());
			ps.setInt(4, todo.getCompleted());
			ps.setInt(5, todo.getSeq());
			ps.setString(6, todo.getUpdatedAt());
			ps.setInt(7, todo.getId());
			return ps.executeUpdate() > 0;
		}
	}

	public List<Todo> selectTodosByUser(int userId) throws SQLException {
		String sql = "SELECT * FROM todo WHERE user_id=? ORDER BY start_date, seq, id";
		List<Todo> result = new ArrayList<>();
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Todo todo = new Todo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"),
							rs.getString("start_date"), rs.getString("end_date"), rs.getInt("completed"),
							rs.getInt("seq"), rs.getString("created_at"), rs.getString("updated_at"));
					result.add(todo);
				}
			}
		}
		return result;
	}

	public List<Todo> listByDate(int userId, LocalDate date) throws SQLException {
		String sql = "SELECT * FROM todo WHERE user_id=? AND start_date<=? AND end_date>=? ORDER BY start_date, seq, id";
		List<Todo> result = new ArrayList<>();
		String dateStr = date.toString();
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setString(2, dateStr);
			ps.setString(3, dateStr);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Todo todo = new Todo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"),
							rs.getString("start_date"), rs.getString("end_date"), rs.getInt("completed"),
							rs.getInt("seq"), rs.getString("created_at"), rs.getString("updated_at"));
					result.add(todo);
				}
			}
		}
		return result;
	}

	public List<Todo> listAll(int userId) throws SQLException {
		String sql = "SELECT * FROM todo WHERE user_id=? ORDER BY start_date, seq, id";
		List<Todo> result = new ArrayList<>();
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Todo todo = new Todo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"),
							rs.getString("start_date"), rs.getString("end_date"), rs.getInt("completed"),
							rs.getInt("seq"), rs.getString("created_at"), rs.getString("updated_at"));
					result.add(todo);
				}
			}
		}
		return result;
	}

	public Todo selectTodoById(int todoId) throws SQLException {
		String sql = "SELECT * FROM todo WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, todoId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new Todo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"),
							rs.getString("start_date"), rs.getString("end_date"), rs.getInt("completed"),
							rs.getInt("seq"), rs.getString("created_at"), rs.getString("updated_at"));
				}
			}
		}
		return null;
	}

	public boolean deleteTodo(int todoId) throws SQLException {
		String sql = "DELETE FROM todo WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, todoId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean toggleCompleted(int todoId, boolean completed) throws SQLException {
		String sql = "UPDATE todo SET completed=? WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, completed ? 1 : 0);
			ps.setInt(2, todoId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean updateTodoSeq(int todoId, int seq) throws SQLException {
		String sql = "UPDATE todo SET seq=? WHERE id=?";
		try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, seq);
			ps.setInt(2, todoId);
			return ps.executeUpdate() > 0;
		}
	}
}
