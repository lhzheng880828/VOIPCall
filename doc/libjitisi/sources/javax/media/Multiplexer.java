package javax.media;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public interface Multiplexer extends PlugIn {
    DataSource getDataOutput();

    Format[] getSupportedInputFormats();

    ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] formatArr);

    int process(Buffer buffer, int i);

    ContentDescriptor setContentDescriptor(ContentDescriptor contentDescriptor);

    Format setInputFormat(Format format, int i);

    int setNumTracks(int i);
}
