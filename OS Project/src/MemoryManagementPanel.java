import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class MemoryManagementPanel {

    private static final Color BUTTON_COLOR = new Color(25, 25, 112); // Light Midnight Blue
    private static final Color BUTTON_HOVER_COLOR = new Color(20, 20, 90); // Darker Midnight Blue

    private int pageSize;
    private int totalMemory;
    private LinkedHashMap<Integer, Integer> memoryPages;
    private JTextArea memoryTextArea;
    private int frames;

    public MemoryManagementPanel(NexGenOSSimulation simulation) {
        this.memoryPages = new LinkedHashMap<>(16, 0.75f, true);
        this.pageSize = loadPageSize();
        this.totalMemory = loadTotalMemory();
        this.frames = 3;

        // Create the frame
        JFrame memoryFrame = new JFrame("Memory Management");
        memoryFrame.setUndecorated(true);
        memoryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        memoryFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Create a background panel with an image
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/images/bg.jpg"));
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);
        splitPane.setOpaque(false);

        JPanel leftPanel = createLeftPanelForMemoryManagement(memoryFrame, simulation);
        leftPanel.setOpaque(false);
        leftPanel.setBackground(new Color(255, 255, 255, 100)); // Low opacity

        memoryTextArea = new JTextArea();
        memoryTextArea.setEditable(false);
        memoryTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        memoryTextArea.setOpaque(false);

        JScrollPane rightPanel = new JScrollPane(memoryTextArea);
        rightPanel.setOpaque(false);
        rightPanel.getViewport().setOpaque(false);
        rightPanel.setBackground(new Color(255, 255, 255, 100)); // Low opacity
        rightPanel.setForeground(Color.WHITE);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        backgroundPanel.add(splitPane, BorderLayout.CENTER);
        memoryFrame.add(backgroundPanel);
        memoryFrame.setVisible(true);
    }

    private JPanel createLeftPanelForMemoryManagement(JFrame memoryFrame, NexGenOSSimulation simulation) {
        JPanel leftPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create "Go Back" button with text and style it like other buttons
        JButton goBackButton = createStyledButton("Go Back", e -> {
            memoryFrame.dispose();
            new NexGenOSGUI(simulation);
        });

        leftPanel.add(goBackButton);

        JButton allocateMemoryBtn = createStyledButton("Allocate Memory", e -> allocateMemory(simulation));
        leftPanel.add(allocateMemoryBtn);

        JButton lruReplaceBtn = createStyledButton("Replace Pages (LRU)", e -> replacePageUsingLRU());
        leftPanel.add(lruReplaceBtn);

        return leftPanel;
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.addActionListener(action);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    private int loadPageSize() {
        int defaultPageSize = 4096;
        try {
            File configFile = new File("config.txt");
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("PageSize")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config file. Using default page size.");
        }
        return defaultPageSize;
    }

    private int loadTotalMemory() {
        int defaultTotalMemory = 1024;
        try {
            File configFile = new File("config.txt");
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("TotalMemory")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config file. Using default total memory.");
        }
        return defaultTotalMemory;
    }

    private void updateMemoryDisplay() {
        StringBuilder memoryInfo = new StringBuilder();
        memoryInfo.append("Current Memory Allocation:\n");

        if (memoryPages.isEmpty()) {
            memoryInfo.append("No processes allocated.\n");
        } else {
            for (Map.Entry<Integer, Integer> entry : memoryPages.entrySet()) {
                memoryInfo.append("Page ID: ").append(entry.getKey())
                        .append(" -> Process ID: ").append(entry.getValue()).append("\n");
            }
        }

        memoryInfo.append("\nFrames in Memory: ").append(memoryPages.size()).append("/").append(frames);

        memoryTextArea.setForeground(Color.WHITE);
        memoryTextArea.setText(memoryInfo.toString());
    }

    private void allocateMemory(NexGenOSSimulation simulation) {
        String memorySizeStr = JOptionPane.showInputDialog("Enter Memory Size for Process:");
        if (memorySizeStr == null) return;

        try {
            int memorySize = Integer.parseInt(memorySizeStr);
            int numPagesRequired = (int) Math.ceil((double) memorySize / pageSize);

            Random random = new Random();
            int processID = random.nextInt(10000); // Generate a random process ID.

            for (int i = 0; i < numPagesRequired; i++) {
                int pageID = generatePageID();
                memoryPages.put(pageID, processID); // Assign the random process ID.
            }

            JOptionPane.showMessageDialog(null, "Memory Allocated with " + numPagesRequired + " pages.");
            updateMemoryDisplay();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid memory size input.");
        }
    }

    private void replacePageUsingLRU() {
        if (memoryPages.size() == 0) {
            JOptionPane.showMessageDialog(null, "No pages to replace.");
            return;
        }

        Iterator<Map.Entry<Integer, Integer>> iterator = memoryPages.entrySet().iterator();
        Map.Entry<Integer, Integer> entry = iterator.next();
        iterator.remove();

        JOptionPane.showMessageDialog(null, "Page ID " + entry.getKey() + " replaced using LRU.");
        updateMemoryDisplay();
    }

    private int generatePageID() {
        return new Random().nextInt(10000);
    }
}
