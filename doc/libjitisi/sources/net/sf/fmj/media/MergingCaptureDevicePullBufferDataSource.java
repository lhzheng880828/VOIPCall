package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;

public class MergingCaptureDevicePullBufferDataSource extends MergingPullBufferDataSource implements CaptureDevice {
    public MergingCaptureDevicePullBufferDataSource(List<PullBufferDataSource> sources) {
        super(sources);
        for (DataSource source : sources) {
            if (!(source instanceof CaptureDevice)) {
                throw new IllegalArgumentException();
            }
        }
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        throw new UnsupportedOperationException();
    }

    public FormatControl[] getFormatControls() {
        List<FormatControl> formatControls = new ArrayList();
        for (DataSource source : this.sources) {
            for (FormatControl formatControl : ((CaptureDevice) source).getFormatControls()) {
                formatControls.add(formatControl);
            }
        }
        return (FormatControl[]) formatControls.toArray(new FormatControl[0]);
    }
}
