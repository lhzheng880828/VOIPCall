package net.java.sip.communicator.service.hid;

public interface HIDService {
    void keyPress(char c);

    void keyPress(int i);

    void keyRelease(char c);

    void keyRelease(int i);

    void mouseMove(int i, int i2);

    void mousePress(int i);

    void mouseRelease(int i);

    void mouseWheel(int i);
}
