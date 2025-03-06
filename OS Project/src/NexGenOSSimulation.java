import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import Process.*;
import Memory.*;

public class NexGenOSSimulation {
    private Map<Integer, PCB> processTable;
    private int processIDCounter = 1;
    private Queue<PCB> readyQueue;
    private Queue<PCB> blockedQueue;
    private Queue<PCB> newQueue;

    private MemoryManager memoryManager; // Memory Manager to handle paging
    private LRUPageReplacement lruPageReplacement;

    public NexGenOSSimulation() {
        processTable = new HashMap<>();
        readyQueue = new LinkedList<>();
        blockedQueue = new LinkedList<>();
        newQueue = new LinkedList<>();

        memoryManager = new MemoryManager("config.txt");
        lruPageReplacement = new LRUPageReplacement(3);
    }

    public void addNewProcess(PCB process) {
        newQueue.add(process);
    }

    public void mediumLevelScheduling() {
        while (!newQueue.isEmpty()) {
            PCB process = newQueue.poll();
            process.setState("Ready");
            readyQueue.add(process);
        }
    }

    public PCB lowLevelScheduling(String algorithm) {
        if (readyQueue.isEmpty()) {
            return null;
        }

        PCB selectedProcess = null;

        if (algorithm.equals("FCFS")) {
            // For FCFS, simply poll the first process
            selectedProcess = readyQueue.poll();
        } else if (algorithm.equals("SJF")) {
            // For SJF, find the process with the shortest burst time
            selectedProcess = readyQueue.stream()
                    .min(Comparator.comparingInt(PCB::getBurstTime))
                    .orElse(null);

            // If a process is found, remove it from the queue
            if (selectedProcess != null) {
                readyQueue.remove(selectedProcess);
            }
        }

        return selectedProcess;
    }

    public void createProcess(int burstTime, int arrivalTime, int priority) {
        int processID = processIDCounter++;
        PCB process = new PCB(processID, "Ready", "User", priority, burstTime, arrivalTime);
        processTable.put(processID, process);

        // Allocate memory to the process
        memoryManager.allocateMemoryToProcess(processID); // Allocating memory based on size

        readyQueue.add(process);
    }

    public void destroyProcess(int processID) {
        processTable.remove(processID);
        readyQueue.removeIf(p -> p.getProcessID() == processID);
        blockedQueue.removeIf(p -> p.getProcessID() == processID);
    }

    public void suspendProcess(int processID) {
        PCB process = processTable.get(processID);
        if (process != null && readyQueue.remove(process)) {
            process.setState("Suspended");
            blockedQueue.add(process);
        }
    }

    public void resumeProcess(int processID) {
        PCB process = processTable.get(processID);
        if (process != null && blockedQueue.remove(process)) {
            process.setState("Ready");
            readyQueue.add(process);
        }
    }

    public void blockProcess(int processID) {
        PCB process = processTable.get(processID);
        if (process != null && readyQueue.remove(process)) {
            process.setState("Blocked");
            blockedQueue.add(process);
        }
    }

    public void wakeupProcess(int processID) {
        PCB process = processTable.get(processID);
        if (process != null && blockedQueue.remove(process)) {
            process.setState("Ready");
            readyQueue.add(process);
        }
    }

    public void changePriority(int processID, int newPriority) {
        PCB process = processTable.get(processID);
        if (process != null) {
            process.setPriority(newPriority);
        }
    }

    public Map<Integer, PCB> getProcessTable() {
        return processTable;
    }

    public Queue<PCB> getReadyQueue() {
        return readyQueue;
    }

    public Queue<PCB> getBlockedQueue() {
        return blockedQueue;
    }

    private void simulatePageAccess(PCB process) {
        int memorySize = process.getBurstTime(); // Using burst time as a placeholder for memory size
        int numPages = (int) Math.ceil((double) memorySize / memoryManager.getPageSize());

        for (int i = 0; i < numPages; i++) {
            lruPageReplacement.accessPage(i);  // Access each page (simulate memory access)
        }
    }
}
