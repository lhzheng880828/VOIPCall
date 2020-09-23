package javax.media;

public interface Renderer extends PlugIn {
    Format[] getSupportedInputFormats();

    int process(Buffer buffer);

    Format setInputFormat(Format format);

    void start();

    void stop();
}
