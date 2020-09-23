package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import net.java.sip.communicator.service.protocol.OperationSetThumbnailedFileFactory;

public class OperationSetThumbnailedFileFactoryImpl implements OperationSetThumbnailedFileFactory {
    public File createFileWithThumbnail(File file, int thumbnailWidth, int thumbnailHeight, String thumbnailMimeType, byte[] thumbnail) {
        return new ThumbnailedFile(file, thumbnailWidth, thumbnailHeight, thumbnailMimeType, thumbnail);
    }
}
