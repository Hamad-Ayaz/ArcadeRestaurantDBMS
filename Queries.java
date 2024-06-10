/* Authors: Josh Samadder and Hamad Ayaz
*  Class: CSC 460
*  Professor: Lester McCann
*  Purpose: Executes all of the queries required for our database to be able to execute. This includes getting all the games in the arcade and those who have the highest
	    scores on each, getting the names and membership information of those who spent over $100 in the past month, among other queries

   Requirements: A valid Oracle connection is required with permissions to select on the required tables
*/

import java.sql.*;

public class Queries {

	/* Name: query1
	 *  Purpose: Executes the first query, which gets all the games in the arcade and those who have the highest score on each
	 *  Preconditions: A valid Oracle connection is created and the tables are set up as expected
	 *  Postconditions: The query is executed successfully
	 *  Parameters: dbconn - The connection to Oracle.
	 */

	public static void query1(Connection dbconn) {
		try (Statement stmt = dbconn.createStatement()) {
			String query = "SELECT g.name AS GameName, gp.score AS HighScore, m.name AS MemberName " +
					"FROM hamadayaz.Game g " +
					"LEFT JOIN (" +
					"    SELECT gp.GID, MAX(gp.score) AS score " +
					"    FROM hamadayaz.GamePlay gp " +
					"    GROUP BY gp.GID" +
					") max_scores ON g.GID = max_scores.GID " +
					"LEFT JOIN hamadayaz.GamePlay gp ON g.GID = gp.GID AND gp.score = max_scores.score " +
					"LEFT JOIN hamadayaz.Member m ON gp.MID = m.MID " +
					"ORDER BY g.GID"; // Query to get the games and the highest scores on each of them
			ResultSet ans = stmt.executeQuery(query); // Get the results of our query

			// Print out the labels of what we are going to print out to the user
			System.out.println("High Scores:\n");
			System.out.printf("%-30s %-30s %-30s\n", "Game Name", "Member Name", "Score");
			System.out.println("-".repeat(90));
			while (ans.next()) {
				// Keep printing the game name, the member who had the high score (if there is one), and what the score is
				// as long as there is still tuples in our result
				String gameName = ans.getString("GameName");
				String memberName = ans.getString("MemberName");
				int score = ans.getInt("HighScore");
				if (ans.wasNull()) {
					System.out.printf("%-30s %-30s %-30s\n", gameName, "No high score", "N/A");
				} else {
					System.out.printf("%-30s %-30s %-30d\n", gameName, memberName, score);
				}
			}
			System.out.println();
		} catch (SQLException e) { // Handle SQL Exceptions if they ever occur
			handleSQLException(e);
		}
	}

	/* Name: query2
	 *  Purpose: Executes the second query, which gets all members and their membership information if they have spent at least $100 in the past month
	 *  Preconditions: A valid Oracle connection is created and the tables are set up as expected
	 *  Postconditions: The query is executed successfully
	 *  Parameters: dbconn - The connection to Oracle
	 */

	public static void query2(Connection dbconn) {
		try {
			String query = "SELECT m.name, m.tier, SUM(tp.amountSpent) AS TotalSpent " +
					"FROM hamadayaz.Member m " +
					"JOIN hamadayaz.TokenPurchase tp ON m.MID = tp.MID " +
					"WHERE tp.purchaseDate >= ADD_MONTHS(CURRENT_DATE, -1) " +
					"GROUP BY m.name, m.tier " +
					"HAVING SUM(tp.amountSpent) >= 100"; // The query to get member names and tiers based on how much they spent in the past month
			PreparedStatement stmt = dbconn.prepareStatement(query);
			ResultSet ans = stmt.executeQuery();

			System.out.println("-------- List of members who spent $100 or more on tokens in the past month --------\n");
			System.out.printf("%-30s %-30s %-30s\n", "Name", "Membership Tier", "Total Spent");
			System.out.println("-".repeat(90));
			while (ans.next()) {
				// Print out the names, tiers, and amount spent as long as there is a result in our relation
				String name = ans.getString("name");
				String tier = ans.getString("tier");
				double totalSpent = ans.getDouble("TotalSpent");
				System.out.printf("%-30s %-30s $%-28.2f\n", name, tier, totalSpent);
			}
		} catch (SQLException e) { // Catch any SQL errors that may have occured along the way
			handleSQLException(e);
		}
		System.out.println();
	}

	/* Name: query3
	*  Purpose: Executes the third query, which gets descriptions of the prizes a member can get with the amount of tickets that they have
	*  Preconditions: A valid Oracle connection is created and the tables are set up as expected
        *  Postconditions: The query is executed successfully
        *  Parameters: MID - A string that is the member's ID
		       dbconn - The connection to Oracle
	*/

	public static void query3(String MID, Connection dbconn) {
		try {
			String ticketsQuery = "SELECT totalTickets FROM hamadayaz.Member WHERE MID = ?"; // Initial query that gets us the number of tickets the member has
			PreparedStatement stmt1 = dbconn.prepareStatement(ticketsQuery);
			stmt1.setString(1, MID);
			ResultSet ans1 = stmt1.executeQuery();

			if (ans1.next()) {
				int ticketNo = ans1.getInt("totalTickets"); // Grab the number of tickets they have as an integer
				String prizesQuery = "SELECT description FROM hamadayaz.Prize WHERE ticketCost <= ?"; // Get the prizes that the member can get with the amount of tickets they have
				PreparedStatement finalStmt = dbconn.prepareStatement(prizesQuery);
				finalStmt.setInt(1, ticketNo);
				ResultSet finalResult = finalStmt.executeQuery();

				// Print out what prizes they can get
				if (finalResult.next()) {
					System.out.println("-------- Items this member can purchase with their tickets --------\n");
					do {
						String val = finalResult.getString("description");
						System.out.println(val);
					} while (finalResult.next());
				} else {
					System.out.println("The member does not have enough tickets to purchase any prizes."); // If there is no result, then the member cannot get any prizes with the amount of tickets they have
				}
			} else {
				System.out.println("Could not find member with the given member ID: " + MID); // If the initial query does not have a result, then the member could not be found
			}
			System.out.println();
		} catch (SQLException e) { // Handle any SQL exceptions that might have happened along the way
			handleSQLException(e);
		}
	}


	/* Name: query4
	*  Purpose: Executes the fourth query, which gets the game the given member has the highest score in
	*  Preconditions: A valid Oracle connection is created and the tables are set up as expected
        *  Postconditions: The query is executed successfully
        *  Parameters: MID - A string that is the member's ID
		       dbconn - The connection to Oracle
	*/

	public static void query4(String MID, Connection dbconn) {
		try {
			String query = "SELECT g.Name AS GameName, gp.score FROM hamadayaz.GamePlay gp " +
					"JOIN hamadayaz.Game g ON gp.GID = g.GID " +
					"WHERE gp.MID = ? ORDER BY gp.score DESC"; // Get the name of the game they have the highest score in
			PreparedStatement stmt = dbconn.prepareStatement(query);
			stmt.setString(1, MID);
			ResultSet ans = stmt.executeQuery();

			if (ans.next()) {
				String gameName = ans.getString("GameName");
				int maxScore = ans.getInt("score");

				String memberQuery = "SELECT name FROM hamadayaz.Member WHERE MID = ?"; // Query to get the name of the member to print out
				PreparedStatement memStmt = dbconn.prepareStatement(memberQuery);
				memStmt.setString(1, MID);
				ResultSet memName = memStmt.executeQuery();
				if (memName.next()) {
					String memberName = memName.getString("name");
					System.out.println("Member " + memberName + " has the highest score on " + gameName + " with a score of " + maxScore);
				}
			} else {
				System.out.println("Member has not played any games or does not exist!"); // Getting no result on the first query means that the member either does not exist or has not played any games
			}

			System.out.println();
		} catch (SQLException e) { // Handle any SQL Exception
			handleSQLException(e);
		}
	}

	/* Name: handleSQLException
	 *  Purpose: Prints out error messages in case of an SQL exception when executing queries
	 *  Preconditions: An SQL exception has been thrown when using one of the queries
	 *  Postconditions: Messages are printed to stderr and the application is exited
	 *  Parameters: e - The SQLException to be handled
	 */
	private static void handleSQLException(SQLException e) {
		System.err.println("*** SQLException:  " +
				"Could not fetch query results.");
		System.err.println("\tMessage:   " + e.getMessage());
		System.err.println("\tSQLState:  " + e.getSQLState());
		System.err.println("\tErrorCode: " + e.getErrorCode());
		System.exit(-1);
	}
}
