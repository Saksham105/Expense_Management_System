package com.expense.split.gui;

import com.expense.split.manager.ExpenseManager;
import com.expense.split.manager.GroupManager;
import com.expense.split.manager.UserManager;
import com.expense.split.model.*;
import com.expense.split.service.DashboardService;
import com.expense.split.service.DebtSimplifier;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Premium Dark-Themed GUI for the Expense Splitter System.
 *
 * Screens  : Login/Register → Dashboard → GroupView
 * Dialogs  : AddExpense | Profile | ManageGroup | SettleUp
 * Settle   : Uses SplitDetail.setStatus(true) to mark debts as paid;
 *            DashboardService + DebtSimplifier both filter paid splits.
 */
public class ExpenseSplitterGUI extends JFrame {

    // ------------------------------------------------------------Managers ------------------------------------------------------------
    private final UserManager       userManager;
    private final GroupManager      groupManager;
    private final ExpenseManager    expenseManager;
    private final DebtSimplifier    debtSimplifier;
    private final DashboardService  dashboardService;

    // ------------------------------------------------------------Session State ------------------------------------------------------------
    private User  currentUser;
    private Group currentGroup;

    // ------------------------------------------------------------ Layout ------------------------------------------------------------
    private final JPanel     mainPanel;
    private final CardLayout cardLayout;

    // ----------------------- Mutable dashboard sub-panels (rebuilt on refresh) ------------------------------------------------------------
    private JPanel dashboardGroupGrid;
    private JPanel dashboardSummaryPanel;
    private JLabel dashboardWelcomeLabel;

    // ------------------------------------------------------------Mutable group-view sub-panels ------------------------------------------------------------
    private JLabel groupTitleLabel;
    private JPanel balanceSummaryPanel;
    private JPanel expenseListPanel;

    // ==========================================================================
    //  DESIGN SYSTEM
    // ==========================================================================
    private static final Color BG_DARK    = new Color(0x1A1A2E);
    private static final Color BG_CARD    = new Color(0x16213E);
    private static final Color BG_ITEM    = new Color(0x0F3460);
    private static final Color ACCENT     = new Color(0xE94560);
    private static final Color ACCENT2    = new Color(0x533483);
    private static final Color SUCCESS    = new Color(0x00C87F);
    private static final Color WARN       = new Color(0xF0A500);
    private static final Color TEXT_PRI   = new Color(0xEAEAEA);
    private static final Color TEXT_MUTED = new Color(0x8892B0);
    private static final Color BORDER_CLR = new Color(0x2D3561);

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_H2    = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONT_BODY  = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BOLD  = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    // ==========================================================================
    //  CONSTRUCTOR
    // ==========================================================================
    public ExpenseSplitterGUI() {
        this.userManager     = new UserManager();
        this.groupManager    = new GroupManager();
        this.expenseManager  = new ExpenseManager();
        this.debtSimplifier  = new DebtSimplifier();
        this.dashboardService = new DashboardService();

        setTitle("Expense Splitter — Premium Edition");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);

        initLoginScreen();
        initDashboard();
        initGroupView();

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    // ==========================================================================
    //  SCREEN 1 — LOGIN / REGISTER
    // ==========================================================================
    private void initLoginScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ── Left branded sidebar ───────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(BG_CARD);
        sidebar.setPreferredSize(new Dimension(330, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(60, 40, 40, 40));

        JLabel logo    = new JLabel("💰");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 64));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("SplitWise Pro");
        appName.setFont(new Font("SansSerif", Font.BOLD, 28));
        appName.setForeground(TEXT_PRI);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel(
                "<html><center>Split expenses fairly.<br>Settle debts smarter.</center></html>");
        tagline.setFont(FONT_BODY);
        tagline.setForeground(TEXT_MUTED);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 18)));
        sidebar.add(appName);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalGlue());

        // ── Right form card ────────────────────────────────────────────────
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(BG_DARK);
        formWrapper.setBorder(new EmptyBorder(60, 80, 60, 80)); // padding around the card

        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(36, 40, 36, 40)));
        // No setMaximumSize needed — card fills the BorderLayout CENTER

        // Tab bar
        JPanel tabBar = new JPanel(new GridLayout(1, 2, 4, 0));
        tabBar.setBackground(BG_CARD);
        tabBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tabBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginTab = createTabButton("Login",    true);
        JButton regTab   = createTabButton("Register", false);
        tabBar.add(loginTab);
        tabBar.add(regTab);

        // ── Login form ─────────────────────────────────────────────────────
        JPanel loginForm = cardForm(BG_CARD);

        JTextField    loginEmail = mkField(false);
        JPasswordField loginPass = (JPasswordField) mkField(true);
        JLabel         loginErr  = errorLabel();
        JButton        loginBtn  = createPrimaryBtn("Login  →");

        loginForm.add(Box.createRigidArea(new Dimension(0, 16)));
        loginForm.add(fieldGroup("Email Address", loginEmail));
        loginForm.add(Box.createRigidArea(new Dimension(0, 12)));
        loginForm.add(fieldGroup("Password", loginPass));
        loginForm.add(loginErr);
        loginForm.add(Box.createRigidArea(new Dimension(0, 18)));
        loginForm.add(loginBtn);

        // ── Register form ──────────────────────────────────────────────────
        JPanel regForm = cardForm(BG_CARD);
        regForm.setVisible(false);

        JTextField     regName  = mkField(false);
        JTextField     regEmail = mkField(false);
        JPasswordField regPass  = (JPasswordField) mkField(true);
        JPasswordField regPass2 = (JPasswordField) mkField(true);
        JLabel         regErr   = errorLabel();
        JButton        regBtn   = createPrimaryBtn("Create Account  →");

        regForm.add(Box.createRigidArea(new Dimension(0, 16)));
        regForm.add(fieldGroup("Full Name", regName));
        regForm.add(Box.createRigidArea(new Dimension(0, 10)));
        regForm.add(fieldGroup("Email Address", regEmail));
        regForm.add(Box.createRigidArea(new Dimension(0, 10)));
        regForm.add(fieldGroup("Password", regPass));
        regForm.add(Box.createRigidArea(new Dimension(0, 10)));
        regForm.add(fieldGroup("Confirm Password", regPass2));
        regForm.add(regErr);
        regForm.add(Box.createRigidArea(new Dimension(0, 18)));
        regForm.add(regBtn);

        // Fixed-width inner panel — centers all form content horizontally inside the card
        JPanel innerPanel = new JPanel();
        innerPanel.setBackground(BG_CARD);
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setMaximumSize(new Dimension(440, Integer.MAX_VALUE));
        innerPanel.setPreferredSize(new Dimension(440, 0));
        innerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        innerPanel.add(tabBar);
        innerPanel.add(loginForm);
        innerPanel.add(regForm);

        // Vertical glue pushes innerPanel to the visual center of the card
        card.add(Box.createVerticalGlue());
        card.add(innerPanel);
        card.add(Box.createVerticalGlue());
        formWrapper.add(card, BorderLayout.CENTER);

        root.add(sidebar,      BorderLayout.WEST);
        root.add(formWrapper,  BorderLayout.CENTER);

        // ── Tab toggle logic ───────────────────────────────────────────────
        loginTab.addActionListener(e -> {
            styleTab(loginTab, true); styleTab(regTab, false);
            loginForm.setVisible(true); regForm.setVisible(false);
            loginErr.setText(" ");
        });
        regTab.addActionListener(e -> {
            styleTab(regTab, true); styleTab(loginTab, false);
            regForm.setVisible(true); loginForm.setVisible(false);
            regErr.setText(" ");
        });

        // ── Login action ───────────────────────────────────────────────────
        loginBtn.addActionListener(e -> {
            String email = loginEmail.getText().trim();
            String pass  = new String(loginPass.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                loginErr.setText("⚠  Please fill all fields"); return;
            }
            User user = userManager.findByEmail(email);
            if (user == null || !user.getPassword().equals(pass)) {
                loginErr.setText("⚠  Invalid email or password");
            } else {
                currentUser = user;
                loginErr.setText(" ");
                loginEmail.setText(""); loginPass.setText("");
                refreshDashboard();
                cardLayout.show(mainPanel, "Dashboard");
            }
        });
        loginPass.addActionListener(e -> loginBtn.doClick());

        // ── Register action ────────────────────────────────────────────────
        regBtn.addActionListener(e -> {
            String name  = regName.getText().trim();
            String email = regEmail.getText().trim();
            String pass  = new String(regPass.getPassword());
            String pass2 = new String(regPass2.getPassword());

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                regErr.setText("⚠  All fields are required"); return;
            }
            if (!pass.equals(pass2)) {
                regErr.setText("⚠  Passwords do not match"); return;
            }
            if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
                regErr.setText("⚠  Enter a valid email address"); return;
            }
            User registered = userManager.registerUser(name, email, pass);
            if (registered != null) {
                regErr.setForeground(SUCCESS);
                regErr.setText("✓  Account created! Please login.");
                regName.setText(""); regEmail.setText("");
                regPass.setText(""); regPass2.setText("");
                javax.swing.Timer t = new javax.swing.Timer(1800, ev -> {
                    styleTab(loginTab, true); styleTab(regTab, false);
                    loginForm.setVisible(true); regForm.setVisible(false);
                    regErr.setForeground(ACCENT); regErr.setText(" ");
                });
                t.setRepeats(false); t.start();
            } else {
                regErr.setText("⚠  Email already registered");
            }
        });
        regPass2.addActionListener(e -> regBtn.doClick());

        mainPanel.add(root, "Login");
    }

    // ==========================================================================
    //  SCREEN 2 — DASHBOARD
    // ==========================================================================
    private void initDashboard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ── Left sidebar ───────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(BG_CARD);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 20, 20));

        dashboardWelcomeLabel = new JLabel("Hello, User");
        dashboardWelcomeLabel.setFont(FONT_H2);
        dashboardWelcomeLabel.setForeground(TEXT_PRI);
        dashboardWelcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Your expense groups");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(200, 1));

        JButton profileBtn = createSidebarBtn("👤   My Profile");
        JButton logoutBtn  = createSidebarBtn("🚪   Logout");

        sidebar.add(dashboardWelcomeLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(subLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        // ── Center: header + scrollable grid ──────────────────────────────
        dashboardGroupGrid = new JPanel();
        dashboardGroupGrid.setBackground(BG_DARK);
        dashboardGroupGrid.setLayout(new BoxLayout(dashboardGroupGrid, BoxLayout.Y_AXIS));
        dashboardGroupGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane centerScroll = new JScrollPane(dashboardGroupGrid);
        centerScroll.setBorder(null);
        centerScroll.getViewport().setBackground(BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel groupsTitle = new JLabel("My Groups");
        groupsTitle.setFont(FONT_TITLE);
        groupsTitle.setForeground(TEXT_PRI);

        JButton createGroupBtn = createAccentBtn("+ New Group");
        topBar.add(groupsTitle,    BorderLayout.WEST);
        topBar.add(createGroupBtn, BorderLayout.EAST);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BG_DARK);
        centerWrapper.add(topBar,       BorderLayout.NORTH);
        centerWrapper.add(centerScroll, BorderLayout.CENTER);

        // ── Right summary panel ────────────────────────────────────────────
        dashboardSummaryPanel = new JPanel();
        dashboardSummaryPanel.setBackground(BG_CARD);
        dashboardSummaryPanel.setPreferredSize(new Dimension(220, 0));
        dashboardSummaryPanel.setLayout(new BoxLayout(dashboardSummaryPanel, BoxLayout.Y_AXIS));
        dashboardSummaryPanel.setBorder(new EmptyBorder(30, 15, 20, 15));

        root.add(sidebar,             BorderLayout.WEST);
        root.add(centerWrapper,       BorderLayout.CENTER);
        root.add(dashboardSummaryPanel, BorderLayout.EAST);

        // ── Actions ────────────────────────────────────────────────────────
        logoutBtn.addActionListener(e -> {
            currentUser = null; currentGroup = null;
            cardLayout.show(mainPanel, "Login");
        });
        profileBtn.addActionListener(e -> showProfileDialog());
        createGroupBtn.addActionListener(e -> {
            String name = showInputPrompt("Create New Group", "Group name (e.g. Trip to Goa):");
            if (name != null && !name.trim().isEmpty()) {
                groupManager.createGroup(name.trim(), currentUser);
                refreshDashboard();
            }
        });

        mainPanel.add(root, "Dashboard");
    }

    private void refreshDashboard() {
        dashboardWelcomeLabel.setText("Hello, " + currentUser.getName().split(" ")[0] + " 👋");

        // ── Group cards ────────────────────────────────────────────────────
        dashboardGroupGrid.removeAll();
        List<Group> groups = groupManager.getGroupsForUser(currentUser);

        if (groups.isEmpty()) {
            JLabel empty = new JLabel("No groups yet — click \"+ New Group\" to start.");
            empty.setForeground(TEXT_MUTED);
            empty.setFont(FONT_BODY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            dashboardGroupGrid.add(Box.createVerticalGlue());
            dashboardGroupGrid.add(empty);
            dashboardGroupGrid.add(Box.createVerticalGlue());
        } else {
            for (Group g : groups) {
                dashboardGroupGrid.add(buildGroupCard(g));
                dashboardGroupGrid.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        // ── Overall summary ────────────────────────────────────────────────
        dashboardSummaryPanel.removeAll();

        JLabel sumTitle = new JLabel("Overall Balance");
        sumTitle.setFont(FONT_H2);
        sumTitle.setForeground(TEXT_PRI);
        sumTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        dashboardSummaryPanel.add(sumTitle);
        dashboardSummaryPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        double totalOwe = 0, totalOwed = 0;
        for (Group g : groups) {
            DashboardService.UserBalance b = dashboardService.getUserBalanceInGroup(currentUser, g);
            totalOwe  += b.totalOwe;
            totalOwed += b.totalOwedTo;
        }
        double net = totalOwed - totalOwe;

        dashboardSummaryPanel.add(statCard("You Owe",         String.format("₹ %.2f", totalOwe),  ACCENT));
        dashboardSummaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardSummaryPanel.add(statCard("You Are Owed",    String.format("₹ %.2f", totalOwed), SUCCESS));
        dashboardSummaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardSummaryPanel.add(statCard("Net Balance",     String.format("₹ %.2f", net),       net >= 0 ? SUCCESS : WARN));

        dashboardGroupGrid.revalidate();    dashboardGroupGrid.repaint();
        dashboardSummaryPanel.revalidate(); dashboardSummaryPanel.repaint();
    }

    private JPanel buildGroupCard(Group g) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(16, 20, 16, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setBackground(BG_CARD);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(g.getName());
        name.setFont(FONT_H2);
        name.setForeground(TEXT_PRI);

        JLabel meta = new JLabel(g.getMembers().size() + " members  ·  " + g.getExpenses().size() + " expenses");
        meta.setFont(FONT_SMALL);
        meta.setForeground(TEXT_MUTED);

        info.add(name);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(meta);

        double total = g.getExpenses().stream().mapToDouble(Expense::getTotalAmount).sum();
        JLabel spend = new JLabel(String.format("₹ %.2f", total));
        spend.setFont(FONT_H2);
        spend.setForeground(TEXT_PRI);
        spend.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(BG_CARD);
        JButton openBtn = createSmallBtn("Open →", ACCENT);
        openBtn.addActionListener(e -> {
            currentGroup = g;
            refreshGroupView();
            cardLayout.show(mainPanel, "GroupView");
        });
        btns.add(openBtn);

        card.add(info,  BorderLayout.WEST);
        card.add(spend, BorderLayout.CENTER);
        card.add(btns,  BorderLayout.EAST);

        addPanelHover(card, BG_CARD, BG_ITEM);
        return card;
    }

    // ==========================================================================
    //  SCREEN 3 — GROUP VIEW
    // ==========================================================================
    private void initGroupView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ------------------------------------------------------------Header ------------------------------------------------------------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_ITEM);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerLeft.setBackground(BG_ITEM);

        JButton backBtn = createSmallBtn("← Back", TEXT_MUTED);
        backBtn.addActionListener(e -> {
            refreshDashboard();
            cardLayout.show(mainPanel, "Dashboard");
        });

        groupTitleLabel = new JLabel("Group Name");
        groupTitleLabel.setFont(FONT_TITLE);
        groupTitleLabel.setForeground(TEXT_PRI);

        headerLeft.add(backBtn);
        headerLeft.add(groupTitleLabel);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(BG_ITEM);

        JButton manageBtn  = createSmallBtn("⚙ Manage Group", TEXT_MUTED);
        JButton addExpBtn  = createSmallBtn("+ Add Expense",  ACCENT);
        JButton settleBtn  = createSmallBtn("✓ Settle Up",    SUCCESS);

        headerRight.add(manageBtn);
        headerRight.add(addExpBtn);
        headerRight.add(settleBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // ------------------------------------------------------------Content ------------------------------------------------------------
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Balance summary (left)
        balanceSummaryPanel = new JPanel();
        balanceSummaryPanel.setBackground(BG_CARD);
        balanceSummaryPanel.setLayout(new BoxLayout(balanceSummaryPanel, BoxLayout.Y_AXIS));
        balanceSummaryPanel.setPreferredSize(new Dimension(240, 0));
        balanceSummaryPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                new EmptyBorder(20, 16, 20, 16)));

        // Expense list (right)
        expenseListPanel = new JPanel();
        expenseListPanel.setBackground(BG_DARK);
        expenseListPanel.setLayout(new BoxLayout(expenseListPanel, BoxLayout.Y_AXIS));

        JScrollPane expScroll = new JScrollPane(expenseListPanel);
        expScroll.setBorder(null);
        expScroll.getViewport().setBackground(BG_DARK);

        content.add(balanceSummaryPanel, BorderLayout.WEST);
        content.add(expScroll,           BorderLayout.CENTER);

        root.add(header,  BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);

        // ------------------------------------------------------------Actions ------------------------------------------------------------
        manageBtn.addActionListener(e -> showManageGroupDialog());
        addExpBtn.addActionListener(e -> showAddExpenseDialog(null));
        settleBtn.addActionListener(e -> showSettlementDialog());

        mainPanel.add(root, "GroupView");
    }

    private void refreshGroupView() {
        groupTitleLabel.setText(currentGroup.getName());

        // ------------------------------------------------------------Balance summary ------------------------------------------------------------
        balanceSummaryPanel.removeAll();

        DashboardService.UserBalance bal =
                dashboardService.getUserBalanceInGroup(currentUser, currentGroup);

        JLabel bTitle = new JLabel("Balance Summary");
        bTitle.setFont(FONT_H2);
        bTitle.setForeground(TEXT_PRI);
        bTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        balanceSummaryPanel.add(bTitle);
        balanceSummaryPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        balanceSummaryPanel.add(statCard("You Owe",      String.format("₹ %.2f", bal.totalOwe),    ACCENT));
        balanceSummaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        balanceSummaryPanel.add(statCard("Owed to You",  String.format("₹ %.2f", bal.totalOwedTo), SUCCESS));
        balanceSummaryPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (!bal.perPersonBalance.isEmpty()) {
            JLabel perLabel = new JLabel("Per Person");
            perLabel.setFont(FONT_SMALL);
            perLabel.setForeground(TEXT_MUTED);
            perLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            balanceSummaryPanel.add(perLabel);
            balanceSummaryPanel.add(Box.createRigidArea(new Dimension(0, 8)));

            for (Map.Entry<User, Double> entry : bal.perPersonBalance.entrySet()) {
                double amount = entry.getValue();
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setBackground(BG_CARD);
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

                JLabel personLbl = new JLabel(entry.getKey().getName());
                personLbl.setFont(FONT_SMALL);
                personLbl.setForeground(TEXT_PRI);

                JLabel amtLbl = new JLabel(String.format("₹ %.2f", Math.abs(amount)));
                amtLbl.setFont(FONT_SMALL);
                amtLbl.setForeground(amount > 0 ? SUCCESS : ACCENT);

                row.add(personLbl, BorderLayout.WEST);
                row.add(amtLbl,    BorderLayout.EAST);
                balanceSummaryPanel.add(row);
                balanceSummaryPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }
        balanceSummaryPanel.add(Box.createVerticalGlue());
        balanceSummaryPanel.revalidate(); balanceSummaryPanel.repaint();

        // ------------------------------------------------------------Expense list ------------------------------------------------------------
        expenseListPanel.removeAll();
        List<Expense> expenses = currentGroup.getExpenses();

        if (expenses.isEmpty()) {
            JLabel empty = new JLabel("No expenses yet. Add one using the button above.");
            empty.setForeground(TEXT_MUTED);
            empty.setFont(FONT_BODY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            expenseListPanel.add(Box.createRigidArea(new Dimension(0, 40)));
            expenseListPanel.add(empty);
        } else {
            for (Expense exp : expenses) {
                expenseListPanel.add(buildExpenseRow(exp));
                expenseListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        expenseListPanel.revalidate(); expenseListPanel.repaint();
    }

    private JPanel buildExpenseRow(Expense exp) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_CARD);
        row.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(12, 16, 12, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setBackground(BG_CARD);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel descLbl = new JLabel(exp.getDescription() + (exp.isRecurring() ? "  🔁" : ""));
        descLbl.setFont(FONT_BOLD);
        descLbl.setForeground(TEXT_PRI);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        JLabel metaLbl = new JLabel(
                "Paid by " + exp.getPaidTo().getName()
                + "  ·  " + exp.getType().name()
                + "  ·  " + exp.getCreatedAt().format(fmt));
        metaLbl.setFont(FONT_SMALL);
        metaLbl.setForeground(TEXT_MUTED);

        info.add(descLbl);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(metaLbl);

        JLabel amtLbl = new JLabel(String.format("₹ %.2f", exp.getTotalAmount()));
        amtLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        amtLbl.setForeground(TEXT_PRI);
        amtLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setBackground(BG_CARD);
        JButton editBtn = createSmallBtn("✏", TEXT_MUTED);
        JButton delBtn  = createSmallBtn("🗑", ACCENT);

        editBtn.addActionListener(e -> showAddExpenseDialog(exp));
        delBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete expense \"" + exp.getDescription() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                currentGroup.getExpenses().remove(exp);
                expenseManager.deleteExpense(exp);
                refreshGroupView();
            }
        });
        btns.add(editBtn);
        btns.add(delBtn);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG_CARD);
        right.add(amtLbl, BorderLayout.CENTER);
        right.add(btns,   BorderLayout.EAST);

        row.add(info,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        addPanelHover(row, BG_CARD, BG_ITEM);
        return row;
    }

    // ==========================================================================
    //  DIALOG — ADD / EDIT EXPENSE
    // ==========================================================================
    private void showAddExpenseDialog(Expense existing) {
        JDialog dlg = new JDialog(this, existing == null ? "Add Expense" : "Edit Expense", true);
        dlg.setSize(560, 650);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel dHeader = styledDialogHeader(existing == null ? "➕  New Expense" : "✏  Edit Expense", ACCENT);
        dlg.add(dHeader, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(BG_DARK);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 28, 10, 28));

        JTextField  descField   = mkField(false);
        JTextField  amountField = mkField(false);
        JComboBox<SplitType> typeCombo = new JComboBox<>(SplitType.values());
        styleCombo(typeCombo);

        if (existing != null) {
            descField.setText(existing.getDescription());
            amountField.setText(String.valueOf(existing.getTotalAmount()));
            typeCombo.setSelectedItem(existing.getType());
        }

        // Participant checkboxes (exclude payer = currentUser)
        List<User> members = new ArrayList<>(currentGroup.getMembers());
        members.remove(currentUser);

        Map<User, JCheckBox> cbMap = new LinkedHashMap<>();
        JPanel partPanel = new JPanel();
        partPanel.setBackground(BG_CARD);
        partPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1), new EmptyBorder(10, 12, 10, 12)));
        partPanel.setLayout(new BoxLayout(partPanel, BoxLayout.Y_AXIS));

        JLabel partLabel = new JLabel("Participants");
        partLabel.setFont(FONT_SMALL);
        partLabel.setForeground(TEXT_MUTED);
        partPanel.add(partLabel);
        partPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        for (User u : members) {
            JCheckBox cb = new JCheckBox(u.getName());
            cb.setBackground(BG_CARD);
            cb.setForeground(TEXT_PRI);
            cb.setFont(FONT_BODY);
            cb.setSelected(true);
            cbMap.put(u, cb);
            partPanel.add(cb);
        }

        // Dynamic per-person inputs
        JPanel perPanel = new JPanel();
        perPanel.setBackground(BG_DARK);
        perPanel.setLayout(new BoxLayout(perPanel, BoxLayout.Y_AXIS));
        Map<User, JTextField> perFields = new LinkedHashMap<>();

        Runnable rebuildPerPanel = () -> {
            perPanel.removeAll();
            SplitType sel = (SplitType) typeCombo.getSelectedItem();
            if (sel == SplitType.PERCENTAGE || sel == SplitType.SHARE || sel == SplitType.ITEM_LEVEL) {
                String hint = sel == SplitType.PERCENTAGE ? "%" : sel == SplitType.SHARE ? "shares" : "₹ amount";
                for (Map.Entry<User, JCheckBox> e : cbMap.entrySet()) {
                    if (!e.getValue().isSelected()) continue;
                    JPanel r = new JPanel(new BorderLayout(8, 0));
                    r.setBackground(BG_DARK);
                    r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                    JLabel nameL = new JLabel(e.getKey().getName());
                    nameL.setForeground(TEXT_PRI);
                    nameL.setFont(FONT_BODY);
                    JTextField valF = perFields.computeIfAbsent(e.getKey(), k -> {
                        JTextField tf = mkField(false);
                        tf.setText("0");
                        return tf;
                    });
                    JLabel hintL = new JLabel(hint);
                    hintL.setForeground(TEXT_MUTED);
                    hintL.setFont(FONT_SMALL);
                    r.add(nameL, BorderLayout.WEST);
                    r.add(valF,  BorderLayout.CENTER);
                    r.add(hintL, BorderLayout.EAST);
                    perPanel.add(r);
                    perPanel.add(Box.createRigidArea(new Dimension(0, 6)));
                }
            }
            perPanel.revalidate(); perPanel.repaint();
        };

        typeCombo.addActionListener(e -> rebuildPerPanel.run());
        cbMap.values().forEach(cb -> cb.addActionListener(e -> rebuildPerPanel.run()));
        rebuildPerPanel.run();

        JCheckBox recurringChk = new JCheckBox("Mark as recurring (monthly)");
        recurringChk.setBackground(BG_DARK);
        recurringChk.setForeground(TEXT_MUTED);
        recurringChk.setFont(FONT_SMALL);
        if (existing != null) recurringChk.setSelected(existing.isRecurring());

        JLabel formErr = errorLabel();

        body.add(fieldGroup("Description",  descField));
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(fieldGroup("Amount (₹)",   amountField));
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(labelGroup("Split Type",   typeCombo));
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(partPanel);
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(perPanel);
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(recurringChk);
        body.add(formErr);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getViewport().setBackground(BG_DARK);
        dlg.add(bodyScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(10, 28, 18, 28));
        JButton cancelBtn = createSidebarBtn("Cancel");
        JButton saveBtn   = createPrimaryBtn("Save Expense");
        footer.add(cancelBtn);
        footer.add(saveBtn);
        dlg.add(footer, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                if (desc.isEmpty()) { formErr.setText("⚠  Description required"); return; }
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0)  { formErr.setText("⚠  Amount must be positive"); return; }

                SplitType type = (SplitType) typeCombo.getSelectedItem();
                List<User> participants = new ArrayList<>();
                for (Map.Entry<User, JCheckBox> entry : cbMap.entrySet())
                    if (entry.getValue().isSelected()) participants.add(entry.getKey());
                if (participants.isEmpty()) { formErr.setText("⚠  Select at least one participant"); return; }

                Map<User, Double> inputData = new HashMap<>();
                if (type == SplitType.PERCENTAGE || type == SplitType.SHARE || type == SplitType.ITEM_LEVEL) {
                    for (User u : participants) {
                        JTextField tf = perFields.get(u);
                        if (tf == null) { formErr.setText("⚠  Enter values for all participants"); return; }
                        inputData.put(u, Double.parseDouble(tf.getText().trim()));
                    }
                }

                // Edit  = delete old + create new
                if (existing != null) {
                    currentGroup.getExpenses().remove(existing);
                    expenseManager.deleteExpense(existing);
                }

                boolean ok = expenseManager.createExpense(
                        amount, currentUser, participants, type, inputData, desc, currentGroup.getId());
                if (ok) {
                    List<Expense> all = expenseManager.listAllExpenses();
                    Expense newExp = all.get(all.size() - 1);
                    newExp.setRecurring(recurringChk.isSelected());
                    currentGroup.addExpense(newExp);
                    refreshGroupView();
                    dlg.dispose();
                } else {
                    formErr.setText("⚠  Failed to create expense. Check your values.");
                }
            } catch (NumberFormatException ex) {
                formErr.setText("⚠  Invalid number format");
            } catch (Exception ex) {
                formErr.setText("⚠  " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // ==========================================================================
    //  DIALOG — SETTLE UP
    // ==========================================================================
    private void showSettlementDialog() {
        JDialog dlg = new JDialog(this, "Settle Up — " + currentGroup.getName(), true);
        dlg.setSize(600, 440);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        dlg.setLayout(new BorderLayout());

        dlg.add(styledDialogHeader("✓  Settle Up — " + currentGroup.getName(), SUCCESS), BorderLayout.NORTH);

        List<DebtSimplifier.Settlement> settlements = debtSimplifier.simplifyDebts(currentGroup);

        if (settlements.isEmpty()) {
            JLabel empty = new JLabel("🎉   Everyone is settled up!");
            empty.setFont(FONT_TITLE);
            empty.setForeground(SUCCESS);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            dlg.add(empty, BorderLayout.CENTER);
        } else {
            String[] cols = {"Payer", "→", "Payee", "Amount", "Action"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (DebtSimplifier.Settlement s : settlements) {
                model.addRow(new Object[]{
                        s.payer.getName(), "→", s.payee.getName(),
                        String.format("₹ %.2f", s.amount), "Settle"
                });
            }

            JTable table = new JTable(model);
            table.setBackground(BG_CARD);
            table.setForeground(TEXT_PRI);
            table.setFont(FONT_BODY);
            table.setRowHeight(40);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setSelectionBackground(BG_ITEM);
            table.setFillsViewportHeight(true);

            table.getTableHeader().setBackground(BG_ITEM);
            table.getTableHeader().setForeground(TEXT_MUTED);
            table.getTableHeader().setFont(FONT_BOLD);

            // Alternate row colors
            DefaultTableCellRenderer altRenderer = new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    setBackground(row % 2 == 0 ? BG_CARD : BG_ITEM);
                    setForeground(TEXT_PRI);
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    return this;
                }
            };
            for (int i = 0; i < table.getColumnCount() - 1; i++)
                table.getColumnModel().getColumn(i).setCellRenderer(altRenderer);

            // "Settle" button column
            table.getColumnModel().getColumn(4).setCellRenderer(
                    (t, val, sel, foc, row, col) -> createSmallBtn("Settle", SUCCESS));

            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent ev) {
                    int row = table.rowAtPoint(ev.getPoint());
                    int col = table.columnAtPoint(ev.getPoint());
                    if (col == 4 && row >= 0 && row < settlements.size()) {
                        DebtSimplifier.Settlement s = settlements.get(row);
                        settleTransaction(s);
                        refreshGroupView();
                        dlg.dispose();
                        JOptionPane.showMessageDialog(ExpenseSplitterGUI.this,
                                "<html><b>" + s.payer.getName() + "</b> settled <b>₹"
                                + String.format("%.2f", s.amount)
                                + "</b> with <b>" + s.payee.getName() + "</b></html>",
                                "Settled!", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(BG_CARD);
            dlg.add(scroll, BorderLayout.CENTER);
        }

        JButton closeBtn = createPrimaryBtn("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel();
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(12, 28, 16, 28));
        footer.add(closeBtn);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    /**
     * Marks SplitDetail entries as PAID (status=true) for the given settlement.
     *
     * Strategy: iterate group expenses where the expense's payer (paidTo) is the
     * settlement's creditor (payee).  Within those, mark the debtor's
     * SplitDetail as paid — up to the settlement amount to avoid over-settling.
     *
     * Also marks matching entries in the debtor's personal split history.
     */
    private void settleTransaction(DebtSimplifier.Settlement s) {
        double remaining = s.amount;

        for (Expense exp : currentGroup.getExpenses()) {
            if (remaining <= 0.001) break;
            // This expense's payer (creditor) must match the settlement payee
            if (!exp.getPaidTo().equals(s.payee)) continue;

            for (SplitDetail detail : exp.getPaidBy()) {
                if (remaining <= 0.001) break;
                if (detail.getUser().equals(s.payer) && !detail.getStatus()) {
                    detail.setStatus(true);
                    remaining -= detail.getAmount();
                }
            }
        }

        // Mirror in the payer's personal split history
        for (SplitDetail sd : s.payer.getSplitHistory()) {
            if (!sd.getStatus()) sd.setStatus(true);
        }
    }

    // ==========================================================================
    //  DIALOG — PROFILE
    // ==========================================================================
    private void showProfileDialog() {
        JDialog dlg = new JDialog(this, "My Profile", true);
        dlg.setSize(480, 540);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        dlg.setLayout(new BorderLayout());

        dlg.add(styledDialogHeader("👤  My Profile", ACCENT2), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(BG_DARK);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel infoLbl = new JLabel(
                "<html><span style='color:#EAEAEA'><b>Name:</b>  " + currentUser.getName()
                + "</span><br><span style='color:#8892B0'><b>Email:</b>  " + currentUser.getEmail()
                + "</span></html>");
        infoLbl.setFont(FONT_BODY);
        infoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField  nameField   = mkField(false);
        JTextField  emailField  = mkField(false);
        JPasswordField oldPass  = (JPasswordField) mkField(true);
        JPasswordField newPass  = (JPasswordField) mkField(true);
        JLabel profileErr = errorLabel();
        profileErr.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton updateNameBtn  = createSmallBtn("Update Name",     ACCENT2);
        JButton updateEmailBtn = createSmallBtn("Update Email",    ACCENT2);
        JButton changePassBtn  = createSmallBtn("Change Password", ACCENT2);
        JButton clearHistBtn   = createSmallBtn("Clear Split History", WARN);
        JButton deleteAccBtn   = createSmallBtn("🗑  Delete Account",  ACCENT);

        updateNameBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { profileErr.setText("⚠  Enter a name"); return; }
            if (userManager.updateName(currentUser, name)) {
                dashboardWelcomeLabel.setText("Hello, " + currentUser.getName().split(" ")[0] + " 👋");
                profileErr.setForeground(SUCCESS); profileErr.setText("✓  Name updated!");
            } else { profileErr.setText("⚠  Update failed"); }
        });

        updateEmailBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) { profileErr.setText("⚠  Enter an email"); return; }
            if (userManager.updateEmail(currentUser, email)) {
                profileErr.setForeground(SUCCESS); profileErr.setText("✓  Email updated!");
            } else { profileErr.setText("⚠  Update failed"); }
        });

        changePassBtn.addActionListener(e -> {
            String op = new String(oldPass.getPassword());
            String np = new String(newPass.getPassword());
            if (!currentUser.getPassword().equals(op)) { profileErr.setText("⚠  Incorrect old password"); return; }
            if (np.isEmpty())                          { profileErr.setText("⚠  New password cannot be empty"); return; }
            if (userManager.changePassword(currentUser, np)) {
                profileErr.setForeground(SUCCESS); profileErr.setText("✓  Password changed!");
            } else { profileErr.setText("⚠  Failed"); }
        });

        clearHistBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(dlg, "Clear all split history?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION && userManager.clearHistory(currentUser)) {
                profileErr.setForeground(SUCCESS); profileErr.setText("✓  History cleared");
            }
        });

        deleteAccBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(dlg,
                    "Permanently delete your account? This cannot be undone.",
                    "⚠  Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION && userManager.deleteUser(currentUser)) {
                dlg.dispose();
                currentUser = null; currentGroup = null;
                cardLayout.show(mainPanel, "Login");
            }
        });

        body.add(infoLbl);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(fieldGroup("New Name", nameField));
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(leftWrap(updateNameBtn));
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(fieldGroup("New Email", emailField));
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(leftWrap(updateEmailBtn));
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(fieldGroup("Current Password", oldPass));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(fieldGroup("New Password", newPass));
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(leftWrap(changePassBtn));
        body.add(profileErr);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(leftWrap(clearHistBtn));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(leftWrap(deleteAccBtn));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        dlg.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = createSidebarBtn("Done");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel();
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(8, 28, 14, 28));
        footer.add(closeBtn);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ==========================================================================
    //  DIALOG — MANAGE GROUP
    // ==========================================================================
    private void showManageGroupDialog() {
        JDialog dlg = new JDialog(this, "Manage Group — " + currentGroup.getName(), true);
        dlg.setSize(440, 460);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        dlg.setLayout(new BorderLayout());

        dlg.add(styledDialogHeader("⚙  Manage Group", BG_ITEM), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(BG_DARK);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Rename
        JTextField renameField = mkField(false);
        JLabel      renameErr  = errorLabel();
        JButton     renameBtn  = createSmallBtn("Rename",  ACCENT2);
        renameBtn.addActionListener(e -> {
            String n = renameField.getText().trim();
            if (n.isEmpty()) { renameErr.setText("⚠  Enter a name"); return; }
            currentGroup.setName(n);
            groupTitleLabel.setText(n);
            dlg.setTitle("Manage Group — " + n);
            renameErr.setForeground(SUCCESS);
            renameErr.setText("✓  Renamed to \"" + n + "\"");
        });

        // Members list
        JLabel membersLbl = new JLabel("Current Members");
        membersLbl.setFont(FONT_H2);
        membersLbl.setForeground(TEXT_PRI);
        membersLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel membersList = new JPanel();
        membersList.setBackground(BG_DARK);
        membersList.setLayout(new BoxLayout(membersList, BoxLayout.Y_AXIS));
        for (User u : currentGroup.getMembers()) {
            JPanel r = new JPanel(new BorderLayout());
            r.setBackground(BG_CARD);
            r.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_CLR, 1), new EmptyBorder(8, 12, 8, 12)));
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            JLabel nameLbl = new JLabel(u.getName() + (u.equals(currentUser) ? "  (you)" : ""));
            nameLbl.setForeground(TEXT_PRI);
            nameLbl.setFont(FONT_BODY);
            r.add(nameLbl, BorderLayout.WEST);
            membersList.add(r);
            membersList.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        // Add member
        JTextField addEmailField = mkField(false);
        JLabel     addErr        = errorLabel();
        JButton    addBtn        = createSmallBtn("+ Add Member", SUCCESS);
        addBtn.addActionListener(e -> {
            String email = addEmailField.getText().trim();
            User u = userManager.findByEmail(email);
            if (u == null) { addErr.setText("⚠  User not found"); return; }
            groupManager.addMember(currentGroup.getId(), u);
            addErr.setForeground(SUCCESS);
            addErr.setText("✓  Added " + u.getName());
            addEmailField.setText("");
        });

        body.add(fieldGroup("Rename Group", renameField));
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(leftWrap(renameBtn));
        body.add(renameErr);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(membersLbl);
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(membersList);
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(fieldGroup("Add Member by Email", addEmailField));
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(leftWrap(addBtn));
        body.add(addErr);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        dlg.add(scroll, BorderLayout.CENTER);

        JButton doneBtn = createSidebarBtn("Done");
        doneBtn.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel();
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(8, 24, 14, 24));
        footer.add(doneBtn);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ==========================================================================
    //  DESIGN HELPERS
    // ==========================================================================

    /** Creates a JTextField or JPasswordField with the dark theme applied. */
    private JTextField mkField(boolean password) {
        JTextField f = password ? new JPasswordField() : new JTextField();
        f.setBackground(BG_ITEM);
        f.setForeground(TEXT_PRI);
        f.setCaretColor(TEXT_PRI);
        f.setFont(FONT_BODY);
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return f;
    }

    /** Label + field stacked vertically in a transparent wrapper. */
    private JPanel fieldGroup(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(field);
        return p;
    }

    /** Label + arbitrary component (e.g. JComboBox). */
    private JPanel labelGroup(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(comp);
        return p;
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(FONT_SMALL);
        l.setForeground(ACCENT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel statCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(BG_ITEM);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                new EmptyBorder(10, 14, 10, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelL = new JLabel(label);
        labelL.setFont(FONT_SMALL);
        labelL.setForeground(TEXT_MUTED);
        JLabel valueL = new JLabel(value);
        valueL.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueL.setForeground(valueColor);
        card.add(labelL, BorderLayout.NORTH);
        card.add(valueL, BorderLayout.CENTER);
        return card;
    }

    private JPanel styledDialogHeader(String title, Color bg) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(bg);
        h.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel l = new JLabel(title);
        l.setFont(FONT_H2);
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);
        return h;
    }

    private JPanel cardForm(Color bg) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }

    private JPanel leftWrap(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(comp);
        return p;
    }

    private String showInputPrompt(String title, String prompt) {
        JTextField field = mkField(false);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(BG_DARK);
        JLabel l = new JLabel(prompt);
        l.setForeground(TEXT_PRI);
        l.setFont(FONT_BODY);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        int res = JOptionPane.showConfirmDialog(this, p, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return (res == JOptionPane.OK_OPTION) ? field.getText() : null;
    }

    // ------------------------------------------------------------Button factories ------------------------------------------------------------

    private JButton createPrimaryBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 20, 12, 20));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtnHover(b, ACCENT, new Color(0xC73652));
        return b;
    }

    private JButton createAccentBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        addBtnHover(b, ACCENT, new Color(0xC73652));
        return b;
    }

    private JButton createSmallBtn(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_SMALL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        addBtnHover(b, color, color.brighter());
        return b;
    }

    private JButton createSidebarBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(BG_ITEM);
        b.setForeground(TEXT_PRI);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        addBtnHover(b, BG_ITEM, ACCENT.darker());
        return b;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton b = new JButton(text);
        styleTab(b, active);
        return b;
    }

    private void styleTab(JButton b, boolean active) {
        b.setBackground(active ? ACCENT : BG_ITEM);
        b.setForeground(active ? Color.WHITE : TEXT_MUTED);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 0, 8, 0));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(BG_ITEM);
        combo.setForeground(TEXT_PRI);
        combo.setFont(FONT_BODY);
    }

    private void addBtnHover(JButton b, Color normal, Color hover) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover);  }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(normal); }
        });
    }

    private void addPanelHover(JPanel p, Color normal, Color hover) {
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { p.setBackground(hover);  }
            @Override public void mouseExited (MouseEvent e) { p.setBackground(normal); }
        });
    }

    // ==========================================================================
    //  ENTRY POINT
    // ==========================================================================
    public static void launch() {
        SwingUtilities.invokeLater(() -> new ExpenseSplitterGUI().setVisible(true));
    }
}
