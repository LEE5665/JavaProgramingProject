package api;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DB {
	private static final String URL = "jdbc:sqlite:./javamodel.db";
	private static HikariDataSource ds;

	static { // 첫 호출 시 실행
		try {
			Class.forName("org.sqlite.JDBC"); // DB 연결 & 커넥터 폴 생성
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

	// DB 없을 시 생성
	private static void initializeSchema() throws SQLException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

			stmt.execute("CREATE TABLE IF NOT EXISTS users (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " username TEXT UNIQUE NOT NULL," + " password TEXT NOT NULL" + ")");

			stmt.execute("CREATE TABLE IF NOT EXISTS memo (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				    + " user_id INTEGER NOT NULL," + " content TEXT," + " fix_flag BOOLEAN DEFAULT 0,"
				    + " update_at TEXT DEFAULT (datetime('now','localtime')),"
				    + " created_at TEXT DEFAULT (datetime('now','localtime')),"
				    + " FOREIGN KEY(user_id) REFERENCES users(id)" + ")");

			stmt.execute("CREATE TABLE IF NOT EXISTS todo (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " user_id INTEGER NOT NULL," + " title TEXT NOT NULL," + " start_date TEXT NOT NULL,"
					+ " end_date TEXT NOT NULL," + " created_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " updated_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " FOREIGN KEY(user_id) REFERENCES users(id)" + ")");

			stmt.execute("CREATE TABLE IF NOT EXISTS check_items (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " todo_id INTEGER NOT NULL," + " content TEXT NOT NULL," + " checked INTEGER DEFAULT 0,"
					+ " type TEXT NOT NULL DEFAULT 'checkbox'," + " seq INTEGER NOT NULL DEFAULT 0,"
					+ " created_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " updated_at TEXT DEFAULT (datetime('now','localtime')),"
					+ " FOREIGN KEY(todo_id) REFERENCES todo(id)" + ")");

			stmt.execute("CREATE INDEX IF NOT EXISTS idx_todo_user_date ON todo(user_id, start_date, end_date)");
		}
	}

	// DB 커넥터 폴 반환
	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
}
