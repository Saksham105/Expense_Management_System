package com.expense.split.gui;

import com.expense.split.manager.ExpenseManager;
import com.expense.split.manager.GroupManager;
import com.expense.split.manager.UserManager;
import com.expense.split.model.*;
import com.expense.split.service.DashboardService;
import com.expense.split.service.DebtSimplifier;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ExpenseSplitterGUI extends JFrame {
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final ExpenseManager expenseManager;
    private final DebtSimplifier debtSimplifier;

    private User currentUser;
    private Group currentGroup;

    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    private final DashboardService dashboardService;

    // Colors for "Premium" feel
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(0, 150, 136);
    private static final Color BG_COLOR = new Color(245, 245, 245);

    public ExpenseSplitterGUI() {
        this.userManager = new UserManager();
        this.groupManager = new GroupManager();
        this.expenseManager = new ExpenseManager();
        this.debtSimplifier = new DebtSimplifier();
        this.dashboardService = new DashboardService();

        setTitle("Expense Splitter - Premium Edition");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

        initLoginScreen();
        initDashboard();
        initGroupView();

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    private void initLoginScreen() {
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(BG_COLOR);

        JPanel loginForm = new JPanel(new GridBagLayout());
        loginForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to Expense Splitter");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginForm.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; loginForm.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1; loginForm.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; loginForm.add(new JLabel("Password:"), gbc);
        JPasswordField passField = new JPasswordField(20);
        gbc.gridx = 1; loginForm.add(passField, gbc);

        JButton loginBtn = createStyledButton("Login", PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginForm.add(loginBtn, gbc);

        JButton signupBtn = createStyledButton("Register New Account", ACCENT_COLOR);
        gbc.gridy = 4;
        loginForm.add(signupBtn, gbc);

        JPanel loginFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginFooter.setOpaque(false);
        JButton loginBackBtn = createStyledButton("Back", Color.GRAY);
        loginFooter.add(loginBackBtn);
        loginBackBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Exit Expense Splitter?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        loginPanel.add(loginForm, BorderLayout.CENTER);
        loginPanel.add(loginFooter, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            User user = userManager.findByEmail(email);
            if (user != null && user.getPassword().equals(pass)) {
                currentUser = user;
                refreshDashboard();
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Name:");
            String email = JOptionPane.showInputDialog(this, "Enter Email:");
            String pass = JOptionPane.showInputDialog(this, "Enter Password:");
            if (name != null && email != null && pass != null
                    && !name.isBlank() && !email.isBlank() && !pass.isEmpty()) {
                User registered = userManager.registerUser(name, email, pass);
                if (registered != null) {
                    JOptionPane.showMessageDialog(this, "Registered! Now login.");
                } else if (userManager.listAllUsers().stream()
                        .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim()))) {
                    JOptionPane.showMessageDialog(this, "Email already exists");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Please try again.");
                }
            }
        });

        mainPanel.add(loginPanel, "Login");
    }

    private JPanel dashboardContent;
    private void initDashboard() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BG_COLOR);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel welcomeLabel = new JLabel("Your Groups");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(welcomeLabel, BorderLayout.WEST);

        JPanel dashboardHeaderEast = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dashboardHeaderEast.setOpaque(false);
        JButton dashboardBackBtn = createStyledButton("Back", Color.WHITE);
        dashboardBackBtn.setForeground(PRIMARY_COLOR);
        JButton createGroupBtn = createStyledButton("+ Create Group", Color.WHITE);
        createGroupBtn.setForeground(PRIMARY_COLOR);
        dashboardHeaderEast.add(dashboardBackBtn);
        dashboardHeaderEast.add(createGroupBtn);
        header.add(dashboardHeaderEast, BorderLayout.EAST);
        dashboardPanel.add(header, BorderLayout.NORTH);

        dashboardBackBtn.addActionListener(e -> {
            currentUser = null;
            currentGroup = null;
            cardLayout.show(mainPanel, "Login");
        });

        dashboardContent = new JPanel();
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(dashboardContent);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);

        createGroupBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Group Name:");
            if (name != null && !name.trim().isEmpty()) {
                groupManager.createGroup(name, currentUser);
                refreshDashboard();
            }
        });

        mainPanel.add(dashboardPanel, "Dashboard");
    }

    private void refreshDashboard() {
        dashboardContent.removeAll();
        List<Group> groups = groupManager.getGroupsForUser(currentUser);
        for (Group g : groups) {
            JPanel card = new JPanel(new BorderLayout());
            card.setMaximumSize(new Dimension(800, 80));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(10, 20, 10, 20),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY)
            ));
            card.setBackground(Color.WHITE);

            JLabel gName = new JLabel(g.getName());
            gName.setFont(new Font("SansSerif", Font.PLAIN, 18));
            card.add(gName, BorderLayout.CENTER);

            JButton openBtn = createStyledButton("Open", ACCENT_COLOR);
            openBtn.addActionListener(e -> {
                currentGroup = g;
                refreshGroupView();
                cardLayout.show(mainPanel, "GroupView");
            });
            card.add(openBtn, BorderLayout.EAST);

            dashboardContent.add(card);
        }
        dashboardContent.revalidate();
        dashboardContent.repaint();
    }

    private JPanel expenseListPanel;
    private JLabel groupTitleLabel;
    private void initGroupView() {
        JPanel groupViewPanel = new JPanel(new BorderLayout());
        groupViewPanel.setBackground(BG_COLOR);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        groupTitleLabel = new JLabel("Group Name");
        groupTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.add(groupTitleLabel, BorderLayout.WEST);

        JPanel navBtns = new JPanel(new FlowLayout());
        navBtns.setBackground(BG_COLOR);
        JButton backBtn = createStyledButton("Back", Color.GRAY);
        JButton addMemberBtn = createStyledButton("Add Member", PRIMARY_COLOR);
        JButton addExpenseBtn = createStyledButton("Add Expense", ACCENT_COLOR);
        JButton settleBtn = createStyledButton("Settle Up", new Color(255, 87, 34));

        navBtns.add(backBtn);
        navBtns.add(addMemberBtn);
        navBtns.add(addExpenseBtn);
        navBtns.add(settleBtn);
        header.add(navBtns, BorderLayout.EAST);

        groupViewPanel.add(header, BorderLayout.NORTH);

        expenseListPanel = new JPanel();
        expenseListPanel.setLayout(new BoxLayout(expenseListPanel, BoxLayout.Y_AXIS));
        groupViewPanel.add(new JScrollPane(expenseListPanel), BorderLayout.CENTER);

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));

        addMemberBtn.addActionListener(e -> {
            String email_idStr = JOptionPane.showInputDialog(this, "Enter User email ID to add:");
            if (email_idStr != null) {
                try {
                    User u = userManager.findByEmail(email_idStr);
                    if (u != null) {
                        groupManager.addMember(currentGroup.getId(), u);
                        JOptionPane.showMessageDialog(this, "Added " + u.getName());
                    } else {
                        JOptionPane.showMessageDialog(this, "User not found");
                    }
                } catch (Exception ex) {}
            }
        });

        addExpenseBtn.addActionListener(e -> showAddExpenseDialog());
        settleBtn.addActionListener(e -> showSettlementDialog());

        mainPanel.add(groupViewPanel, "GroupView");
    }

    private void refreshGroupView() {
        groupTitleLabel.setText(currentGroup.getName());
        expenseListPanel.removeAll();
        for (Expense exp : currentGroup.getExpenses()) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            p.add(new JLabel("<html><b>" + exp.getDescription() + "</b>: " + exp.getTotalAmount() + " paid by " + exp.getPaidTo().getName() + "</html>"));
            expenseListPanel.add(p);
        }
        expenseListPanel.revalidate();
        expenseListPanel.repaint();
    }

    private void showAddExpenseDialog() {
        JDialog dialog = new JDialog(this, "Add Expense", true);
        dialog.setSize(400, 500);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField descField = new JTextField(15);
        JTextField amountField = new JTextField(15);
        JComboBox<SplitType> typeCombo = new JComboBox<>(SplitType.values());

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; dialog.add(descField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; dialog.add(amountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Split Type:"), gbc);
        gbc.gridx = 1; dialog.add(typeCombo, gbc);

        // Selection of participants - simplified for GUI
        JLabel info = new JLabel("All group members will be participants.");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(info, gbc);

        JButton saveBtn = createStyledButton("Save Expense", ACCENT_COLOR);
        gbc.gridy = 4;
        dialog.add(saveBtn, gbc);

        JButton cancelExpenseBtn = createStyledButton("Back", Color.GRAY);
        gbc.gridy = 5;
        dialog.add(cancelExpenseBtn, gbc);

        cancelExpenseBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String desc = descField.getText();
                SplitType type = (SplitType) typeCombo.getSelectedItem();
                
                List<User> participants = new ArrayList<>(currentGroup.getMembers());
                // For simplicity in GUI demo, we exclude the payer relative to existing logic if needed
                // But the current ExpenseService logic says participants should not contain payer
                participants.remove(currentUser);

                Map<User, Double> inputData = new HashMap<>();
                if (type == SplitType.PERCENTAGE) {
                    for (User u : participants) {
                        String p = JOptionPane.showInputDialog(dialog, "Percentage for " + u.getName() + ":");
                        inputData.put(u, Double.parseDouble(p));
                    }
                } else if (type == SplitType.SHARE) {
                    for (User u : participants) {
                        String p = JOptionPane.showInputDialog(dialog, "Shares for " + u.getName() + ":");
                        inputData.put(u, Double.parseDouble(p));
                    }
                } else if (type == SplitType.ITEM_LEVEL) {
                    for (User u : participants) {
                        String p = JOptionPane.showInputDialog(dialog, "Amount for " + u.getName() + ":");
                        inputData.put(u, Double.parseDouble(p));
                    }
                }

                boolean ok = expenseManager.createExpense(amount, currentUser, participants, type, inputData, desc, currentGroup.getId());
                if (ok) {
                    // Manual sync since currentGroup in memory might not be the repository instance
                    // Actually GroupManager should return the same instance if persistent, 
                    // but for this demo let's add it to the local list if not already there
                    currentGroup.addExpense(expenseManager.listAllExpenses().get(expenseManager.listAllExpenses().size()-1));
                    refreshGroupView();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create expense. Check logic/percentages.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSettlementDialog() {
        List<DebtSimplifier.Settlement> settlements = debtSimplifier.simplifyDebts(currentGroup);
        StringBuilder sb = new StringBuilder("<html><h3>Minimum Transactions to Settle:</h3><ul>");
        if (settlements.isEmpty()) {
            sb.append("<li>Everyone is settled!</li>");
        } else {
            for (DebtSimplifier.Settlement s : settlements) {
                sb.append("<li>").append(s.payer.getName()).append(" owes ").append(s.payee.getName())
                  .append(": <b>").append(String.format("%.2f", s.amount)).append("</b></li>");
            }
        }
        sb.append("</ul></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Settlement View", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(color == Color.WHITE ? PRIMARY_COLOR : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        return btn;
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new ExpenseSplitterGUI().setVisible(true));
    }
}
