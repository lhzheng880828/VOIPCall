package net.sf.fmj.media.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

public class Resource {
    static String AUDIO_FORMAT_KEY = "AF.";
    static String AUDIO_HIT_KEY = "AH.";
    static String AUDIO_INPUT_KEY = "AI.";
    static String AUDIO_SIZE_KEY = "ATS";
    static int AUDIO_TBL_SIZE = 40;
    static String MISC_FORMAT_KEY = "MF.";
    static String MISC_HIT_KEY = "MH.";
    static String MISC_INPUT_KEY = "MI.";
    static String MISC_SIZE_KEY = "MTS";
    static int MISC_TBL_SIZE = 10;
    private static final String USERHOME = "user.home";
    static String VIDEO_FORMAT_KEY = "VF.";
    static String VIDEO_HIT_KEY = "VH.";
    static String VIDEO_INPUT_KEY = "VI.";
    static String VIDEO_SIZE_KEY = "VTS";
    static int VIDEO_TBL_SIZE = 20;
    static FormatTable audioFmtTbl = null;
    private static String filename = null;
    static Object fmtTblSync = new Object();
    private static Hashtable hash = null;
    static FormatTable miscFmtTbl = null;
    static boolean needSaving = false;
    private static String userhome = null;
    private static final int versionNumber = 200;
    static FormatTable videoFmtTbl;

    static {
        hash = null;
        userhome = null;
        hash = new Hashtable();
        boolean securityPrivelege = true;
        if (1 != null) {
            try {
                userhome = System.getProperty(USERHOME);
            } catch (Exception e) {
                userhome = null;
                securityPrivelege = false;
            }
        }
        if (userhome == null) {
            securityPrivelege = false;
        }
        InputStream is = null;
        if (securityPrivelege) {
            is = findResourceFile();
            if (is == null) {
            }
        }
        if (!readResource(is)) {
            hash = new Hashtable();
        }
    }

    public static final synchronized boolean commit() throws IOException {
        synchronized (Resource.class) {
        }
        return true;
    }

    public static final synchronized void destroy() {
        synchronized (Resource.class) {
            if (filename != null) {
                try {
                    new File(filename).delete();
                } catch (Throwable th) {
                    filename = null;
                }
            }
        }
        return;
    }

    private static final synchronized InputStream findResourceFile() {
        InputStream ris;
        synchronized (Resource.class) {
            if (userhome == null) {
                ris = null;
            } else {
                try {
                    filename = userhome + File.separator + ".fmj.resource";
                    ris = getResourceStream(new File(filename));
                } catch (Throwable th) {
                    filename = null;
                    ris = null;
                }
            }
        }
        return ris;
    }

    public static final synchronized Object get(String key) {
        Object obj;
        synchronized (Resource.class) {
            if (key != null) {
                obj = hash.get(key);
            } else {
                obj = null;
            }
        }
        return obj;
    }

    public static final Format[] getDB(Format input) {
        Format[] formatArr;
        synchronized (fmtTblSync) {
            if (audioFmtTbl == null) {
                initDB();
            }
            if (input instanceof AudioFormat) {
                formatArr = audioFmtTbl.get(input);
            } else if (input instanceof VideoFormat) {
                formatArr = videoFmtTbl.get(input);
            } else {
                formatArr = miscFmtTbl.get(input);
            }
        }
        return formatArr;
    }

    private static final FileInputStream getResourceStream(File file) throws IOException {
        try {
            if (file.exists()) {
                return new FileInputStream(file.getPath());
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    static final void initDB() {
        synchronized (fmtTblSync) {
            audioFmtTbl = new FormatTable(AUDIO_TBL_SIZE);
            videoFmtTbl = new FormatTable(VIDEO_TBL_SIZE);
            miscFmtTbl = new FormatTable(MISC_TBL_SIZE);
            loadDB();
        }
    }

    private static final void loadDB() {
        synchronized (fmtTblSync) {
            int size;
            int i;
            Object value;
            Object hit;
            Object key = get(AUDIO_SIZE_KEY);
            if (key instanceof Integer) {
                size = ((Integer) key).intValue();
            } else {
                size = 0;
            }
            if (size > AUDIO_TBL_SIZE) {
                System.err.println("Resource file is corrupted");
                size = AUDIO_TBL_SIZE;
            }
            audioFmtTbl.last = size;
            for (i = 0; i < size; i++) {
                key = get(AUDIO_INPUT_KEY + i);
                value = get(AUDIO_FORMAT_KEY + i);
                hit = get(AUDIO_HIT_KEY + i);
                if (!(key instanceof Format) || !(value instanceof Format[]) || !(hit instanceof Integer)) {
                    System.err.println("Resource file is corrupted");
                    audioFmtTbl.last = 0;
                    break;
                }
                audioFmtTbl.keys[i] = (Format) key;
                audioFmtTbl.table[i] = (Format[]) value;
                audioFmtTbl.hits[i] = ((Integer) hit).intValue();
            }
            key = get(VIDEO_SIZE_KEY);
            if (key instanceof Integer) {
                size = ((Integer) key).intValue();
            } else {
                size = 0;
            }
            if (size > VIDEO_TBL_SIZE) {
                System.err.println("Resource file is corrupted");
                size = VIDEO_TBL_SIZE;
            }
            videoFmtTbl.last = size;
            for (i = 0; i < size; i++) {
                key = get(VIDEO_INPUT_KEY + i);
                value = get(VIDEO_FORMAT_KEY + i);
                hit = get(VIDEO_HIT_KEY + i);
                if (!(key instanceof Format) || !(value instanceof Format[]) || !(hit instanceof Integer)) {
                    System.err.println("Resource file is corrupted");
                    videoFmtTbl.last = 0;
                    break;
                }
                videoFmtTbl.keys[i] = (Format) key;
                videoFmtTbl.table[i] = (Format[]) value;
                videoFmtTbl.hits[i] = ((Integer) hit).intValue();
            }
            key = get(MISC_SIZE_KEY);
            if (key instanceof Integer) {
                size = ((Integer) key).intValue();
            } else {
                size = 0;
            }
            if (size > MISC_TBL_SIZE) {
                System.err.println("Resource file is corrupted");
                size = MISC_TBL_SIZE;
            }
            miscFmtTbl.last = size;
            for (i = 0; i < size; i++) {
                key = get(MISC_INPUT_KEY + i);
                value = get(MISC_FORMAT_KEY + i);
                hit = get(MISC_HIT_KEY + i);
                if (!(key instanceof Format) || !(value instanceof Format[]) || !(hit instanceof Integer)) {
                    System.err.println("Resource file is corrupted");
                    miscFmtTbl.last = 0;
                    break;
                }
                miscFmtTbl.keys[i] = (Format) key;
                miscFmtTbl.table[i] = (Format[]) value;
                miscFmtTbl.hits[i] = ((Integer) hit).intValue();
            }
        }
    }

    public static final void purgeDB() {
        synchronized (fmtTblSync) {
            if (audioFmtTbl == null) {
                return;
            }
            audioFmtTbl = new FormatTable(AUDIO_TBL_SIZE);
            videoFmtTbl = new FormatTable(VIDEO_TBL_SIZE);
            miscFmtTbl = new FormatTable(MISC_TBL_SIZE);
        }
    }

    public static final Format[] putDB(Format input, Format[] supported) {
        Format[] list;
        synchronized (fmtTblSync) {
            Format in = input.relax();
            list = new Format[supported.length];
            for (int i = 0; i < supported.length; i++) {
                list[i] = supported[i].relax();
            }
            if (in instanceof AudioFormat) {
                audioFmtTbl.save(in, list);
            } else if (in instanceof VideoFormat) {
                videoFmtTbl.save(in, list);
            } else {
                miscFmtTbl.save(in, list);
            }
            needSaving = true;
        }
        return list;
    }

    private static final synchronized boolean readResource(InputStream ris) {
        boolean z;
        synchronized (Resource.class) {
            if (ris == null) {
                z = false;
            } else {
                try {
                    ObjectInputStream ois = new ObjectInputStream(ris);
                    int tableSize = ois.readInt();
                    if (ois.readInt() > 200) {
                        System.err.println("Version number mismatch.\nThere could be errors in reading the resource");
                    }
                    hash = new Hashtable();
                    for (int i = 0; i < tableSize; i++) {
                        String key = ois.readUTF();
                        try {
                            hash.put(key, ois.readObject());
                        } catch (ClassNotFoundException e) {
                        } catch (OptionalDataException e2) {
                        }
                    }
                    ois.close();
                    ris.close();
                    z = true;
                } catch (IOException ioe) {
                    System.err.println("IOException in readResource: " + ioe);
                    z = false;
                } catch (Throwable th) {
                    z = false;
                }
            }
        }
        return z;
    }

    public static final synchronized boolean remove(String key) {
        boolean z;
        synchronized (Resource.class) {
            if (key != null) {
                if (hash.containsKey(key)) {
                    hash.remove(key);
                    z = true;
                }
            }
            z = false;
        }
        return z;
    }

    public static final synchronized void removeGroup(String keyStart) {
        synchronized (Resource.class) {
            Vector keys = new Vector();
            if (keyStart != null) {
                Enumeration e = hash.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    if (key.startsWith(keyStart)) {
                        keys.addElement(key);
                    }
                }
            }
            for (int i = 0; i < keys.size(); i++) {
                hash.remove(keys.elementAt(i));
            }
        }
    }

    public static final synchronized void reset() {
        synchronized (Resource.class) {
            hash = new Hashtable();
        }
    }

    public static final void saveDB() {
        synchronized (fmtTblSync) {
            if (needSaving) {
                int i;
                reset();
                set(AUDIO_SIZE_KEY, new Integer(audioFmtTbl.last));
                for (i = 0; i < audioFmtTbl.last; i++) {
                    set(AUDIO_INPUT_KEY + i, audioFmtTbl.keys[i]);
                    set(AUDIO_FORMAT_KEY + i, audioFmtTbl.table[i]);
                    set(AUDIO_HIT_KEY + i, new Integer(audioFmtTbl.hits[i]));
                }
                set(VIDEO_SIZE_KEY, new Integer(videoFmtTbl.last));
                for (i = 0; i < videoFmtTbl.last; i++) {
                    set(VIDEO_INPUT_KEY + i, videoFmtTbl.keys[i]);
                    set(VIDEO_FORMAT_KEY + i, videoFmtTbl.table[i]);
                    set(VIDEO_HIT_KEY + i, new Integer(videoFmtTbl.hits[i]));
                }
                set(MISC_SIZE_KEY, new Integer(miscFmtTbl.last));
                for (i = 0; i < miscFmtTbl.last; i++) {
                    set(MISC_INPUT_KEY + i, miscFmtTbl.keys[i]);
                    set(MISC_FORMAT_KEY + i, miscFmtTbl.table[i]);
                    set(MISC_HIT_KEY + i, new Integer(miscFmtTbl.hits[i]));
                }
                try {
                    commit();
                } catch (Throwable th) {
                }
                needSaving = false;
                return;
            }
        }
    }

    public static final synchronized boolean set(String key, Object value) {
        boolean z;
        synchronized (Resource.class) {
            if (key == null || value == null) {
                z = false;
            } else {
                hash.put(key, value);
                z = true;
            }
        }
        return z;
    }
}
