package smart_lighting;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Statement;

import model.PatternModel;

public class SQLManager {

	java.sql.Connection conn = null;

	public SQLManager() {

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/smart_light?" + "user=root&password=");

			// Do something with the Connection

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public ArrayList<PatternModel> getLightPatterns() {

		ArrayList<PatternModel> array = new ArrayList<>();

		java.sql.Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"select * from pattern_range left join pattern_power on pattern_range.pattern_id = pattern_power.pattern_id order by pattern_power.power DESC");
			while (rs.next()) {
				array.add(new PatternModel(rs.getInt(3), rs.getString(2), rs.getFloat(5)));
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return array;
	}

}
