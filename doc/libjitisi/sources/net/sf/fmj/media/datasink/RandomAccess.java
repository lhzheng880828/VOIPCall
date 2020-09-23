package net.sf.fmj.media.datasink;

public interface RandomAccess {
    void setEnabled(boolean z);

    boolean write(long j, int i);
}
