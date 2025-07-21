import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SmartTaskManager extends JFrame {
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField taskField, dateField, priorityField;
    private JButton addButton, deleteButton, completeButton;
    private Connection conn;

    public SmartTaskManager() {
        setTitle("Smart Task Manager");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        taskField = new JTextField(15);
        dateField = new JTextField(10);
        priorityField = new JTextField(8);
        addButton = new JButton("Add Task");
        deleteButton = new JButton("Delete Task");
        completeButton = new JButton("Mark Complete");

        tableModel = new DefaultTableModel(new String[]{"ID", "Task", "Due Date", "Priority", "Status"}, 0);
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityField);
        inputPanel.add(addButton);

        JPanel controlPanel = new JPanel();
        controlPanel.add(completeButton);
        controlPanel.add(deleteButton);

        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        // Database
        connectDatabase();
        loadTasks();

        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        completeButton.addActionListener(e -> completeTask());
    }

    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:tasks.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY, task TEXT, due_date TEXT, priority TEXT, status TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try {
            tableModel.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("task"),
                    rs.getString("due_date"),
                    rs.getString("priority"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTask() {
        String task = taskField.getText();
        String dueDate = dateField.getText();
        String priority = priorityField.getText();
        if (task.isEmpty() || dueDate.isEmpty() || priority.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO tasks (task, due_date, priority, status) VALUES (?, ?, ?, 'Incomplete')");
            ps.setString(1, task);
            ps.setString(2, dueDate);
            ps.setString(3, priority);
            ps.executeUpdate();
            loadTasks();
            taskField.setText("");
            dateField.setText("");
            priorityField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTask() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to delete");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void completeTask() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to mark complete");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET status = 'Complete' WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartTaskManager().setVisible(true));
    }
}
