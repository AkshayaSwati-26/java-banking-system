import java.sql.*;
import java.util.Scanner;

public class Account {
    private int id;
    private String accountNumber;
    private double balance;
    private Scanner sc = new Scanner(System.in);

    public boolean login(String accNum, String pin) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM accounts WHERE account_number = ? AND pin = ?");
            ps.setString(1, accNum);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.id = rs.getInt("id");
                this.accountNumber = accNum;
                this.balance = rs.getDouble("balance");
                System.out.println("Login successful.\n");
                return true;
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void register() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Create Account Number: ");
        String accNum = sc.nextLine();
        System.out.print("Create PIN: ");
        String pin = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO accounts (name, account_number, pin) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, accNum);
            ps.setString(3, pin);
            ps.executeUpdate();
            System.out.println("Account created successfully!");
        } catch (SQLException e) {
            System.out.println("Error: Account might already exist.");
        }
    }

    public void checkBalance() {
        System.out.println("Your Balance: $" + balance);
    }

    public void deposit(double amount) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            balance += amount;
            PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
            ps.setDouble(1, balance);
            ps.setInt(2, id);
            ps.executeUpdate();

            PreparedStatement ts = conn.prepareStatement("INSERT INTO transactions (account_id, type, amount) VALUES (?, 'deposit', ?)");
            ts.setInt(1, id);
            ts.setDouble(2, amount);
            ts.executeUpdate();

            conn.commit();
            System.out.println("Deposit successful.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Insufficient funds.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            balance -= amount;
            PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
            ps.setDouble(1, balance);
            ps.setInt(2, id);
            ps.executeUpdate();

            PreparedStatement ts = conn.prepareStatement("INSERT INTO transactions (account_id, type, amount) VALUES (?, 'withdraw', ?)");
            ts.setInt(1, id);
            ts.setDouble(2, amount);
            ts.executeUpdate();

            conn.commit();
            System.out.println("Withdrawal successful.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transfer(String toAccNum, double amount) {
        if (amount > balance) {
            System.out.println("Insufficient funds for transfer.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check if target exists
            PreparedStatement getReceiver = conn.prepareStatement("SELECT id, balance FROM accounts WHERE account_number = ?");
            getReceiver.setString(1, toAccNum);
            ResultSet rs = getReceiver.executeQuery();

            if (!rs.next()) {
                System.out.println("Target account not found.");
                return;
            }

            int toId = rs.getInt("id");
            double toBalance = rs.getDouble("balance");

            // Update sender
            balance -= amount;
            PreparedStatement updateSender = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
            updateSender.setDouble(1, balance);
            updateSender.setInt(2, id);
            updateSender.executeUpdate();

            // Update receiver
            PreparedStatement updateReceiver = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?");
            updateReceiver.setDouble(1, toBalance + amount);
            updateReceiver.setInt(2, toId);
            updateReceiver.executeUpdate();

            // Log transaction
            PreparedStatement log = conn.prepareStatement("INSERT INTO transactions (account_id, type, amount, related_account) VALUES (?, 'transfer', ?, ?)");
            log.setInt(1, id);
            log.setDouble(2, amount);
            log.setString(3, toAccNum);
            log.executeUpdate();

            conn.commit();
            System.out.println("Transfer successful.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewMiniStatement() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC LIMIT 5");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Last 5 Transactions ---");
            while (rs.next()) {
                System.out.printf("%s of $%.2f on %s", rs.getString("type"), rs.getDouble("amount"), rs.getTimestamp("timestamp"));
                String related = rs.getString("related_account");
                if (related != null) {
                    System.out.print(" to/from: " + related);
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewAllTransactions() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Full Transaction History ---");
            while (rs.next()) {
                System.out.printf("%s of $%.2f on %s\n", rs.getString("type"), rs.getDouble("amount"), rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void changePin() {
        System.out.print("Enter current PIN: ");
        String currentPin = sc.nextLine();
        System.out.print("Enter new PIN: ");
        String newPin = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET pin = ? WHERE id = ? AND pin = ?");
            ps.setString(1, newPin);
            ps.setInt(2, id);
            ps.setString(3, currentPin);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("PIN changed successfully.");
            } else {
                System.out.println("Incorrect current PIN.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void filterTransactions() {
        System.out.print("Filter by (type/date): ");
        String filter = sc.nextLine();

        String query = "SELECT * FROM transactions WHERE account_id = ?";
        if (filter.equalsIgnoreCase("type")) {
            System.out.print("Enter type (deposit/withdraw/transfer): ");
            String type = sc.nextLine();
            query += " AND type = ?";
        } else if (filter.equalsIgnoreCase("date")) {
            System.out.print("Start Date (YYYY-MM-DD): ");
            String start = sc.nextLine();
            System.out.print("End Date (YYYY-MM-DD): ");
            String end = sc.nextLine();
            query += " AND DATE(timestamp) BETWEEN ? AND ?";
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, id);
            if (filter.equalsIgnoreCase("type")) {
                ps.setString(2, sc.nextLine());
            } else if (filter.equalsIgnoreCase("date")) {
                ps.setString(2, sc.nextLine());
                ps.setString(3, sc.nextLine());
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("%s of $%.2f on %s\n", rs.getString("type"), rs.getDouble("amount"), rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateProfile() {
        System.out.print("New Email: ");
        String email = sc.nextLine();
        System.out.print("New Phone: ");
        String phone = sc.nextLine();
        System.out.print("New Address: ");
        String address = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET email = ?, phone = ?, address = ? WHERE id = ?");
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setInt(4, id);
            ps.executeUpdate();
            System.out.println("Profile updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void createFixedDeposit() {
        System.out.print("Enter principal amount: ");
        double principal = Double.parseDouble(sc.nextLine());
        System.out.print("Duration in months: ");
        int months = Integer.parseInt(sc.nextLine());
        double rate = 0.05; // 5% annual interest

        double interest = principal * Math.pow((1 + rate / 12), months) - principal;
        double maturity = principal + interest;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO fixed_deposits (account_id, principal, months, interest_rate, maturity_amount, start_date, maturity_date) VALUES (?, ?, ?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? MONTH))"
            );
            ps.setInt(1, id);
            ps.setDouble(2, principal);
            ps.setInt(3, months);
            ps.setDouble(4, rate);
            ps.setDouble(5, maturity);
            ps.setInt(6, months);
            ps.executeUpdate();

            System.out.printf("Fixed Deposit Created. Maturity Amount: $%.2f\n", maturity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
