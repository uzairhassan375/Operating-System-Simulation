import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.concurrent.Semaphore;

interface IPCRemote extends Remote {
    void sendMessage(String message) throws RemoteException;
    String receiveMessage() throws RemoteException;
}

class IPCRemoteImpl extends UnicastRemoteObject implements IPCRemote {
    private String message;

    protected IPCRemoteImpl() throws RemoteException {
        super();
        message = "";
    }

    @Override
    public synchronized void sendMessage(String message) throws RemoteException {
        this.message = message;
    }

    @Override
    public synchronized String receiveMessage() throws RemoteException {
        return message;
    }
}

public class SynchronizationIPC extends JFrame {
    private static final Color BUTTON_COLOR = new Color(25, 25, 112); // Light Midnight Blue
    private static final Color BUTTON_HOVER_COLOR = new Color(20, 20, 90); // Darker Midnight Blue

    private JTextArea logArea;
    private JButton addProcessButton;
    private JButton sharedMemoryButton;
    private JButton stopSimulationButton;
    private JButton socketCommunicationButton;
    private JButton rmiCommunicationButton;
    private JButton goBackButton;
    private Semaphore semaphore;
    private int processCount;
    private boolean isSimulationRunning;
    private IPCRemote ipcRemote;

    public SynchronizationIPC() {
        semaphore = new Semaphore(1);
        processCount = 0;
        isSimulationRunning = false;

        // Create the frame
        setTitle("Synchronization and IPC");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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

        JPanel leftPanel = createLeftPanelForSynchronization();
        leftPanel.setOpaque(false);
        leftPanel.setBackground(new Color(255, 255, 255, 100)); // Low opacity

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        logArea.setOpaque(false);
        logArea.setForeground(Color.WHITE);

        JScrollPane rightPanel = new JScrollPane(logArea);
        rightPanel.setOpaque(false);
        rightPanel.getViewport().setOpaque(false);
        rightPanel.setBackground(new Color(255, 255, 255, 100)); // Low opacity
        rightPanel.setForeground(Color.WHITE);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        backgroundPanel.add(splitPane, BorderLayout.CENTER);
        add(backgroundPanel);

        // Initialize RMI
        try {
            ipcRemote = new IPCRemoteImpl();
            Registry registry = LocateRegistry.createRegistry(1099);  // Local server for RMI
            registry.rebind("IPCRemote", ipcRemote);  // Register the remote object
            log("RMI Registry started on port 1099.");
        } catch (Exception e) {
            log("Error starting RMI: " + e.getMessage());
        }
    }

    private JPanel createLeftPanelForSynchronization() {
        JPanel leftPanel = new JPanel(new GridLayout(8, 1, 5, 5)); // Increased rows to 8 for new buttons
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create "Go Back" button
        goBackButton = createStyledButton("Go Back", e -> {
            dispose();
            // Navigate back to previous window (you can add your own code here)
        });

        // Add all buttons to the left panel
        addProcessButton = createStyledButton("Add Process", e -> addProcess());
        socketCommunicationButton = createStyledButton("Start Socket Communication", e -> startSocketCommunication());
        rmiCommunicationButton = createStyledButton("Start RMI Communication", e -> startRmiCommunication());

        leftPanel.add(goBackButton);
        leftPanel.add(addProcessButton);
        leftPanel.add(socketCommunicationButton);
        leftPanel.add(rmiCommunicationButton);

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

    private void addProcess() {
        processCount++;
        String processName = "Process " + processCount;
        new Thread(() -> accessSharedResource(processName)).start();
    }

    private void accessSharedResource(String processName) {
        try {
            log(processName + " is waiting to access the critical section...");
            semaphore.acquire();
            log(processName + " has entered the critical section.");

            Thread.sleep(2000);

            log(processName + " is leaving the critical section.");

            ipcRemote.sendMessage(processName + " completed its task.");
            log("RMI Message Sent: " + processName + " completed its task.");

            // Send a message via socket after completing the task
            sendMessageViaSocket(processName + " completed its task.");

        } catch (InterruptedException | RemoteException e) {
            Thread.currentThread().interrupt();
            log("Error: " + e.getMessage());
        } finally {
            semaphore.release(); // Release the semaphore
            log(processName + " has released the resource.");
        }
    }

    private void sendMessageViaSocket(String message) {
        try (Socket socket = new Socket("localhost", 8080);  // Connect to the server running on localhost at port 8080
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Create output stream to send data
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { // Input stream to read the response

            out.println(message);
            String response = in.readLine();
            log("Socket server responded: " + response);
        } catch (IOException e) {
            log("Error in socket communication: " + e.getMessage());
        }
    }

    private void startSocketCommunication() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                System.out.println("Socket server started on port 8080.");
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                        String received = in.readLine();
                        System.out.println("Socket Received: " + received);
                        out.println("Acknowledged: " + received);
                    }
                }
            } catch (IOException e) {
                log("Error in socket communication: " + e.getMessage());
            }
        }).start();
    }

    private void startRmiCommunication() {
        try {
            String serverIp = "192.168.1.100"; // Change this to the server machine IP address
            Registry registry = LocateRegistry.getRegistry(serverIp, 1099);  // Connect to the server's RMI registry
            ipcRemote = (IPCRemote) registry.lookup("IPCRemote");
            ipcRemote.sendMessage("RMI Communication Started");
            log("RMI Communication started.");
        } catch (Exception e) {
            log("Error in RMI communication: " + e.getMessage());
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SynchronizationIPC app = new SynchronizationIPC();
            app.setVisible(true);
        });
    }
}
