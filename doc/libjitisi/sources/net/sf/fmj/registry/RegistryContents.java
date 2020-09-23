package net.sf.fmj.registry;

import java.util.Vector;
import javax.media.CaptureDeviceInfo;
import net.sf.fmj.media.MimeTable;

class RegistryContents {
    Vector<CaptureDeviceInfo> captureDeviceInfoList = new Vector();
    Vector<String> contentPrefixList = new Vector();
    final MimeTable mimeTable = new MimeTable();
    Vector<String>[] plugins = new Vector[]{new Vector(), new Vector(), new Vector(), new Vector(), new Vector()};
    Vector<String> protocolPrefixList = new Vector();

    RegistryContents() {
    }
}
