package net.sf.fmj.media;

import java.lang.reflect.Method;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.Format;
import javax.media.PlugIn;
import javax.media.PlugInManager;
import javax.media.format.VideoFormat;

public abstract class BasicPlugIn implements PlugIn {
    private static final boolean DEBUG = false;
    private static Method forName3ArgsM;
    private static Method getContextClassLoaderM;
    private static boolean jdkInit = false;
    private static ClassLoader systemClassLoader;
    protected Object[] controls = new Control[0];

    private static boolean checkIfJDK12() {
        if (jdkInit) {
            return forName3ArgsM != null;
        } else {
            jdkInit = true;
            try {
                forName3ArgsM = Class.class.getMethod("forName", new Class[]{String.class, Boolean.TYPE, ClassLoader.class});
                systemClassLoader = (ClassLoader) ClassLoader.class.getMethod("getSystemClassLoader", new Class[0]).invoke(ClassLoader.class, new Object[0]);
                getContextClassLoaderM = Thread.class.getMethod("getContextClassLoader", new Class[0]);
                return true;
            } catch (Throwable th) {
                forName3ArgsM = null;
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:?, code skipped:
            r0 = (java.lang.ClassLoader) getContextClassLoaderM.invoke(java.lang.Thread.currentThread(), new java.lang.Object[0]);
     */
    /* JADX WARNING: Missing block: B:16:0x006a, code skipped:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x0074, code skipped:
            throw new java.lang.ClassNotFoundException(r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:19:0x0075, code skipped:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x0076, code skipped:
            throw r1;
     */
    /* JADX WARNING: Missing block: B:22:?, code skipped:
            return (java.lang.Class) forName3ArgsM.invoke(java.lang.Class.class, new java.lang.Object[]{r8, new java.lang.Boolean(true), systemClassLoader});
     */
    /* JADX WARNING: Missing block: B:23:?, code skipped:
            return (java.lang.Class) forName3ArgsM.invoke(java.lang.Class.class, new java.lang.Object[]{r8, new java.lang.Boolean(true), r0});
     */
    public static java.lang.Class<?> getClassForName(java.lang.String r8) throws java.lang.ClassNotFoundException {
        /*
        r2 = java.lang.Class.forName(r8);	 Catch:{ Exception -> 0x0005, Error -> 0x0016 }
    L_0x0004:
        return r2;
    L_0x0005:
        r1 = move-exception;
        r2 = checkIfJDK12();
        if (r2 != 0) goto L_0x001e;
    L_0x000c:
        r2 = new java.lang.ClassNotFoundException;
        r3 = r1.getMessage();
        r2.<init>(r3);
        throw r2;
    L_0x0016:
        r1 = move-exception;
        r2 = checkIfJDK12();
        if (r2 != 0) goto L_0x001e;
    L_0x001d:
        throw r1;
    L_0x001e:
        r2 = forName3ArgsM;	 Catch:{ Throwable -> 0x003d }
        r3 = java.lang.Class.class;
        r4 = 3;
        r4 = new java.lang.Object[r4];	 Catch:{ Throwable -> 0x003d }
        r5 = 0;
        r4[r5] = r8;	 Catch:{ Throwable -> 0x003d }
        r5 = 1;
        r6 = new java.lang.Boolean;	 Catch:{ Throwable -> 0x003d }
        r7 = 1;
        r6.<init>(r7);	 Catch:{ Throwable -> 0x003d }
        r4[r5] = r6;	 Catch:{ Throwable -> 0x003d }
        r5 = 2;
        r6 = systemClassLoader;	 Catch:{ Throwable -> 0x003d }
        r4[r5] = r6;	 Catch:{ Throwable -> 0x003d }
        r2 = r2.invoke(r3, r4);	 Catch:{ Throwable -> 0x003d }
        r2 = (java.lang.Class) r2;	 Catch:{ Throwable -> 0x003d }
        goto L_0x0004;
    L_0x003d:
        r2 = move-exception;
        r2 = getContextClassLoaderM;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r3 = java.lang.Thread.currentThread();	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r4 = 0;
        r4 = new java.lang.Object[r4];	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r0 = r2.invoke(r3, r4);	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r0 = (java.lang.ClassLoader) r0;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r2 = forName3ArgsM;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r3 = java.lang.Class.class;
        r4 = 3;
        r4 = new java.lang.Object[r4];	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r5 = 0;
        r4[r5] = r8;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r5 = 1;
        r6 = new java.lang.Boolean;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r7 = 1;
        r6.<init>(r7);	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r4[r5] = r6;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r5 = 2;
        r4[r5] = r0;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r2 = r2.invoke(r3, r4);	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        r2 = (java.lang.Class) r2;	 Catch:{ Exception -> 0x006a, Error -> 0x0075 }
        goto L_0x0004;
    L_0x006a:
        r1 = move-exception;
        r2 = new java.lang.ClassNotFoundException;
        r3 = r1.getMessage();
        r2.<init>(r3);
        throw r2;
    L_0x0075:
        r1 = move-exception;
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlugIn.getClassForName(java.lang.String):java.lang.Class");
    }

    public static Format matches(Format in, Format[] outs) {
        for (int i = 0; i < outs.length; i++) {
            if (in.matches(outs[i])) {
                return outs[i];
            }
        }
        return null;
    }

    public static boolean plugInExists(String name, int type) {
        Vector cnames = PlugInManager.getPlugInList(null, null, type);
        for (int i = 0; i < cnames.size(); i++) {
            if (name.equals(cnames.elementAt(i))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void error() {
        throw new RuntimeException(getClass().getName() + " PlugIn error");
    }

    public Object getControl(String controlType) {
        try {
            Class<?> cls = Class.forName(controlType);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] getControls() {
        return this.controls;
    }

    /* access modifiers changed from: protected */
    public Object getInputData(Buffer inBuffer) {
        return inBuffer.getData();
    }

    /* access modifiers changed from: protected|final */
    public final long getNativeData(Object data) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public Object getOutputData(Buffer buffer) {
        return buffer.getData();
    }

    /* access modifiers changed from: protected */
    public byte[] validateByteArraySize(Buffer buffer, int newSize) {
        byte[] typedArray;
        Object objectArray = buffer.getData();
        if (objectArray instanceof byte[]) {
            typedArray = (byte[]) objectArray;
            if (typedArray.length >= newSize) {
                return typedArray;
            }
            byte[] tempArray = new byte[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else {
            typedArray = new byte[newSize];
        }
        buffer.setData(typedArray);
        byte[] bArr = typedArray;
        return typedArray;
    }

    /* access modifiers changed from: protected */
    public Object validateData(Buffer buffer, int length, boolean allowNative) {
        Format format = buffer.getFormat();
        Class<?> dataType = format.getDataType();
        if (length < 1 && format != null && (format instanceof VideoFormat)) {
            length = ((VideoFormat) format).getMaxDataLength();
        }
        if (dataType == Format.byteArray) {
            return validateByteArraySize(buffer, length);
        }
        if (dataType == Format.shortArray) {
            return validateShortArraySize(buffer, length);
        }
        if (dataType == Format.intArray) {
            return validateIntArraySize(buffer, length);
        }
        System.err.println("Error in validateData");
        return null;
    }

    /* access modifiers changed from: protected */
    public int[] validateIntArraySize(Buffer buffer, int newSize) {
        int[] typedArray;
        Object objectArray = buffer.getData();
        if (objectArray instanceof int[]) {
            typedArray = (int[]) objectArray;
            if (typedArray.length >= newSize) {
                return typedArray;
            }
            int[] tempArray = new int[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else {
            typedArray = new int[newSize];
        }
        buffer.setData(typedArray);
        int[] iArr = typedArray;
        return typedArray;
    }

    /* access modifiers changed from: protected */
    public short[] validateShortArraySize(Buffer buffer, int newSize) {
        short[] typedArray;
        Object objectArray = buffer.getData();
        if (objectArray instanceof short[]) {
            typedArray = (short[]) objectArray;
            if (typedArray.length >= newSize) {
                return typedArray;
            }
            short[] tempArray = new short[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else {
            typedArray = new short[newSize];
        }
        buffer.setData(typedArray);
        short[] sArr = typedArray;
        return typedArray;
    }
}
