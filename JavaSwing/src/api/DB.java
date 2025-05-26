package api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import api.model.Memo;

public class DB {
	private static final String URL = "jdbc:sqlite:./javamodel.db";
	private static HikariDataSource ds;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(URL);
			config.setUsername("");
			config.setPassword("");
			config.setMaximumPoolSize(5);
			config.addDataSourceProperty("journal_mode", "WAL");
			config.addDataSourceProperty("synchronous", "NORMAL");
			ds = new HikariDataSource(config);
			initializeSchema();
		} catch (Exception e) {
			throw new RuntimeException("Database 초기화 실패", e);
		}
	}

	private static void initializeSchema() throws SQLException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

			/* ---------------- users ---------------- */
			stmt.execute("CREATE TABLE IF NOT EXISTS users (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " username TEXT UNIQUE NOT NULL," + " password TEXT NOT NULL" + ")");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");

			/* ---------------- memo ---------------- */
			stmt.execute("CREATE TABLE IF NOT EXISTS memo (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " user_id INTEGER," + " content TEXT," + " fix_flag BOOLEAN DEFAULT 0,"
					+ " order_index INTEGER DEFAULT 0," + " update_at TEXT," + " created_at TEXT,"
					+ " FOREIGN KEY(user_id) REFERENCES users(id)" + ")");
			/* ---------------- todos ---------------- */
			stmt.execute("CREATE TABLE IF NOT EXISTS todos (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " user_id INTEGER NOT NULL," + " parent_id INTEGER," + " depth INTEGER DEFAULT 0,"
					+ " title TEXT NOT NULL," + " note TEXT," + " todo_date TEXT NOT NULL," + " start_time TEXT,"
					+ " end_time TEXT," + " completed INTEGER DEFAULT 0," + " seq INTEGER NOT NULL DEFAULT 0,"
					+ " created_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " updated_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " FOREIGN KEY(user_id)  REFERENCES users(id)," + " FOREIGN KEY(parent_id) REFERENCES todos(id)"
					+ ")");

			stmt.execute("CREATE INDEX IF NOT EXISTS idx_todos_user_date ON todos(user_id, todo_date)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_todos_parent_seq ON todos(parent_id, seq)");

		}
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	public static boolean registerUser(String username, String plainPassword) {
		String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
		String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, hashed);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int login(String username, String plainPassword) {
		String sql = "SELECT id, password FROM users WHERE username = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String storedHash = rs.getString("password");
					if (BCrypt.checkpw(plainPassword, storedHash)) {
						return rs.getInt("id");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static List<Memo> loadMemo(int userId) {
		List<Memo> list = new ArrayList<>();
		String sql = "SELECT * FROM memo WHERE user_id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Memo m = new Memo(rs.getInt("id"), rs.getInt("user_id"), rs.getString("content"),
							rs.getBoolean("fix_flag"), rs.getInt("order_index"),
							LocalDateTime.parse(rs.getString("update_at")),
							LocalDateTime.parse(rs.getString("created_at")));
					list.add(m);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		list.sort((m1, m2) -> {
			if (m1.isFixFlag() && !m2.isFixFlag())
				return -1;
			if (!m1.isFixFlag() && m2.isFixFlag())
				return 1;
			if (m1.isFixFlag()) {
				return Integer.compare(m1.getOrderIndex(), m2.getOrderIndex());
			}
			return m2.getUpdateAt().compareTo(m1.getUpdateAt());
		});

		return list;
	}

	public static int insertMemo(int userId, String content, int orderIndex) {
		String now = LocalDateTime.now().toString();
		String sql = "INSERT INTO memo(user_id, content, fix_flag, order_index, update_at, created_at) "
				+ "VALUES(?, ?, 0, ?, ?, ?)";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, userId);
			ps.setString(2, content);
			ps.setInt(3, orderIndex);
			ps.setString(4, now);
			ps.setString(5, now);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static void setFixFlag(int memoId, boolean flag) {
		String now = LocalDateTime.now().toString();
		String sql = "UPDATE memo SET fix_flag = ?, update_at = ? WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, flag);
			ps.setString(2, now);
			ps.setInt(3, memoId);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateMemoContent(int memoId, String content) {
		String now = LocalDateTime.now().toString();
		String sql = "UPDATE memo SET content = ?, update_at = ? WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, content);
			ps.setString(2, now);
			ps.setInt(3, memoId);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void toggleFixFlag(int memoId, boolean fixFlag) {
		String sql = "UPDATE memo SET fix_flag = ?, update_at = ? WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, fixFlag);
			
			ps.setString(2, LocalDateTime.now().toString());
			ps.setInt(3, memoId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteMemo(int memoId) {
		String sql = "DELETE FROM memo WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, memoId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
