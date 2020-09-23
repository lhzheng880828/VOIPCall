package net.sf.fmj.media.protocol.javasound;

import java.io.File;
import java.io.IOException;
import org.jitsi.android.util.javax.sound.sampled.AudioFileFormat.Type;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat.Encoding;
import org.jitsi.android.util.javax.sound.sampled.AudioInputStream;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.DataLine.Info;
import org.jitsi.android.util.javax.sound.sampled.LineUnavailableException;
import org.jitsi.android.util.javax.sound.sampled.TargetDataLine;

public class SimpleAudioRecorder extends Thread {
    private AudioInputStream m_audioInputStream;
    private TargetDataLine m_line;
    private File m_outputFile;
    private Type m_targetType;

    public static void main(String[] args) {
        if (args.length != 1 || args[0].equals("-h")) {
            printUsageAndExit();
        }
        File outputFile = new File(args[0]);
        AudioFormat audioFormat = new AudioFormat(Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
        TargetDataLine targetDataLine = null;
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(new Info(TargetDataLine.class, audioFormat));
            targetDataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            out("unable to get a recording line");
            e.printStackTrace();
            System.exit(1);
        }
        SimpleAudioRecorder recorder = new SimpleAudioRecorder(targetDataLine, Type.WAVE, outputFile);
        out("Press ENTER to start the recording.");
        try {
            System.in.read();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        recorder.start();
        out("Recording...");
        out("Press ENTER to stop the recording.");
        try {
            System.in.read();
        } catch (IOException e22) {
            e22.printStackTrace();
        }
        recorder.stopRecording();
        out("Recording stopped.");
    }

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }

    private static void printUsageAndExit() {
        out("SimpleAudioRecorder: usage:");
        out("\tjava SimpleAudioRecorder -h");
        out("\tjava SimpleAudioRecorder <audiofile>");
        System.exit(0);
    }

    public SimpleAudioRecorder(TargetDataLine line, Type targetType, File file) {
        this.m_line = line;
        this.m_audioInputStream = new AudioInputStream(line);
        this.m_targetType = targetType;
        this.m_outputFile = file;
    }

    public void run() {
        try {
            AudioSystem.write(this.m_audioInputStream, this.m_targetType, this.m_outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.m_line.start();
        super.start();
    }

    public void stopRecording() {
        this.m_line.stop();
        this.m_line.close();
    }
}
