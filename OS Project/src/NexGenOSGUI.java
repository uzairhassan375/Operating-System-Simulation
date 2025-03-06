import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.table.TableCellRenderer;


import Process.*;

public class NexGenOSGUI extends JFrame {
    private NexGenOSSimulation simulation;
    private JTable pcbTable;
    private DefaultTableModel tableModel;
    private JFrame mainFrame;

    // Define Color Palette
    private static final Color BUTTON_COLOR = new Color(25, 25, 112); // Light Midnight Blue
    private static final Color BUTTON_HOVER_COLOR = new Color(20, 20, 90); // Darker Midnight Blue
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240); // Light Grey
    private static final Color TEXT_COLOR = new Color(33, 33, 33); // Dark Grey

    public NexGenOSGUI(NexGenOSSimulation simulation) {
        this.simulation = simulation;
        setupHomePage();
    }

    private void setupHomePage() {
        mainFrame = new JFrame("NexGen OS");
        mainFrame.setUndecorated(true); // Remove top bar (window decorations)
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen

        // Custom JPanel with background image
        JPanel mainPanel = new JPanel() {
            private Image backgroundImage = new ImageIcon(getClass().getResource("/images/bg.jpg")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Add heading
        JLabel headingLabel = new JLabel("NexGen", JLabel.CENTER);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        headingLabel.setForeground(Color.WHITE);
        headingLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding for the heading
        mainPanel.add(headingLabel, BorderLayout.NORTH);

        // Buttons section with a 2x2 grid
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100)); // Padding around the grid
        buttonGrid.setOpaque(false); // Make it transparent for the background image

        // Create Buttons
        JButton processManagementBtn = createStyledButton("Process Management", e -> setupProcessManagementScreen());
        JButton memoryManagementBtn = createStyledButton("Memory Management", e -> setupMemoryManagementScreen());
        JButton ioManagementBtn = createStyledButton("I/O Management", e -> showNotImplementedMessage("I/O Management"));
        JButton otherOperationsBtn = createStyledButton("Other Operations", e -> openSynchronizationWindow());

        // Add buttons to the grid
        buttonGrid.add(processManagementBtn);
        buttonGrid.add(memoryManagementBtn);
        buttonGrid.add(ioManagementBtn);
        buttonGrid.add(otherOperationsBtn);

        // Add the button grid to the center of the panel
        mainPanel.add(buttonGrid, BorderLayout.CENTER);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }


    private void setupMemoryManagementScreen() {
        mainFrame.dispose();
        new MemoryManagementPanel(simulation); // Open the memory management window
    }

    private void openSynchronizationWindow() {
        // Check if an instance is already open, or create a new instance
        SwingUtilities.invokeLater(() -> {
            SynchronizationIPC syncIPC = new SynchronizationIPC();
            syncIPC.setVisible(true); // Make the window visible
        });
    }

    private void setupProcessManagementScreen() {
        mainFrame.dispose(); // Close the main screen
        JFrame processFrame = new JFrame("Process Management");
        processFrame.setUndecorated(true); // Remove top bar (window decorations)
        processFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        processFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen

        // Create the background panel with image
        JPanel backgroundPanel = new JPanel() {
            private Image backgroundImage = new ImageIcon(getClass().getResource("/images/bg.jpg")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this); // Draw the background image
            }
        };

        backgroundPanel.setLayout(new BorderLayout());

        // Set up the JSplitPane with transparent panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(0); // Hide the divider
        splitPane.setEnabled(false); // Disable divider resizing
        splitPane.setOpaque(false); // Make the split pane transparent
        splitPane.setLeftComponent(createLeftPanel(processFrame));
        splitPane.setRightComponent(createRightPanel());

        // Add the split pane to the background panel
        backgroundPanel.add(splitPane, BorderLayout.CENTER);

        // Add the background panel to the frame
        processFrame.add(backgroundPanel);
        processFrame.setVisible(true);
    }

    private JPanel createLeftPanel(JFrame processFrame) {
        JPanel leftPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setOpaque(false); // Make the left panel transparent

        // Create "Go Back" button with text and style it like other buttons
        JButton goBackButton = createStyledButton("Go Back", e -> {
            processFrame.dispose();
            setupHomePage();
        });
        leftPanel.add(goBackButton);

        JButton createProcessBtn = createStyledButton("Create Process", e -> createProcess());
        JButton destroyProcessBtn = createStyledButton("Destroy Process", e -> destroyProcess());
        JButton suspendProcessBtn = createStyledButton("Suspend Process", e -> suspendProcess());
        JButton resumeProcessBtn = createStyledButton("Resume Process", e -> resumeProcess());
        JButton blockProcessBtn = createStyledButton("Block Process", e -> blockProcess());
        JButton wakeupProcessBtn = createStyledButton("Wakeup Process", e -> wakeupProcess());
        JButton dispatchProcessBtn = createStyledButton("Dispatch Process", e -> dispatchAllProcesses());
        JButton changePriorityBtn = createStyledButton("Change Priority", e -> changePriority());

        leftPanel.add(createProcessBtn);
        leftPanel.add(destroyProcessBtn);
        leftPanel.add(suspendProcessBtn);
        leftPanel.add(resumeProcessBtn);
        leftPanel.add(blockProcessBtn);
        leftPanel.add(wakeupProcessBtn);
        leftPanel.add(dispatchProcessBtn);
        leftPanel.add(changePriorityBtn);

        return leftPanel;
    }

    private JScrollPane createRightPanel() {
        String[] columnNames = {"Process ID", "State", "Owner", "Priority", "Burst Time", "Arrival Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        pcbTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(new Color(0, 0, 10));
                }
                return c;
            }
        };

        pcbTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pcbTable.setRowHeight(25);
        pcbTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pcbTable.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        pcbTable.setForeground(Color.WHITE); // Set table text color to white for contrast
        pcbTable.getTableHeader().setBackground(new Color(200, 200, 200)); // Light gray header
        pcbTable.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(pcbTable);

        // Make the JScrollPane transparent and keep the background image
        scrollPane.setBackground(new Color(0, 0, 0, 0)); // Transparent background for JScrollPane
        scrollPane.setOpaque(false); // Set JScrollPane to be transparent
        scrollPane.getViewport().setOpaque(false); // Ensure the viewport is also transparent

        return scrollPane;
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false); // Remove focus outline
        button.setBorderPainted(false); // Remove border outline
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR); // Darker color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR); // Default button color
            }
        });
        return button;
    }

    private void createProcess() {
        try {
            String burstTimeStr = JOptionPane.showInputDialog(this, "Enter Burst Time:");
            String arrivalTimeStr = JOptionPane.showInputDialog(this, "Enter Arrival Time:");
            String priorityStr = JOptionPane.showInputDialog(this, "Enter Initial Priority:");

            if (burstTimeStr == null || arrivalTimeStr == null || priorityStr == null) {
                JOptionPane.showMessageDialog(this, "Process creation canceled.");
                return;
            }

            int burstTime = Integer.parseInt(burstTimeStr);
            int arrivalTime = Integer.parseInt(arrivalTimeStr);
            int priority = Integer.parseInt(priorityStr);

            simulation.createProcess(burstTime, arrivalTime, priority);
            JOptionPane.showMessageDialog(this, "Process Created.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Process creation failed.");
        }
    }

    private void destroyProcess() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Destroy:");
        try {
            int processID = Integer.parseInt(idStr);
            simulation.destroyProcess(processID);
            JOptionPane.showMessageDialog(this, "Process with ID: " + processID + " destroyed.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void suspendProcess() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Suspend:");
        try {
            int processID = Integer.parseInt(idStr);
            simulation.suspendProcess(processID);
            JOptionPane.showMessageDialog(this, "Process with ID: " + processID + " suspended.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void resumeProcess() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Resume:");
        try {
            int processID = Integer.parseInt(idStr);
            simulation.resumeProcess(processID);
            JOptionPane.showMessageDialog(this, "Process with ID: " + processID + " resumed.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void blockProcess() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Block:");
        try {
            int processID = Integer.parseInt(idStr);
            simulation.blockProcess(processID);
            JOptionPane.showMessageDialog(this, "Process with ID: " + processID + " blocked.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void wakeupProcess() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Wakeup:");
        try {
            int processID = Integer.parseInt(idStr);
            simulation.wakeupProcess(processID);
            JOptionPane.showMessageDialog(this, "Process with ID: " + processID + " woken up.");
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void dispatchAllProcesses() {
        String[] options = {"FCFS", "SJF"};
        int choice = JOptionPane.showOptionDialog(this, "Select a scheduling algorithm:", "Dispatch All Processes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == -1) {
            JOptionPane.showMessageDialog(this, "No algorithm selected. Aborting dispatch.");
            return;
        }

        String algorithm = options[choice];

        // Dispatch processes until the ready queue is empty
        while (!simulation.getReadyQueue().isEmpty()) {
            PCB dispatchedProcess = simulation.lowLevelScheduling(algorithm);
            if (dispatchedProcess != null) {
                dispatchedProcess.setState("Running");

                // Simulate process execution
                JOptionPane.showMessageDialog(this, "Dispatching Process ID: " + dispatchedProcess.getProcessID() +
                        " with Burst Time: " + dispatchedProcess.getBurstTime());
                dispatchedProcess.setState("Completed");

                // Remove from process table as it is completed
                simulation.getProcessTable().remove(dispatchedProcess.getProcessID());
            }

            // Refresh the UI to reflect the updated ready queue
            refreshTable();
        }

        JOptionPane.showMessageDialog(this, "All processes have been dispatched using " + algorithm + ".");
    }

    private void changePriority() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Process ID to Change Priority:");
        try {
            int processID = Integer.parseInt(idStr);
            String priorityStr = JOptionPane.showInputDialog(this, "Enter new priority:");
            try {
                int newPriority = Integer.parseInt(priorityStr);
                simulation.changePriority(processID, newPriority);
                JOptionPane.showMessageDialog(this, "Priority of Process " + processID + " changed.");
                refreshTable();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Priority.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Process ID.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (PCB process : simulation.getProcessTable().values()) {
            tableModel.addRow(new Object[]{
                    process.getProcessID(),
                    process.getState(),
                    process.getOwner(),
                    process.getPriority(),
                    process.getBurstTime(),
                    process.getArrivalTime()
            });
        }
    }

    private void showNotImplementedMessage(String feature) {
        JOptionPane.showMessageDialog(this, feature + " functionality not implemented yet.");
    }

    public static void main(String[] args) {
        NexGenOSSimulation simulation = new NexGenOSSimulation();
        SwingUtilities.invokeLater(() -> new NexGenOSGUI(simulation));
    }
}
