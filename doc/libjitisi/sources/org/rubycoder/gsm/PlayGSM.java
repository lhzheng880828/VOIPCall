package org.rubycoder.gsm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import sun.audio.AudioPlayer;

public class PlayGSM {
    public static void main(String[] argv) {
        if (argv.length != 1) {
            System.out.println("Usage: PlayGSM <url>");
            System.exit(0);
        }
        stream(argv[0]);
    }

    private static void stream(String u) {
        URL url = null;
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            System.out.println("The URL is invalid.");
            System.exit(1);
        }
        InputStream gsmStream = null;
        try {
            gsmStream = url.openStream();
        } catch (IOException e2) {
            System.err.println("IO exception occured.");
            System.exit(1);
        }
        byte[] b = new byte[1];
        AudioPlayer.player.start(new GSMDecoderStream(gsmStream));
    }
}
