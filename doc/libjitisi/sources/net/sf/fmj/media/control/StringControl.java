package net.sf.fmj.media.control;

public interface StringControl extends AtomicControl {
    String getTitle();

    String getValue();

    String setTitle(String str);

    String setValue(String str);
}
