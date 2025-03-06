package Process;

import javax.swing.*;
import java.util.*;

public class Scheduler {

    public static PCB performFCFS(Queue<PCB> readyQueue, Map<Integer, PCB> processTable, JFrame frame) {
        if (readyQueue.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Ready Queue is empty.");
            return null;
        }

        PCB process = readyQueue.poll();
        process.setState("Running");
        JOptionPane.showMessageDialog(frame, "Running Process: " + process.getProcessID());
        process.setState("Completed");
        processTable.remove(process.getProcessID());
        return process;
    }

    public static PCB performSJF(Queue<PCB> readyQueue, Map<Integer, PCB> processTable, JFrame frame) {
        if (readyQueue.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Ready Queue is empty.");
            return null;
        }

        PCB shortestProcess = readyQueue.stream()
                .min(Comparator.comparingInt(PCB::getBurstTime))
                .orElse(null);

        if (shortestProcess != null) {
            readyQueue.remove(shortestProcess);
            shortestProcess.setState("Running");
            JOptionPane.showMessageDialog(frame, "Running Process: " + shortestProcess.getProcessID());
            shortestProcess.setState("Completed");
            processTable.remove(shortestProcess.getProcessID());
            return shortestProcess;
        }
        return null;
    }
}
