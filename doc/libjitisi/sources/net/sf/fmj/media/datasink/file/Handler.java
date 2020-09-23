package net.sf.fmj.media.datasink.file;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.IncompatibleSourceException;
import javax.media.MediaLocator;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceStream;
import javax.media.protocol.SourceTransferHandler;
import net.sf.fmj.media.Syncable;
import net.sf.fmj.media.datasink.BasicDataSink;
import net.sf.fmj.media.datasink.RandomAccess;

public class Handler extends BasicDataSink implements SourceTransferHandler, Seekable, Runnable, RandomAccess, Syncable {
    protected static final int BUFFER_LEN = 131072;
    protected static final int CLOSED = 3;
    private static final boolean DEBUG = false;
    protected static final int NOT_INITIALIZED = 0;
    protected static final int OPENED = 1;
    protected static final int STARTED = 2;
    public int WRITE_CHUNK_SIZE = 16384;
    protected byte[] buffer1 = new byte[131072];
    protected int buffer1Length;
    protected boolean buffer1Pending = DEBUG;
    protected long buffer1PendingLocation = -1;
    protected byte[] buffer2 = new byte[131072];
    protected int buffer2Length;
    protected boolean buffer2Pending = DEBUG;
    protected long buffer2PendingLocation = -1;
    private Integer bufferLock = new Integer(0);
    protected int bytesWritten = 0;
    protected String contentType = null;
    protected Control[] controls;
    private boolean errorCreatingStreamingFile = DEBUG;
    protected boolean errorEncountered = DEBUG;
    protected String errorReason = null;
    protected File file;
    protected boolean fileClosed = DEBUG;
    protected FileDescriptor fileDescriptor = null;
    protected int filePointer = 0;
    protected int fileSize = 0;
    long lastSyncTime = -1;
    protected MediaLocator locator = null;
    protected long nextLocation = 0;
    protected boolean push;
    protected RandomAccessFile qtStrRaFile = null;
    protected RandomAccessFile raFile = null;
    private boolean receivedEOS = DEBUG;
    protected DataSource source;
    protected int state = 0;
    protected SourceStream stream;
    private boolean streamingEnabled = DEBUG;
    protected SourceStream[] streams;
    protected boolean syncEnabled = DEBUG;
    protected File tempFile = null;
    protected Thread writeThread = null;

    public void close() {
        close(null);
    }

    /* access modifiers changed from: protected|final */
    /* JADX WARNING: Missing block: B:9:0x0010, code skipped:
            if (r6.push == false) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:10:0x0012, code skipped:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code skipped:
            if (r1 >= r6.streams.length) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:13:0x0018, code skipped:
            ((javax.media.protocol.PushSourceStream) r6.streams[r1]).setTransferHandler(null);
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:18:0x0027, code skipped:
            if (r7 == null) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:19:0x0029, code skipped:
            r6.errorEncountered = true;
            sendDataSinkErrorEvent(r7);
            r3 = r6.bufferLock;
     */
    /* JADX WARNING: Missing block: B:20:0x0031, code skipped:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:22:?, code skipped:
            r6.bufferLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:23:0x0037, code skipped:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:25:?, code skipped:
            r6.source.stop();
     */
    /* JADX WARNING: Missing block: B:50:0x0078, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:51:0x0079, code skipped:
            java.lang.System.err.println("IOException when stopping source " + r0);
     */
    public final void close(java.lang.String r7) {
        /*
        r6 = this;
        r3 = 3;
        r5 = 0;
        monitor-enter(r6);
        r2 = r6.state;	 Catch:{ all -> 0x0024 }
        if (r2 != r3) goto L_0x0009;
    L_0x0007:
        monitor-exit(r6);	 Catch:{ all -> 0x0024 }
    L_0x0008:
        return;
    L_0x0009:
        r2 = 3;
        r6.setState(r2);	 Catch:{ all -> 0x0024 }
        monitor-exit(r6);	 Catch:{ all -> 0x0024 }
        r2 = r6.push;
        if (r2 == 0) goto L_0x0027;
    L_0x0012:
        r1 = 0;
    L_0x0013:
        r2 = r6.streams;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x0027;
    L_0x0018:
        r2 = r6.streams;
        r2 = r2[r1];
        r2 = (javax.media.protocol.PushSourceStream) r2;
        r2.setTransferHandler(r5);
        r1 = r1 + 1;
        goto L_0x0013;
    L_0x0024:
        r2 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0024 }
        throw r2;
    L_0x0027:
        if (r7 == 0) goto L_0x0038;
    L_0x0029:
        r2 = 1;
        r6.errorEncountered = r2;
        r6.sendDataSinkErrorEvent(r7);
        r3 = r6.bufferLock;
        monitor-enter(r3);
        r2 = r6.bufferLock;	 Catch:{ all -> 0x0075 }
        r2.notifyAll();	 Catch:{ all -> 0x0075 }
        monitor-exit(r3);	 Catch:{ all -> 0x0075 }
    L_0x0038:
        r2 = r6.source;	 Catch:{ IOException -> 0x0078 }
        r2.stop();	 Catch:{ IOException -> 0x0078 }
    L_0x003d:
        r2 = r6.raFile;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x0046;
    L_0x0041:
        r2 = r6.raFile;	 Catch:{ IOException -> 0x0098 }
        r2.close();	 Catch:{ IOException -> 0x0098 }
    L_0x0046:
        r2 = r6.streamingEnabled;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x0053;
    L_0x004a:
        r2 = r6.qtStrRaFile;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x0053;
    L_0x004e:
        r2 = r6.qtStrRaFile;	 Catch:{ IOException -> 0x0098 }
        r2.close();	 Catch:{ IOException -> 0x0098 }
    L_0x0053:
        r2 = r6.source;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x005c;
    L_0x0057:
        r2 = r6.source;	 Catch:{ IOException -> 0x0098 }
        r2.disconnect();	 Catch:{ IOException -> 0x0098 }
    L_0x005c:
        r2 = r6.streamingEnabled;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x006d;
    L_0x0060:
        r2 = r6.tempFile;	 Catch:{ IOException -> 0x0098 }
        if (r2 == 0) goto L_0x006d;
    L_0x0064:
        r2 = r6.errorCreatingStreamingFile;	 Catch:{ IOException -> 0x0098 }
        if (r2 != 0) goto L_0x0092;
    L_0x0068:
        r2 = r6.tempFile;	 Catch:{ IOException -> 0x0098 }
        r6.deleteFile(r2);	 Catch:{ IOException -> 0x0098 }
    L_0x006d:
        r6.raFile = r5;
        r6.qtStrRaFile = r5;
        r6.removeAllListeners();
        goto L_0x0008;
    L_0x0075:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0075 }
        throw r2;
    L_0x0078:
        r0 = move-exception;
        r2 = java.lang.System.err;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "IOException when stopping source ";
        r3 = r3.append(r4);
        r3 = r3.append(r0);
        r3 = r3.toString();
        r2.println(r3);
        goto L_0x003d;
    L_0x0092:
        r2 = r6.file;	 Catch:{ IOException -> 0x0098 }
        r6.deleteFile(r2);	 Catch:{ IOException -> 0x0098 }
        goto L_0x006d;
    L_0x0098:
        r0 = move-exception;
        r2 = java.lang.System.out;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "close: ";
        r3 = r3.append(r4);
        r3 = r3.append(r0);
        r3 = r3.toString();
        r2.println(r3);
        goto L_0x006d;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.datasink.file.Handler.close(java.lang.String):void");
    }

    private boolean deleteFile(File file) {
        boolean fileDeleted = DEBUG;
        try {
            return file.delete();
        } catch (Throwable th) {
            return fileDeleted;
        }
    }

    public long doSeek(long where) {
        if (this.raFile != null) {
            try {
                this.raFile.seek(where);
                this.filePointer = (int) where;
                return where;
            } catch (IOException ioe) {
                close("Error in seek: " + ioe);
            }
        }
        return -1;
    }

    public long doTell() {
        if (this.raFile != null) {
            try {
                return this.raFile.getFilePointer();
            } catch (IOException ioe) {
                close("Error in tell: " + ioe);
            }
        }
        return -1;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Object getControl(String controlName) {
        return null;
    }

    public Object[] getControls() {
        if (this.controls == null) {
            this.controls = new Control[0];
        }
        return this.controls;
    }

    public MediaLocator getOutputLocator() {
        return this.locator;
    }

    public boolean isRandomAccess() {
        return true;
    }

    public void open() throws IOException, SecurityException {
        String pathName;
        try {
            if (this.state == 0 && this.locator != null) {
                pathName = this.locator.getRemainder();
                while (pathName.charAt(0) == '/' && (pathName.charAt(1) == '/' || pathName.charAt(2) == ':')) {
                    pathName = pathName.substring(1);
                }
                if (System.getProperty("file.separator").equals("\\")) {
                    pathName = pathName.replace('/', '\\');
                }
                this.file = new File(pathName);
                if (!this.file.exists() || deleteFile(this.file)) {
                    String parent = this.file.getParent();
                    if (parent != null) {
                        new File(parent).mkdirs();
                    }
                    if (this.streamingEnabled) {
                        String fileqt;
                        int index = pathName.lastIndexOf(".");
                        if (index > 0) {
                            fileqt = pathName.substring(0, index) + ".nonstreamable" + pathName.substring(index, pathName.length());
                        } else {
                            fileqt = this.file + ".nonstreamable.mov";
                        }
                        this.tempFile = new File(fileqt);
                        this.raFile = new RandomAccessFile(this.tempFile, "rw");
                        this.fileDescriptor = this.raFile.getFD();
                        this.qtStrRaFile = new RandomAccessFile(this.file, "rw");
                    } else {
                        this.raFile = new RandomAccessFile(this.file, "rw");
                        this.fileDescriptor = this.raFile.getFD();
                    }
                    setState(1);
                } else {
                    System.err.println("datasink open: Existing file " + pathName + " cannot be deleted. Check if " + "some other process is using " + " this file");
                    if (this.push) {
                        ((PushSourceStream) this.stream).setTransferHandler(null);
                    }
                    throw new IOException("Existing file " + pathName + " cannot be deleted");
                }
            }
            if (this.state == 0 && this.stream != null) {
                ((PushSourceStream) this.stream).setTransferHandler(null);
            }
        } catch (IOException e) {
            System.err.println("datasink open: IOException when creating RandomAccessFile " + pathName + " : " + e);
            if (this.push) {
                ((PushSourceStream) this.stream).setTransferHandler(null);
            }
            throw e;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (this.state == 0 && this.stream != null) {
                ((PushSourceStream) this.stream).setTransferHandler(null);
            }
        }
    }

    public void run() {
        while (this.state != 3 && !this.errorEncountered) {
            synchronized (this.bufferLock) {
                while (!this.buffer1Pending && !this.buffer2Pending && !this.errorEncountered && this.state != 3 && !this.receivedEOS) {
                    try {
                        this.bufferLock.wait(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (this.buffer2Pending) {
                write(this.buffer2, this.buffer2PendingLocation, this.buffer2Length);
                this.buffer2Pending = DEBUG;
            }
            synchronized (this.bufferLock) {
                if (this.buffer1Pending) {
                    byte[] tempBuffer = this.buffer2;
                    this.buffer2 = this.buffer1;
                    this.buffer2Pending = true;
                    this.buffer2PendingLocation = this.buffer1PendingLocation;
                    this.buffer2Length = this.buffer1Length;
                    this.buffer1Pending = DEBUG;
                    this.buffer1 = tempBuffer;
                    this.bufferLock.notifyAll();
                } else if (this.receivedEOS) {
                }
            }
        }
        if (this.receivedEOS) {
            if (this.raFile != null) {
                if (!this.streamingEnabled) {
                    try {
                        this.raFile.close();
                    } catch (IOException e2) {
                    }
                    this.raFile = null;
                }
                this.fileClosed = true;
            }
            if (!this.streamingEnabled) {
                sendEndofStreamEvent();
            }
        }
        if (this.errorEncountered && this.state != 3) {
            close(this.errorReason);
        }
    }

    public synchronized long seek(long where) {
        this.nextLocation = where;
        return where;
    }

    public void setEnabled(boolean b) {
        this.streamingEnabled = b;
    }

    public void setOutputLocator(MediaLocator output) {
        this.locator = output;
    }

    public void setSource(DataSource ds) throws IncompatibleSourceException {
        if ((ds instanceof PushDataSource) || (ds instanceof PullDataSource)) {
            this.source = ds;
            if (this.source instanceof PushDataSource) {
                this.push = true;
                try {
                    ((PushDataSource) this.source).connect();
                } catch (IOException e) {
                }
                this.streams = ((PushDataSource) this.source).getStreams();
            } else {
                this.push = DEBUG;
                try {
                    ((PullDataSource) this.source).connect();
                } catch (IOException e2) {
                }
                this.streams = ((PullDataSource) this.source).getStreams();
            }
            if (this.streams == null || this.streams.length != 1) {
                throw new IncompatibleSourceException("DataSource should have 1 stream");
            }
            this.stream = this.streams[0];
            this.contentType = this.source.getContentType();
            if (this.push) {
                ((PushSourceStream) this.stream).setTransferHandler(this);
                return;
            }
            return;
        }
        throw new IncompatibleSourceException("Incompatible datasource");
    }

    /* access modifiers changed from: protected */
    public void setState(int state) {
        synchronized (this) {
            this.state = state;
        }
    }

    public void setSyncEnabled() {
        this.syncEnabled = true;
    }

    public void start() throws IOException {
        if (this.state == 1) {
            if (this.source != null) {
                this.source.start();
            }
            if (this.writeThread == null) {
                this.writeThread = new Thread(this);
                this.writeThread.start();
            }
            setState(2);
        }
    }

    public void stop() throws IOException {
        if (this.state == 2) {
            if (this.source != null) {
                this.source.stop();
            }
            setState(1);
        }
    }

    public long tell() {
        return this.nextLocation;
    }

    public synchronized void transferData(PushSourceStream pss) {
        int totalRead = 0;
        int spaceAvailable = 131072;
        int bytesRead = 0;
        if (!this.errorEncountered) {
            if (this.buffer1Pending) {
                synchronized (this.bufferLock) {
                    while (this.buffer1Pending) {
                        try {
                            this.bufferLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            while (spaceAvailable > 0) {
                try {
                    bytesRead = pss.read(this.buffer1, totalRead, spaceAvailable);
                    if (bytesRead > 16384 && this.WRITE_CHUNK_SIZE < 32768) {
                        if (bytesRead <= Buffer.FLAG_SKIP_FEC || this.WRITE_CHUNK_SIZE >= 131072) {
                            if (bytesRead > 32768) {
                                if (this.WRITE_CHUNK_SIZE < Buffer.FLAG_SKIP_FEC) {
                                    this.WRITE_CHUNK_SIZE = Buffer.FLAG_SKIP_FEC;
                                }
                            }
                            if (this.WRITE_CHUNK_SIZE < 32768) {
                                this.WRITE_CHUNK_SIZE = 32768;
                            }
                        } else {
                            this.WRITE_CHUNK_SIZE = 131072;
                        }
                    }
                } catch (IOException e2) {
                }
                if (bytesRead <= 0) {
                    break;
                }
                totalRead += bytesRead;
                spaceAvailable -= bytesRead;
            }
            if (totalRead > 0) {
                synchronized (this.bufferLock) {
                    this.buffer1Pending = true;
                    this.buffer1PendingLocation = this.nextLocation;
                    this.buffer1Length = totalRead;
                    this.nextLocation = -1;
                    this.bufferLock.notifyAll();
                }
            }
            if (bytesRead == -1) {
                this.receivedEOS = true;
                while (!this.fileClosed && !this.errorEncountered && this.state != 3) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e3) {
                    }
                }
            }
        }
    }

    private void write(byte[] buffer, long location, int length) {
        if (location != -1) {
            try {
                doSeek(location);
            } catch (IOException ioe) {
                this.errorEncountered = true;
                this.errorReason = ioe.toString();
                return;
            }
        }
        int offset = 0;
        while (length > 0) {
            int toWrite = this.WRITE_CHUNK_SIZE;
            if (length < toWrite) {
                toWrite = length;
            }
            this.raFile.write(buffer, offset, toWrite);
            this.bytesWritten += toWrite;
            if (this.fileDescriptor != null && this.syncEnabled && this.bytesWritten >= this.WRITE_CHUNK_SIZE) {
                this.bytesWritten -= this.WRITE_CHUNK_SIZE;
                this.fileDescriptor.sync();
            }
            this.filePointer += toWrite;
            length -= toWrite;
            offset += toWrite;
            if (this.filePointer > this.fileSize) {
                this.fileSize = this.filePointer;
            }
            Thread.yield();
        }
    }

    public boolean write(long inOffset, int numBytes) {
        if (inOffset >= 0 && numBytes > 0) {
            int remaining = numBytes;
            try {
                this.raFile.seek(inOffset);
                while (remaining > 0) {
                    int bytesToRead;
                    if (remaining > 131072) {
                        bytesToRead = 131072;
                    } else {
                        bytesToRead = remaining;
                    }
                    this.raFile.read(this.buffer1, 0, bytesToRead);
                    this.qtStrRaFile.write(this.buffer1, 0, bytesToRead);
                    remaining -= bytesToRead;
                }
                return true;
            } catch (Exception e) {
                this.errorCreatingStreamingFile = true;
                System.err.println("Exception when creating streamable version of media file: " + e.getMessage());
                return DEBUG;
            }
        } else if (inOffset >= 0 || numBytes <= 0) {
            sendEndofStreamEvent();
            return true;
        } else {
            this.qtStrRaFile.seek(0);
            this.qtStrRaFile.seek((long) (numBytes - 1));
            this.qtStrRaFile.writeByte(0);
            this.qtStrRaFile.seek(0);
            return true;
        }
    }
}
