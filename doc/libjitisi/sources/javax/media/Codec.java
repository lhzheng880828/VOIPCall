package javax.media;

public interface Codec extends PlugIn {
    Format[] getSupportedInputFormats();

    Format[] getSupportedOutputFormats(Format format);

    int process(Buffer buffer, Buffer buffer2);

    Format setInputFormat(Format format);

    Format setOutputFormat(Format format);
}
