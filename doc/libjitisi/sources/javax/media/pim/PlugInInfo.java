package javax.media.pim;

import javax.media.Format;

class PlugInInfo {
    public String className;
    public Format[] inputFormats;
    public Format[] outputFormats;

    public PlugInInfo(String name, Format[] formats, Format[] formats2) {
        this.className = name;
        this.inputFormats = formats;
        this.outputFormats = formats2;
    }

    public boolean equals(Object other) {
        return (other instanceof PlugInInfo) && (this.className == ((PlugInInfo) other).className || (this.className != null && this.className.equals(((PlugInInfo) other).className)));
    }

    public int hashCode() {
        return this.className.hashCode();
    }
}
