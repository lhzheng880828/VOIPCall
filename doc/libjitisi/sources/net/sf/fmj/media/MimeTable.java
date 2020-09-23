package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class MimeTable {
    private static final Hashtable<String, String> reverseHashTable = new Hashtable();
    private final Hashtable<String, String> hashTable = new Hashtable();

    public boolean addMimeType(String fileExtension, String mimeType) {
        this.hashTable.put(fileExtension, mimeType);
        reverseHashTable.put(mimeType, fileExtension);
        return true;
    }

    public void clear() {
        this.hashTable.clear();
        reverseHashTable.clear();
    }

    public String getDefaultExtension(String mimeType) {
        return (String) reverseHashTable.get(mimeType);
    }

    public List<String> getExtensions(String mimeType) {
        List<String> result = new ArrayList();
        for (String k : this.hashTable.keySet()) {
            if (((String) this.hashTable.get(k)).equals(mimeType)) {
                result.add(k);
            }
        }
        return result;
    }

    public Hashtable<String, String> getMimeTable() {
        Hashtable<String, String> result = new Hashtable();
        result.putAll(this.hashTable);
        return result;
    }

    public String getMimeType(String fileExtension) {
        return (String) this.hashTable.get(fileExtension);
    }

    public Set<String> getMimeTypes() {
        Set<String> result = new HashSet();
        for (Object add : this.hashTable.values()) {
            result.add(add);
        }
        return result;
    }

    public boolean removeMimeType(String fileExtension) {
        if (this.hashTable.get(fileExtension) == null) {
            return false;
        }
        reverseHashTable.remove(this.hashTable.get(fileExtension));
        this.hashTable.remove(fileExtension);
        return true;
    }
}
