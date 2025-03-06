package Memory;

import java.io.*;
import java.util.*;

public class MemoryManager {
    private Map<Integer, ProcessMemory> processMemoryMap;
    private int pageSize;
    private int totalMemory;
    private LRUPageReplacement lruPageReplacement;

    public MemoryManager(String configFilePath) {
        processMemoryMap = new HashMap<>();
        this.pageSize = loadPageSize(configFilePath);
        this.totalMemory = loadTotalMemory(configFilePath);
        this.lruPageReplacement = new LRUPageReplacement(totalMemory / pageSize);
    }

    public int loadPageSize(String configFilePath) {
        int defaultPageSize = 4096;
        try {
            File configFile = new File(configFilePath);
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
            System.err.println("Error reading config file. Using default value.");
        }
        return defaultPageSize;
    }

    private int loadTotalMemory(String configFilePath) {
        int defaultTotalMemory = 1024;
        try {
            File configFile = new File(configFilePath);
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
            System.err.println("Error reading config file. Using default value.");
        }
        return defaultTotalMemory;
    }

    public void allocateMemoryToProcess(int processID) {
        ProcessMemory processMemory = new ProcessMemory(processID, pageSize);
        processMemoryMap.put(processID, processMemory);


        for (Page page : processMemory.getPages()) {
            lruPageReplacement.accessPage(page.pageNumber);
        }
    }

    public int getPageSize() {
        return pageSize;
    }

}
