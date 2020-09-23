package com.sun.media;

import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.PlugIn;
import javax.media.PlugInManager;
import javax.media.ResourceUnavailableException;

@Deprecated
public abstract class BasicPlugIn implements PlugIn {
    protected Object[] controls = new Object[0];

    public abstract void close();

    public abstract String getName();

    public abstract void open() throws ResourceUnavailableException;

    public abstract void reset();

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
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
        if (name == null) {
            throw new NullPointerException();
        }
        Vector v = PlugInManager.getPlugInList(null, null, type);
        for (int i = 0; i < v.size(); i++) {
            if (name.equals((String) v.get(i))) {
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
            Class<?> clazz = Class.forName(controlType);
            Object[] controls = getControls();
            for (Object control : controls) {
                if (clazz.isInstance(control)) {
                    return control;
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
        byte[] dataCast;
        Object data = buffer.getData();
        if (data == null || data.getClass() != byte[].class) {
            dataCast = null;
        } else {
            dataCast = (byte[]) data;
        }
        if (dataCast != null && dataCast.length >= newSize) {
            return dataCast;
        }
        byte[] newData = new byte[newSize];
        if (dataCast != null) {
            System.arraycopy(dataCast, 0, newData, 0, dataCast.length);
        }
        buffer.setData(newData);
        return newData;
    }

    /* access modifiers changed from: protected */
    public Object validateData(Buffer buffer, int length, boolean allowNative) {
        Class<?> dataType = buffer.getFormat().getDataType();
        if (dataType == Format.byteArray) {
            return validateByteArraySize(buffer, length);
        }
        if (dataType == Format.shortArray) {
            return validateShortArraySize(buffer, length);
        }
        if (dataType == Format.intArray) {
            return validateIntArraySize(buffer, length);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int[] validateIntArraySize(Buffer buffer, int newSize) {
        int[] dataCast;
        Object data = buffer.getData();
        if (data == null || data.getClass() != int[].class) {
            dataCast = null;
        } else {
            dataCast = (int[]) data;
        }
        if (dataCast != null && dataCast.length >= newSize) {
            return dataCast;
        }
        int[] newData = new int[newSize];
        if (dataCast != null) {
            System.arraycopy(dataCast, 0, newData, 0, dataCast.length);
        }
        buffer.setData(newData);
        return newData;
    }

    /* access modifiers changed from: protected */
    public short[] validateShortArraySize(Buffer buffer, int newSize) {
        short[] dataCast;
        Object data = buffer.getData();
        if (data == null || data.getClass() != short[].class) {
            dataCast = null;
        } else {
            dataCast = (short[]) data;
        }
        if (dataCast != null && dataCast.length >= newSize) {
            return dataCast;
        }
        short[] newData = new short[newSize];
        if (dataCast != null) {
            System.arraycopy(dataCast, 0, newData, 0, dataCast.length);
        }
        buffer.setData(newData);
        return newData;
    }
}
