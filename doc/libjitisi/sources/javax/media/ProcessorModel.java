package javax.media;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public class ProcessorModel {
    private Format[] formats;
    private DataSource inputDataSource;
    private MediaLocator inputLocator;
    private ContentDescriptor outputContentDescriptor;

    public ProcessorModel(DataSource inputDataSource, Format[] formats, ContentDescriptor outputContentDescriptor) {
        this.inputDataSource = inputDataSource;
        this.formats = formats;
        this.outputContentDescriptor = outputContentDescriptor;
    }

    public ProcessorModel(Format[] formats, ContentDescriptor outputContentDescriptor) {
        this.formats = formats;
        this.outputContentDescriptor = outputContentDescriptor;
    }

    public ProcessorModel(MediaLocator inputLocator, Format[] formats, ContentDescriptor outputContentDescriptor) {
        this.inputLocator = inputLocator;
        this.formats = formats;
        this.outputContentDescriptor = outputContentDescriptor;
    }

    public ContentDescriptor getContentDescriptor() {
        return this.outputContentDescriptor;
    }

    public DataSource getInputDataSource() {
        return this.inputDataSource;
    }

    public MediaLocator getInputLocator() {
        return this.inputLocator;
    }

    public Format getOutputTrackFormat(int tIndex) {
        if (this.formats != null && tIndex >= 0 && tIndex < this.formats.length) {
            return this.formats[tIndex];
        }
        return null;
    }

    public int getTrackCount(int availableTrackCount) {
        if (this.formats == null) {
            return -1;
        }
        return this.formats.length;
    }

    public boolean isFormatAcceptable(int tIndex, Format tFormat) {
        if (this.formats != null && tIndex >= 0 && tIndex < this.formats.length) {
            return tFormat.matches(this.formats[tIndex]);
        }
        return true;
    }
}
