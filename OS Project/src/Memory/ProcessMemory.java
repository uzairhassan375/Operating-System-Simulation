package Memory;

import java.util.*;

public class ProcessMemory {
    private int processID;
    private int memorySize;
    private int pageSize;
    private List<Page> pages;

    public ProcessMemory(int processID, int pageSize) {
        this.processID = processID;
        this.memorySize = memorySize;
        this.pageSize = pageSize;
        this.pages = new ArrayList<>();

        int numPages = (int) Math.ceil((double) memorySize / pageSize);
        for (int i = 0; i < numPages; i++) {
            pages.add(new Page(i));
        }
    }

    public List<Page> getPages() {
        return pages;
    }
}
