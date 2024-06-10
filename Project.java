/* Author: Hamad Ayaz
 * Course: CS460
 * Assg_Name: Final Project
 * Assg_DueDate: 4/1/2024 12:30PM
 * TA: Priyansh Nayak, Jake Bode & Ahmad Musa
 * Instructor: Lester I. McCann
 * Description: Project.java manages a database for arcade game members, games, and prizes.
 * It provides functionality for both admins and customers to interact with the database,
 * including operations such as adding, updating, deleting, and querying data. The program
 * focuses on ensuring data integrity, secure database transactions, and user-friendly
 * interactions through a console-based menu system. The techniques used include database
 * connectivity, SQL operations and error handling.
 *
 * Language: Java, version 16
 * Compilation: run javac Queries.java first, then run compiled file with command line, example:java Project
 * */


import java.sql.*;
import java.util.Scanner;

/**
 * Class Name: Project
 * Author: [Your Name]
 * External Packages: java.sql, java.util
 * Containing Package: Default package.
 * Inheritance Information: No Inheritance.
 *
 * Description: The Project class serves as the main entry point for a database management
 * system tailored for arcade game centers. It facilitates interaction with a database to manage members,
 * games, and prizes through a console-based interface. Admins can add, update, or delete members, games,
 * and prizes, while customers can update their profiles, purchase tokens, and redeem prizes. The application
 * ensures robust database connectivity and transaction management, emphasizing secure and efficient operations.
 *
 * Constructor: This class utilizes Java's default constructor as it does not explicitly define one.
 *
 * Instance Methods:
 * main(String[] args): Initializes the application, manages database connection, and facilitates user interaction through a menu-driven interface.
 * adminOperations(Scanner scanner): Handles administrative tasks such as member and game management.
 * customerOperations(Scanner scanner): Manages customer interactions for updating profiles, buying tokens, and redeeming prizes.
 * addMember(Scanner scanner): Adds a new member to the database after checking for existing entries.
 * updateMember(Scanner scanner): Updates an existing member's information in the database.
 * deleteMember(Scanner scanner): Removes a member from the database and handles related records.
 * manageTicketRedemption(Scanner scanner, String MID, int totalTickets): Manages the redemption of prizes for members based on their accumulated tickets.
 * listAndRedeemPrizes(Scanner scanner, String MID, int totalTickets): Lists available prizes and manages prize redemption processes.
 * redeemSelectedPrize(String MID, String prizeId): Executes the redemption of a selected prize for a member.
 * updateMemberTicketCount(String MID): Updates the ticket count for a member post-redemption.
 * proceedToDeleteMember(String MID): Finalizes the deletion of a member, ensuring all related records are also removed.
 * deleteRelatedRecords(String MID): Deletes all records related to a member from dependent tables.
 * addGame(Scanner scanner): Adds a new game to the database.
 * deleteGame(Scanner scanner): Removes a game from the database along with any associated gameplay records.
 * redeemPrizes(Scanner scanner): Handles the redemption of prizes by members.
 * buyTokens(Scanner scanner): Manages the purchase of tokens by members, updating their spent total and adjusting membership tiers if necessary.
 * addOrUpdatePrize(Scanner scanner): Adds a new prize or updates an existing one in the prize catalog.
 * deletePrize(Scanner scanner): Removes a prize from the system, including all related redemption records.
 * runQueries(Scanner scanner): Provides a menu-driven interface for running various predefined queries about games, members, and prizes.
 */


public class Project {

    private static final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle"; // connection
    private static Connection dbconn;

    /**
     * Method Name: main
     * Purpose: Serves as the entry point for the application. It handles initial user authentication,
     *          connects to the database, and navigates through the main menu.
     * Pre-conditions: Requires valid database credentials and network access to the database server.
     * Post-conditions: Depending on user choices, various functionalities of the application are executed.
     * Parameters:
     * - args (in): Command-line arguments. not in use.
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your Oracle DBMS username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your Oracle password: ");
            String password = scanner.nextLine();

            Class.forName("oracle.jdbc.OracleDriver");
            dbconn = DriverManager.getConnection(oracleURL, username, password);
            dbconn.setAutoCommit(false);

            System.out.println("Connected to the database successfully.");

            boolean exitProgram = false;
            while (!exitProgram) {
                System.out.println("\nChoose your role:");
                System.out.println("1. Database Admin");
                System.out.println("2. Customer");
                System.out.println("3. Queries");
                System.out.println("4. Exit Program");
                System.out.print("Enter your choice: ");
                int role = Integer.parseInt(scanner.nextLine());

                switch (role) {
                    case 1:
                        adminOperations(scanner);
                        break;
                    case 2:
                        customerOperations(scanner);
                        break;
                    case 3:
                        runQueries(scanner);
                        break;
                    case 4:
                        exitProgram = true;
                        break;
                    default:
                        System.out.println("Invalid role selected. Please try again.");
                }
            }

            dbconn.close();
            System.out.println("Exiting program.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method Name: adminOperations
     * Purpose: Provides an administrative interface to manage members, games, and prizes.
     * Pre-conditions: User must have administrative privileges.
     * Post-conditions: Executes database operations based on user choices.
     * Parameters:
     * - scanner (in): Scanner object for reading user input.
     */
    private static void adminOperations(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\n0. <-- to Main Menu"); // menu
            System.out.println("Member Operations:");
            System.out.println("1. Add Member");
            System.out.println("2. Update Member");
            System.out.println("3. Delete Member");
            System.out.println("Game Operations:");
            System.out.println("4. Add Game");
            System.out.println("5. Delete Game");
            System.out.println("Prize Operations:");
            System.out.println("6. Add Prize");
            System.out.println("7. Delete Prize");

            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            // options
            switch (choice) {
                case 0:
                    return;
                case 1:
                    addMember(scanner);
                    break;
                case 2:
                    updateMember(scanner);
                    break;
                case 3:
                    deleteMember(scanner);
                    break;
                case 4:
                    addGame(scanner);
                    break;
                case 5:
                    deleteGame(scanner);
                    break;
                case 6:
                    addOrUpdatePrize(scanner);
                    break;
                case 7:
                    deletePrize(scanner);
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    /**
     * Method Name: customerOperations
     * Purpose: Manages operations that a customer can perform, such as updating account information
     *          and redeeming prizes.
     * Pre-conditions: The user must be logged in as a customer.
     * Post-conditions: Performs various customer-specific database operations.
     * Parameters:
     * - scanner (in): Scanner object for reading user input.
     */
    private static void customerOperations(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\n0. <-- to Main Menu"); // menu
            System.out.println("Member Operations:");
            System.out.println("1. Update Your Account Info");
            System.out.println("2. Delete Your Account");
            System.out.println("3. Redeem Prizes");
            System.out.println("4. Buy Tokens");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            // options
            switch (choice) {
                case 0:
                    return;
                case 1:
                    updateMember(scanner);
                    break;
                case 2:
                    deleteMember(scanner);
                    break;
                case 3:
                    redeemPrizes(scanner);
                    break;
                case 4:
                    buyTokens(scanner);
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    /**
     * Method Name: addMember
     * Purpose: Adds a new member to the database after verifying that the member does not already exist.
     * Pre-conditions: Member ID provided must not already exist in the database.
     * Post-conditions: A new member record is added to the database.
     * Parameters:
     * - scanner (in): Scanner object for capturing member data from user input.
     */
    private static void addMember(Scanner scanner) throws SQLException {
        System.out.print("Enter Member ID: ");
        String mid = scanner.nextLine();

        // Check if member already exists
        String checkSql = "SELECT MID FROM hamadayaz.Member WHERE MID = ?";
        try (PreparedStatement checkStmt = dbconn.prepareStatement(checkSql)) {
            checkStmt.setString(1, mid);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("A member with ID " + mid + " already exists.");
                return;  // Exit the method if the member exists
            }
        }

        // Continue with adding the member
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();
        System.out.print("Enter Member Tier: ");
        String tier = scanner.nextLine();
        System.out.print("Enter Total Spent ($): ");
        double totalSpent = scanner.nextDouble();
        System.out.print("Enter Total Tickets: ");
        int totalTickets = scanner.nextInt();
        scanner.nextLine();

        // insert member
        String sql = "INSERT INTO hamadayaz.Member (MID, name, phoneNo, address, tier, totalSpent, totalTickets) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbconn.prepareStatement(sql)) {
            pstmt.setString(1, mid);
            pstmt.setString(2, name);
            pstmt.setString(3, phoneNo);
            pstmt.setString(4, address);
            pstmt.setString(5, tier);
            pstmt.setDouble(6, totalSpent);
            pstmt.setInt(7, totalTickets);
            int count = pstmt.executeUpdate();
            dbconn.commit();
            System.out.println(count + " member(s) added.");
        }
    }

    /**
     * Method Name: updateMember
     * Purpose: Updates existing member information in the database.
     * Pre-conditions: The member ID must exist in the database.
     * Post-conditions: Updates the member record with new data provided by the user.
     * Parameters:
     * - scanner (in): Scanner object for capturing new member data from user input.
     */
    private static void updateMember(Scanner scanner) throws SQLException {
        System.out.print("Enter Member ID to update: ");
        String MID = scanner.nextLine();

        // Check if member exists
        String existCheckSql = "SELECT MID FROM hamadayaz.Member WHERE MID = ?";
        try (PreparedStatement existCheckStmt = dbconn.prepareStatement(existCheckSql)) {
            existCheckStmt.setString(1, MID);
            ResultSet existRs = existCheckStmt.executeQuery();
            if (!existRs.next()) {
                System.out.println("No member found with ID: " + MID);
                return;  // Exit the method if no member is found
            }
        }

        // Proceed with update
        System.out.print("Enter new Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter new Address: ");
        String address = scanner.nextLine();

        // update record
        String sql = "UPDATE hamadayaz.Member SET name = ?, phoneNo = ?, address = ? WHERE MID = ?";
        try (PreparedStatement pstmt = dbconn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phoneNo);
            pstmt.setString(3, address);
            pstmt.setString(4, MID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                dbconn.commit();
                System.out.println("Member updated successfully.");
            } else {
                System.out.println("Update failed. No member found with ID: " + MID);
            }
        }
    }

    /**
     * Method Name: deleteMember
     * Purpose: Deletes a member from the database after checking if they are eligible for deletion based on ticket counts.
     * Pre-conditions: The member must exist, and if they have more than 10 tickets, a redemption process is triggered.
     * Post-conditions: Member record is removed if they have fewer than 10 tickets or after redeeming their tickets.
     * Parameters:
     * - scanner (in): Scanner object for capturing the member ID from user input.
     */
    private static void deleteMember(Scanner scanner) throws SQLException {
        System.out.print("Enter Member ID to delete: ");
        String MID = scanner.nextLine();

        try { // select tickets
            String sqlCheckTickets = "SELECT totalTickets FROM hamadayaz.Member WHERE MID = ?";
            PreparedStatement pstmt = dbconn.prepareStatement(sqlCheckTickets);
            pstmt.setString(1, MID);
            ResultSet rs = pstmt.executeQuery();

            // handle possibilities
            if (rs.next()) {
                int totalTickets = rs.getInt("totalTickets");
                if (totalTickets > 10) {
                    manageTicketRedemption(scanner, MID, totalTickets); // redeem prizes
                } else {
                    System.out.println("Member has fewer than 10 tickets, they can be deleted directly.");
                    proceedToDeleteMember(MID); // delete member
                }
            } else {
                System.out.println("No member found with ID: " + MID);
            }
        } catch (SQLException e) {
            System.out.println("Transaction failed: " + e.getMessage());
            dbconn.rollback();
            throw e;
        }
    }

    /**
     * Method Name: manageTicketRedemption
     * Purpose: Manages the ticket redemption process for a member before they are deleted.
     * Pre-conditions: Called when a member has more than 10 tickets.
     * Post-conditions: Prizes are redeemed, and tickets are deducted accordingly.
     * Parameters:
     * - scanner (in): Scanner object for user interaction.
     * - MID (in): Member ID.
     * - totalTickets (in): The total number of tickets the member has.
     */
    private static void manageTicketRedemption(Scanner scanner, String MID, int totalTickets) throws SQLException {
        while (totalTickets > 10) {
            System.out.println("\nMember has " + totalTickets + " tickets available for redemption:");
            if (listAndRedeemPrizes(scanner, MID, totalTickets)) {
                System.out.println("No prizes were redeemed. Exiting redemption process.");
                return;
            }
            totalTickets = updateMemberTicketCount(MID); // update tickets
        }
        proceedToDeleteMember(MID); // then delete member
    }

    /**
     * Method Name: listAndRedeemPrizes
     * Purpose: Lists available prizes and manages the redemption process.
     * Pre-conditions: The member has enough tickets for at least one prize.
     * Post-conditions: Member's tickets are updated based on redeemed prizes.
     * Parameters:
     * - scanner (in): Scanner object for user interaction.
     * - MID (in): Member ID.
     * - totalTickets (in): The current ticket count of the member.
     */
    private static boolean listAndRedeemPrizes(Scanner scanner, String MID, int totalTickets) throws SQLException {
        String sql = "SELECT PrizeID, description, ticketCost FROM hamadayaz.Prize WHERE ticketCost <= ? AND inventoryCount > 0 ORDER BY ticketCost";
        PreparedStatement pstmt = dbconn.prepareStatement(sql);
        pstmt.setInt(1, totalTickets);
        ResultSet rs = pstmt.executeQuery();

        boolean hasPrizes = false;
        while (rs.next()) {
            hasPrizes = true;
            String prizeID = rs.getString("PrizeID");
            String description = rs.getString("description");
            int ticketCost = rs.getInt("ticketCost");
            System.out.println("PrizeID: " + prizeID + ", Description: " + description + ", Ticket Cost: " + ticketCost);
        }
        // handling
        if (!hasPrizes) {
            System.out.println("No prizes available for the current ticket count.");
            return true;
        }
        // redeem
        System.out.print("Enter Prize ID to redeem (or type 'skip' to cancel): ");
        String prizeId = scanner.nextLine();
        if ("skip".equalsIgnoreCase(prizeId)) {
            return true;
        }

        return !redeemSelectedPrize(MID, prizeId);
    }

    /**
     * Method Name: redeemSelectedPrize
     * Purpose: Processes the redemption of a selected prize for a member.
     * Pre-conditions: The selected prize must be available and within the member's ticket budget.
     * Post-conditions: Prize inventory and member's tickets are updated.
     * Parameters:
     * - MID (in): Member ID.
     * - prizeId (in): Prize ID of the selected prize.
     */
    private static boolean redeemSelectedPrize(String MID, String prizeId) throws SQLException {
        String sql = "SELECT ticketCost, inventoryCount FROM hamadayaz.Prize WHERE PrizeID = ? AND inventoryCount > 0";
        PreparedStatement pstmt = dbconn.prepareStatement(sql);
        pstmt.setString(1, prizeId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            int ticketCost = rs.getInt("ticketCost");
            int inventoryCount = rs.getInt("inventoryCount");

            if (inventoryCount > 0) {
                // Update Prize Inventory
                sql = "UPDATE hamadayaz.Prize SET inventoryCount = inventoryCount - 1 WHERE PrizeID = ?";
                pstmt = dbconn.prepareStatement(sql);
                pstmt.setString(1, prizeId);
                pstmt.executeUpdate();

                // Update Member Tickets
                sql = "UPDATE hamadayaz.Member SET totalTickets = totalTickets - ? WHERE MID = ?";
                pstmt = dbconn.prepareStatement(sql);
                pstmt.setInt(1, ticketCost);
                pstmt.setString(2, MID);
                pstmt.executeUpdate();

                // Record the transaction
                sql = "INSERT INTO hamadayaz.PrizeRedemption (XactID, MID, PrizeID, xactDate) VALUES (seq_prize_redemption.nextval, ?, ?, CURRENT_DATE)";
                pstmt = dbconn.prepareStatement(sql);
                pstmt.setString(1, MID);
                pstmt.setString(2, prizeId);
                pstmt.executeUpdate();

                dbconn.commit();
                System.out.println("Prize redeemed successfully.");
                return true;
            } else {
                System.out.println("Selected prize cannot be redeemed due to insufficient inventory.");
                return false;
            }
        } else {
            System.out.println("Selected prize not found or inventory is zero.");
            return false;
        }
    }

    /**
     * Method Name: updateMemberTicketCount
     * Purpose: Retrieves and returns the latest ticket count for a member.
     * Pre-conditions: Member must exist in the database.
     * Post-conditions: Returns the current ticket count.
     * Parameters:
     * - MID (in): Member ID.
     */
    private static int updateMemberTicketCount(String MID) throws SQLException {
        String sql = "SELECT totalTickets FROM hamadayaz.Member WHERE MID = ?";
        PreparedStatement pstmt = dbconn.prepareStatement(sql);
        pstmt.setString(1, MID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("totalTickets");
        }
        return 0;  // Default if no data found
    }

    /**
     * Method Name: proceedToDeleteMember
     * Purpose: Completes the deletion process of a member after all conditions are met.
     * Pre-conditions: All dependent records must be handled before deletion.
     * Post-conditions: Member is removed from the database.
     * Parameters:
     * - MID (in): Member ID.
     */
    private static void proceedToDeleteMember(String MID) throws SQLException {
        try {
            // Delete related records from all dependent tables
            deleteRelatedRecords(MID);

            // Finally, delete the member
            String sqlDeleteMember = "DELETE FROM hamadayaz.Member WHERE MID = ?";
            try (PreparedStatement pstmtDelete = dbconn.prepareStatement(sqlDeleteMember)) {
                pstmtDelete.setString(1, MID);
                int affectedRows = pstmtDelete.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Member deleted successfully.");
                    dbconn.commit(); // Commit all changes if everything is successful
                } else {
                    System.out.println("Failed to delete member. No member found with ID: " + MID);
                    dbconn.rollback(); // Rollback if the member does not exist
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during deletion: " + e.getMessage());
            dbconn.rollback(); // Rollback on any error during the deletion process
            throw e;
        }
    }

    /**
     * Method Name: deleteRelatedRecords
     * Purpose: Deletes all records related to the member from dependent tables before the member itself is deleted.
     * Pre-conditions: Member ID must exist.
     * Post-conditions: All records associated with the member in related tables are deleted.
     * Parameters:
     * - MID (in): Member ID.
     */
    private static void deleteRelatedRecords(String MID) throws SQLException {
        // List of related tables
        String[] relatedTables = new String[] { "PrizeRedemption", "Coupon", "TokenPurchase", "Gameplay" };
        for (String table : relatedTables) {
            String sqlDelete = "DELETE FROM hamadayaz." + table + " WHERE MID = ?";
            try (PreparedStatement pstmt = dbconn.prepareStatement(sqlDelete)) {
                pstmt.setString(1, MID);
                pstmt.executeUpdate(); // Execute the delete command for each table
            }
        }
    }

    /**
     * Method Name: addGame
     * Purpose: Adds a new game to the database.
     * Pre-conditions: Game ID provided must not already exist in the database.
     * Post-conditions: A new game record is added to the database.
     * Parameters:
     * - scanner (in): Scanner object for capturing game data from user input.
     */
    private static void addGame(Scanner scanner) throws SQLException {
        System.out.print("Enter Game ID: ");
        String gid = scanner.nextLine();
        System.out.print("Enter Game Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Token Cost: ");
        int tokenCost = scanner.nextInt();
        System.out.print("Enter Factor: ");
        double factor = scanner.nextDouble();
        scanner.nextLine();

        // Insert record
        String sql = "INSERT INTO hamadayaz.Game (GID, name, tokenCost, factor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbconn.prepareStatement(sql)) {
            pstmt.setString(1, gid);
            pstmt.setString(2, name);
            pstmt.setInt(3, tokenCost);
            pstmt.setDouble(4, factor);
            int count = pstmt.executeUpdate();
            dbconn.commit();  // Commit the transaction
            System.out.println(count + " game(s) added.");
        } catch (SQLException e) {
            System.err.println("Error adding game: " + e.getMessage());
            dbconn.rollback();  // Rollback on error
            throw e;
        }
    }

    /**
     * Method Name: deleteGame
     * Purpose: Deletes a game from the database after ensuring all related gameplay records are also deleted.
     * Pre-conditions: Game ID must exist in the database.
     * Post-conditions: Game and all related gameplay records are removed.
     * Parameters:
     * - scanner (in): Scanner object for capturing the game ID from user input.
     */
    private static void deleteGame(Scanner scanner) throws SQLException {
        System.out.print("Enter Game ID to delete: ");
        String gid = scanner.nextLine();

        try {
            // Start by deleting gameplay records for the game
            String sqlDeleteGameplay = "DELETE FROM hamadayaz.GamePlay WHERE GID = ?";
            try (PreparedStatement pstmtGameplay = dbconn.prepareStatement(sqlDeleteGameplay)) {
                pstmtGameplay.setString(1, gid);
                pstmtGameplay.executeUpdate();  // Execute
            }

            // delete the game itself
            String sqlDeleteGame = "DELETE FROM hamadayaz.Game WHERE GID = ?";
            try (PreparedStatement pstmtGame = dbconn.prepareStatement(sqlDeleteGame)) {
                pstmtGame.setString(1, gid);
                int gameAffectedRows = pstmtGame.executeUpdate();

                if (gameAffectedRows > 0) {
                    dbconn.commit();  // Commit the transaction if game deletion is successful
                    System.out.println("Game records deleted successfully.");
                } else {
                    System.out.println("No game found with ID: " + gid);
                    dbconn.rollback();  // Rollback if the game does not exist or other issues
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting game: " + e.getMessage());
            dbconn.rollback();  // Rollback on error
            throw e;
        }
    }

    /**
     * Method Name: redeemPrizes
     * Purpose: Facilitates the redemption of prizes for a member based on their available tickets.
     * Pre-conditions: Member ID must exist and have enough tickets.
     * Post-conditions: Prizes are redeemed, and tickets are deducted.
     * Parameters:
     * - scanner (in): Scanner object for user interaction.
     */
    private static void redeemPrizes(Scanner scanner) throws SQLException {
        System.out.print("Enter Your Member ID: ");
        String MID = scanner.nextLine();
        try {
            // Fetch the total number of tickets the member has
            String sql = "SELECT totalTickets FROM hamadayaz.Member WHERE MID = ?";
            PreparedStatement pstmt = dbconn.prepareStatement(sql);
            pstmt.setString(1, MID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalTickets = rs.getInt("totalTickets");
                if (totalTickets > 0) {
                    System.out.println("\nYou have " + totalTickets + " tickets. Redeeming prizes...");
                    if (listAndRedeemPrizes(scanner, MID, totalTickets)) {
                        System.out.println("No prizes were redeemed.");
                    }
                } else {
                    System.out.println("You do not have enough tickets to redeem any prizes.");
                }
            } else {
                System.out.println("Member ID not found. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            dbconn.rollback();
        }
    }

    /**
     * Method Name: buyTokens
     * Purpose: Manages the purchase of game tokens by members, updating their spending total and potentially their membership tier.
     * Pre-conditions: Member ID must exist in the database.
     * Post-conditions: Member's total spent and total tickets are updated based on the purchase.
     * Parameters:
     * - scanner (in): Scanner object for capturing the amount to be spent on tokens.
     */
    private static void buyTokens(Scanner scanner) throws SQLException {
        System.out.print("Enter Your Member ID: ");
        String MID = scanner.nextLine();

        // Check if member exists in the database
        String memberCheckSql = "SELECT * FROM hamadayaz.Member WHERE MID = ?";
        PreparedStatement checkStmt = dbconn.prepareStatement(memberCheckSql);
        checkStmt.setString(1, MID);
        ResultSet checkRs = checkStmt.executeQuery();
        if (!checkRs.next()) {
            System.out.println("Member ID not found.");
            return; // Exit if member does not exist
        }

        System.out.print("Enter amount to spend on tokens ($): ");
        double amountSpent = scanner.nextDouble();
        scanner.nextLine();

        // Retrieve member details
        double currentSpent = checkRs.getDouble("totalSpent");
        int currentTickets = checkRs.getInt("totalTickets");
        String currentTier = checkRs.getString("tier");

        // Calculate discount based on tier
        double discount = 0.0;
        if ("Diamond".equals(currentTier)) {
            discount = 0.20; // 20% discount for Diamond members
        } else if ("Gold".equals(currentTier)) {
            discount = 0.10; // 10% discount for Gold members
        }

        double discountedAmount = amountSpent * (1 - discount); // Actual money spent after discount
        double amountSaved = amountSpent - discountedAmount; // Amount saved due to discount
        int tokensPurchased = (int) (amountSpent * 5); // 5 tokens per dollar of the amount spent, not the discounted amount

        // Update total spent to include only the discounted amount
        double newTotalSpent = currentSpent + discountedAmount;
        // To assign membership
        double preDiscountSpent = currentSpent + amountSpent;

        // Calculate bonus tickets for tier upgrades
        int bonusTickets = 0;
        String newTier = currentTier;
        if (preDiscountSpent >= 500 && !"Diamond".equals(currentTier) && !"Gold".equals(currentTier)) {
            newTier = "Diamond";
            bonusTickets = 15000; // Bonus for upgrading to Diamond
        }
        else if (preDiscountSpent >= 500 && "Gold".equals(currentTier)) {
            newTier = "Diamond";
            bonusTickets = 10000; // Bonus for upgrading to Diamond
        }
        else if (preDiscountSpent >= 250 && !"Gold".equals(currentTier) && !"Diamond".equals(currentTier)) {
            newTier = "Gold";
            bonusTickets = 5000; // Bonus for upgrading to Gold
        }

        // Update member records with new totals, tickets, and tier
        String updateSql = "UPDATE hamadayaz.Member SET totalSpent = ?, totalTickets = ?, tier = ? WHERE MID = ?";
        PreparedStatement updateStmt = dbconn.prepareStatement(updateSql);
        updateStmt.setDouble(1, newTotalSpent);
        updateStmt.setInt(2, currentTickets + bonusTickets);
        updateStmt.setString(3, newTier);
        updateStmt.setString(4, MID);
        updateStmt.executeUpdate();

        // Record the token purchase transaction
        String purchaseSql = "INSERT INTO hamadayaz.TokenPurchase (PID, MID, tokenNo, purchaseDate, amountSpent) VALUES (seq_token_purchase.nextval, ?, ?, CURRENT_DATE, ?)";
        PreparedStatement purchaseStmt = dbconn.prepareStatement(purchaseSql);
        purchaseStmt.setString(1, MID);
        purchaseStmt.setInt(2, tokensPurchased);
        purchaseStmt.setDouble(3, discountedAmount);
        purchaseStmt.executeUpdate();

        dbconn.commit();

        System.out.println(tokensPurchased + " tokens purchased successfully. Total spent: $" + discountedAmount + " (You saved: $" + amountSaved + " with a " + (int)(discount * 100) + "% discount), Tickets awarded: " + bonusTickets + ", Tier: " + newTier);
    }

    /**
     * Method Name: addOrUpdatePrize
     * Purpose: Adds a new prize to the database or updates an existing one if it already exists.
     * Pre-conditions: The prize may or may not exist in the database.
     * Post-conditions: A new prize is added or an existing prize's inventory is updated.
     * Parameters:
     * - scanner (in): Scanner object for capturing prize details from user input.
     * Author: Worked with JennyYu
     */
    private static void addOrUpdatePrize(Scanner scanner) throws SQLException {
        System.out.print("Enter Prize ID: ");
        String prizeID = scanner.nextLine();

        String sqlSelect = "SELECT * FROM hamadayaz.Prize WHERE prizeID = ?";
        PreparedStatement selectStmt = dbconn.prepareStatement(sqlSelect);
        selectStmt.setString(1, prizeID);
        ResultSet rs = selectStmt.executeQuery();

        // if prize already exists then update inventory
        if (rs.next()) {
            System.out.print("Prize already exists. Enter additional inventory to add: ");
            int additionalInventory = scanner.nextInt();
            scanner.nextLine();

            int newInventory = rs.getInt("inventoryCount") + additionalInventory;
            String sqlUpdate = "UPDATE hamadayaz.Prize SET inventoryCount = ? WHERE prizeID = ?";
            PreparedStatement updateStmt = dbconn.prepareStatement(sqlUpdate);
            updateStmt.setInt(1, newInventory);
            updateStmt.setString(2, prizeID);
            updateStmt.executeUpdate();
            System.out.println("Updated inventory for prize ID: " + prizeID);
        } else {
            System.out.print("Enter Description: ");
            String description = scanner.nextLine();
            System.out.print("Enter Ticket Cost: ");
            int ticketCost = scanner.nextInt();
            System.out.print("Enter Inventory Count: ");
            int inventoryCount = scanner.nextInt();
            scanner.nextLine();

            // insert prize
            String sqlInsert = "INSERT INTO hamadayaz.Prize (prizeID, description, ticketCost, inventoryCount) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = dbconn.prepareStatement(sqlInsert);
            insertStmt.setString(1, prizeID);
            insertStmt.setString(2, description);
            insertStmt.setInt(3, ticketCost);
            insertStmt.setInt(4, inventoryCount);
            insertStmt.executeUpdate();
            System.out.println("New prize added.");
        }
        dbconn.commit();
    }

    /**
     * Method Name: deletePrize
     * Purpose: Deletes a prize from the database after removing all related redemption records.
     * Pre-conditions: Prize ID must exist in the database.
     * Post-conditions: Prize and all related redemption records are removed.
     * Parameters:
     * - scanner (in): Scanner object for capturing the prize ID from user input.
     * Author: Worked with JennyYu
     */
    private static void deletePrize(Scanner scanner) throws SQLException {
        System.out.print("Enter Prize ID to delete: ");
        String prizeID = scanner.nextLine();

        // Delete Prize Redemption records first
        String deleteRedemptionsSql = "DELETE FROM hamadayaz.PrizeRedemption WHERE PrizeID = ?";
        PreparedStatement deleteRedemptionsStmt = dbconn.prepareStatement(deleteRedemptionsSql);
        deleteRedemptionsStmt.setString(1, prizeID);
        deleteRedemptionsStmt.executeUpdate();

        // Now delete the prize
        String deletePrizeSql = "DELETE FROM hamadayaz.Prize WHERE prizeID = ?";
        PreparedStatement deletePrizeStmt = dbconn.prepareStatement(deletePrizeSql);
        deletePrizeStmt.setString(1, prizeID);
        int prizeRowsAffected = deletePrizeStmt.executeUpdate();

        if (prizeRowsAffected > 0) {
            System.out.println("Prize deleted successfully.");
        } else {
            System.out.println("No prize found with ID: " + prizeID);
        }

        dbconn.commit();
    }

    /**
     * Method Name: runQueries
     * Purpose: Provides a menu to run various predefined queries about games, members, and prizes.
     * Pre-conditions: Queries must be defined and executable against the database.
     * Post-conditions: Executes selected query and displays results.
     * Parameters:
     * - scanner (in): Scanner object for user interaction.
     */
    private static void runQueries(Scanner scanner) {
        while (true) {
            System.out.println("\n0. <-- to Main Menu");
            System.out.println("Select a query to run:");
            System.out.println("1. List all games and high scores");
            System.out.println("2. Members who spent $100 on tokens this month");
            System.out.println("3. Prizes a member can redeem");
            System.out.println("4. Highest game score by a member");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 0:
                    System.out.println("Returning to main menu...");
                    return;  // Exits the current method.
                case 1:
                    Queries.query1(dbconn);
                    break;
                case 2:
                    Queries.query2(dbconn);
                    break;
                case 3:
                    System.out.print("Enter Member ID: ");
                    String MID = scanner.nextLine();
                    Queries.query3(MID, dbconn);
                    break;
                case 4:
                    System.out.print("Enter Member ID: ");
                    String memberID = scanner.nextLine();
                    Queries.query4(memberID, dbconn);
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }
}
