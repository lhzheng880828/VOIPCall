package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;

public class ThumbnailedFile extends File {
    private static final long serialVersionUID = 0;
    private final byte[] thumbnail;
    private final int thumbnailHeight;
    private final String thumbnailMimeType;
    private final int thumbnailWidth;

    public ThumbnailedFile(File file, int thumbnailWidth, int thumbnailHeight, String thumbnailMimeType, byte[] thumbnail) {
        super(file.getPath());
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailMimeType = thumbnailMimeType;
        this.thumbnail = thumbnail;
    }

    public byte[] getThumbnailData() {
        return this.thumbnail;
    }

    public int getThumbnailWidth() {
        return this.thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return this.thumbnailHeight;
    }

    public String getThumbnailMimeType() {
        return this.thumbnailMimeType;
    }
}
