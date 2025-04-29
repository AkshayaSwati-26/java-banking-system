import java.util.Scanner;

public class BankingApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Account acc = new Account();

        System.out.println("Welcome to the Java Banking System");
        System.out.println("1. Register");
        System.out.println("2. Login");

        System.out.print("Choose option: ");
        int option = Integer.parseInt(sc.nextLine());

        if (option == 1) {
            Account.register();
        } else if (option == 2) {
            System.out.print("Account Number: ");
            String accNum = sc.nextLine();
            System.out.print("PIN: ");
            String pin = sc.nextLine();

            if (acc.login(accNum, pin)) {
                int choice;
                do {
                    System.out.println("\n--- Menu ---");
                    System.out.println("1. Check Balance");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Fund Transfer");
                    System.out.println("5. Mini Statement");
                    System.out.println("6. Full Transaction History");
                    System.out.println("7. Exit");
                    System.out.println("8. Change PIN");
                    System.out.println("9. Filter Transactions");
                    System.out.println("10. Update Profile");
                    System.out.println("11. Create Fixed Deposit");
                    System.out.println("12. Exit");

                    System.out.print("Choose: ");
                    choice = Integer.parseInt(sc.nextLine());

                    switch (choice) {
                        case 1 -> acc.checkBalance();
                        case 2 -> {
                            System.out.print("Amount to deposit: ");
                            double amt = Double.parseDouble(sc.nextLine());
                            acc.deposit(amt);
                        }
                        case 3 -> {
                            System.out.print("Amount to withdraw: ");
                            double amt = Double.parseDouble(sc.nextLine());
                            acc.withdraw(amt);
                        }
                        case 4 -> {
                            System.out.print("Target Account Number: ");
                            String to = sc.nextLine();
                            System.out.print("Amount: ");
                            double amt = Double.parseDouble(sc.nextLine());
                            acc.transfer(to, amt);
                        }
                        case 5 -> acc.viewMiniStatement();
                        case 6 -> acc.viewAllTransactions();
                        case 7 -> System.out.println("Goodbye!");
                        case 8 -> acc.changePin();
                        case 9 -> acc.filterTransactions();
                        case 10 -> acc.updateProfile();
                        case 11 -> acc.createFixedDeposit();
                        case 12 -> System.out.println("Goodbye!");

                        default -> System.out.println("Invalid option.");
                    }
                } while (choice != 7);
            }
        } else {
            System.out.println("Invalid input.");
        }
    }
}
