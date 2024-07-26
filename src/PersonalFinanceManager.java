import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;

public class PersonalFinanceManager extends JFrame {
    private Connection connection;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> typeComboBox;
    private JTextArea summaryArea;

    public PersonalFinanceManager() {
        // Initialize Database Connection
        initializeDB();

        // Setup Frame
        setTitle("Personal Finance Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel typeLabel = new JLabel("Type:");
        typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField();

        inputPanel.add(typeLabel);
        inputPanel.add(typeComboBox);
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new AddButtonListener());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ClearButtonListener());

        inputPanel.add(clearButton);
        inputPanel.add(addButton);

        // Summary Area
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(summaryArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Display summary on startup
        updateSummary();

        setVisible(true);
    }

    private void initializeDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/finance_db", "root", "@#aditya2006");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTransaction(String type, double amount, String description) {
        try {
            String query = "insert into transactions (type, amount, description, date) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, type);
            stmt.setDouble(2, amount);
            stmt.setString(3, description);
            stmt.setDate(4, new Date(System.currentTimeMillis()));
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearTransactions() {
        try {
            String query = "delete from transactions";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            JOptionPane.showMessageDialog(this, "All transactions have been cleared.", "Cleared", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error clearing transactions.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        try {
            String query = "select * from transactions";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            StringBuilder summary = new StringBuilder("Summary:\n\n");
            double totalIncome = 0;
            double totalExpense = 0;

            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                Date date = rs.getDate("date");

                summary.append(String.format("%-10s %-10.2f %-20s %-10s\n", type, amount, description, date.toString()));

                if (type.equals("Income")) {
                    totalIncome += amount;
                } else {
                    totalExpense += amount;
                }
            }

            summary.append("\nTotal Income: ").append(totalIncome);
            summary.append("\nTotal Expense: ").append(totalExpense);
            summary.append("\nNet Balance: ").append(totalIncome - totalExpense);

            summaryArea.setText(summary.toString());

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String type = (String) typeComboBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();

            addTransaction(type, amount, description);
            updateSummary();

            amountField.setText("");
            descriptionField.setText("");
        }
    }

    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearTransactions();
            updateSummary();
        }
    }

    public static void main(String[] args) {
        PersonalFinanceManager money = new PersonalFinanceManager();
    }
}
