package com.expense.split;

import com.expense.split.design.Color;
import com.expense.split.gui.ExpenseSplitterGUI;

public class ExpenseSplitterApplication {
    public static void main(String[] args) {
        System.out.println(Color.BLUE + "STARTING APPLICATION..." + Color.RESET);
        ExpenseSplitterGUI.launch();
    }
}
// import com.expense.split.design.Color;
// import com.expense.split.dto.Input;
// import com.expense.split.manager.ExpenseManager;
// import com.expense.split.manager.UserManager;
// import com.expense.split.model.Expense;
// import com.expense.split.model.SplitType;
// import com.expense.split.model.User;
// import com.expense.split.gui.ExpenseSplitterGUI;
// import java.util.*;

// public class ExpenseSplitterApplication {
//     private static final UserManager userManager = new UserManager();
//     private static final ExpenseManager expenseManager = new ExpenseManager();

//     public static void main(String[] args) {
//         System.out.println("1. Launch CLI");
//         System.out.println("2. Launch GUI (Premium)");
//         System.out.print("Choose mode: ");
//         Scanner scMode = new Scanner(System.in);
//         String mode = scMode.nextLine();
//         if ("2".equals(mode)) {
//             ExpenseSplitterGUI.launch();
//             return;
//         }

//         User currentUser = null;

//         Scanner sc = new Scanner(System.in);

//         while (true) {
//             try {
//                 if (currentUser == null) {
//                     System.out.println(Color.BRIGHT_BLUE + "\n=== Expense Splitter ===" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "1. Signup" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "2. Login" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "3. Exit" + Color.RESET);
//                     System.out.print(Color.YELLOW + "Choose option: " + Color.CYAN);

//                     String choice = sc.nextLine().trim();
//                     System.out.print(Color.RESET);

//                     switch (choice) {
//                         case "1" -> { // signup
//                             try {
//                                 User user = userManager.registerUser();
//                                 if (user != null)
//                                     System.out.println(Color.BRIGHT_GREEN + "Signup successful. You can login now." + Color.RESET);
//                                 else
//                                     System.out.println(Color.BRIGHT_RED + "Signup failed." + Color.RESET);

//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[UNKNOWN ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "2" -> { // login
//                             try {
//                                 String email = Input.email("Enter email: ");
//                                 String password = Input.password("Enter password: ");

//                                 User user = userManager.findByEmail(email);
//                                 if (user == null) {
//                                     System.out.println(Color.BRIGHT_RED + "User not found." + Color.RESET);
//                                 } else if (!user.getPassword().equals(password)) {
//                                     System.out.println(Color.BRIGHT_RED + "Incorrect password." + Color.RESET);
//                                 } else {
//                                     currentUser = user;
//                                     System.out.println(Color.BRIGHT_GREEN + "Login successful. Welcome, " + currentUser.getName() + "!" + Color.RESET);
//                                 }

//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[UNKNOWN ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "3" -> {
//                             System.out.println(Color.BRIGHT_YELLOW + "Exiting..." + Color.RESET);
//                             sc.close();
//                             return;
//                         }

//                         default -> System.out.println(Color.BRIGHT_RED + "Invalid option." + Color.RESET);
//                     }

//                 } else { // logged in menu
//                     System.out.println(Color.BRIGHT_BLUE + "\n=== Main Menu (Logged in as " + currentUser.getName() + ") ===" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "1. View Profile" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "2. Update Name" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "3. Update Email" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "4. Change Password" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "5. Clear Split History" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "6. Delete Account" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "7. Logout" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "8. Create Expense" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "9. List All Expenses" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "10. View Expense by ID" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "11. Delete Expense" + Color.RESET);
//                     System.out.println(Color.BRIGHT_GREEN + "12. List All Users" + Color.RESET);
//                     System.out.print(Color.YELLOW + "Choose option: " + Color.CYAN);

//                     String choice = sc.nextLine().trim();
//                     System.out.print(Color.RESET);

//                     switch (choice) {
//                         case "1" -> System.out.println(Color.BRIGHT_WHITE + currentUser.toString() + Color.RESET);

//                         case "2" -> {
//                             try {
//                                 String name = Input.name("Enter new name: ");
//                                 if (userManager.updateName(currentUser, name))
//                                     System.out.println(Color.BRIGHT_GREEN + "Name updated." + Color.RESET);
//                                 else
//                                     System.out.println(Color.BRIGHT_RED + "Update failed." + Color.RESET);
//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "3" -> {
//                             try {
//                                 String email = Input.email("Enter new email: ");
//                                 if (userManager.updateEmail(currentUser, email))
//                                     System.out.println(Color.BRIGHT_GREEN + "Email updated." + Color.RESET);
//                                 else
//                                     System.out.println(Color.BRIGHT_RED + "Update failed." + Color.RESET);
//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "4" -> {
//                             try {
//                                 String newPassword = Input.password("Enter new password: ");
//                                 if (userManager.changePassword(currentUser, newPassword))
//                                     System.out.println(Color.BRIGHT_GREEN + "Password changed." + Color.RESET);
//                                 else
//                                     System.out.println(Color.BRIGHT_RED + "Password change failed." + Color.RESET);
//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "5" -> {
//                             if (userManager.clearHistory(currentUser))
//                                 System.out.println(Color.BRIGHT_GREEN + "Split history cleared." + Color.RESET);
//                             else
//                                 System.out.println(Color.BRIGHT_RED + "Operation failed." + Color.RESET);
//                         }

//                         case "6" -> {
//                             System.out.print(Color.YELLOW + "Are you sure you want to delete your account? (yes/no): " + Color.CYAN);
//                             String ans = sc.nextLine().trim().toLowerCase();
//                             System.out.print(Color.RESET);
//                             if (ans.equals("yes") || ans.equals("y")) {
//                                 if (userManager.deleteUser(currentUser)) {
//                                     System.out.println(Color.BRIGHT_GREEN + "Account deleted." + Color.RESET);
//                                     currentUser = null;
//                                 } else {
//                                     System.out.println(Color.BRIGHT_RED + "Delete failed." + Color.RESET);
//                                 }
//                             }
//                         }

//                         case "7" -> {
//                             currentUser = null;
//                             System.out.println(Color.BRIGHT_YELLOW + "Logged out." + Color.RESET);
//                         }

//                         case "8" -> { // create expense
//                             try {
//                                 double amount = Input.amount("Enter total amount: ");
//                                 User paidTo = currentUser;

//                                 System.out.println(Color.BRIGHT_GREEN + "Select participants (comma separated ids):" + Color.RESET);
//                                 listUsers();

//                                 System.out.print(Color.YELLOW + "Enter ids: " + Color.CYAN);
//                                 String line = sc.nextLine().trim();
//                                 System.out.print(Color.RESET);
                                
//                                 String[] parts = line.split(",");
                                
//                                 List<User> participants = new ArrayList<>();
//                                 for (String p : parts) {
//                                     try {
//                                         long id = Long.parseLong(p.trim());
//                                         User u = userManager.findById(id);
//                                         if (u != null) participants.add(u);
//                                     } catch (NumberFormatException ignored) {}
//                                 }

//                                 SplitType splitType = Input.splitType("Enter split type (EQUAL/PERCENTAGE): ");
//                                 Map<User, Double> inputData = new HashMap<>();

//                                 if (splitType == SplitType.PERCENTAGE) {
//                                     for (User u : participants) {
//                                         System.out.print(Color.YELLOW + "Enter percentage for " + u.getName() + ": " + Color.CYAN);
//                                         double per = Double.parseDouble(sc.nextLine().trim());
//                                         System.out.print(Color.RESET);
//                                         inputData.put(u, per);
//                                     }
//                                 }

//                                 System.out.print(Color.YELLOW + "Enter group id: " + Color.CYAN);
//                                 long groupId = Long.parseLong(sc.nextLine().trim());
//                                 System.out.print(Color.RESET);

//                                 String description = Input.description("Enter description: ");

//                                 boolean ok = expenseManager.createExpense(amount, paidTo, participants, splitType, inputData, description, groupId);
//                                 System.out.println(ok ? (Color.BRIGHT_GREEN + "Expense created." + Color.RESET) : (Color.BRIGHT_RED + "Failed to create expense." + Color.RESET));

//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "9" -> {
//                             List<Expense> all = expenseManager.listAllExpenses();
//                             System.out.println(Color.BRIGHT_WHITE + "Expenses: " + all + Color.RESET);
//                         }

//                         case "10" -> {
//                             try {
//                                 long id = Input.id("Enter expense id: ");
//                                 Expense exp = expenseManager.findExpenseById(id);
//                                 if (exp != null)
//                                     System.out.println(Color.BRIGHT_WHITE + exp.toString() + Color.RESET);
//                                 else
//                                     System.out.println(Color.BRIGHT_RED + "Expense not found." + Color.RESET);
//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "11" -> {
//                             try {
//                                 long id = Input.id("Enter expense id to delete: ");
//                                 Expense exp = expenseManager.findExpenseById(id);
//                                 if (exp == null) {
//                                     System.out.println(Color.BRIGHT_RED + "Expense not found." + Color.RESET);
//                                 } else {
//                                     if (expenseManager.deleteExpense(exp))
//                                         System.out.println(Color.BRIGHT_GREEN + "Expense deleted." + Color.RESET);
//                                     else
//                                         System.out.println(Color.BRIGHT_RED + "Delete failed." + Color.RESET);
//                                 }
//                             } catch (Exception e) {
//                                 System.err.println(Color.BRIGHT_RED + "[ERROR]: " + e.getMessage() + Color.RESET);
//                             }
//                         }

//                         case "12" -> listUsers();

//                         default -> System.out.println(Color.BRIGHT_RED + "Invalid option." + Color.RESET);
//                     }
//                 }
//             } catch (Exception e) {
//                 System.err.println(Color.BRIGHT_RED + "[UNEXPECTED ERROR]: " + e.getMessage() + Color.RESET);
//             }
//         }
//     }

//     private static void listUsers() {
//         List<User> users = userManager.listAllUsers();
//         if (users.isEmpty()) {
//             System.out.println(Color.BRIGHT_YELLOW + "No users registered." + Color.RESET);
//             return;
//         }

//         for (User u : users) {
//             System.out.println(Color.BRIGHT_WHITE + u.toString() + Color.RESET);
//         }
//     }
// }
