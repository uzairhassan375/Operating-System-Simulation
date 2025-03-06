package Memory;

import java.util.*;

public class LRUPageReplacement {
    private int capacity;
    private LinkedHashMap<Integer, Integer> memory;

    public LRUPageReplacement(int capacity) {
        this.capacity = capacity;
        memory = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    public void accessPage(int pageNumber) {
        if (memory.containsKey(pageNumber)) {
            System.out.println("Page " + pageNumber + " is already in memory.");
        } else {
            if (memory.size() == capacity) {
                Integer lruPage = memory.keySet().iterator().next();
                memory.remove(lruPage);
                System.out.println("Page " + lruPage + " replaced by page " + pageNumber);
            }
            memory.put(pageNumber, pageNumber);
            System.out.println("Page " + pageNumber + " loaded into memory.");
        }
    }

}
