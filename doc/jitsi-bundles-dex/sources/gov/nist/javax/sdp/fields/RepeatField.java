package gov.nist.javax.sdp.fields;

import java.util.LinkedList;
import java.util.ListIterator;
import javax.sdp.RepeatTime;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class RepeatField extends SDPField implements RepeatTime {
    private static final long serialVersionUID = -6415338212212641819L;
    protected TypedTime activeDuration;
    protected SDPObjectList offsets = new SDPObjectList();
    protected TypedTime repeatInterval;

    public RepeatField() {
        super(SDPFieldNames.REPEAT_FIELD);
    }

    public void setRepeatInterval(TypedTime interval) {
        this.repeatInterval = interval;
    }

    public void setActiveDuration(TypedTime duration) {
        this.activeDuration = duration;
    }

    public void addOffset(TypedTime offset) {
        this.offsets.add(offset);
    }

    public LinkedList getOffsets() {
        return this.offsets;
    }

    public int getRepeatInterval() throws SdpParseException {
        if (this.repeatInterval == null) {
            return -1;
        }
        return this.repeatInterval.getTime();
    }

    public void setRepeatInterval(int repeatInterval) throws SdpException {
        if (repeatInterval < 0) {
            throw new SdpException("The repeat interval is <0");
        }
        if (this.repeatInterval == null) {
            this.repeatInterval = new TypedTime();
        }
        this.repeatInterval.setTime(repeatInterval);
    }

    public int getActiveDuration() throws SdpParseException {
        if (this.activeDuration == null) {
            return -1;
        }
        return this.activeDuration.getTime();
    }

    public void setActiveDuration(int activeDuration) throws SdpException {
        if (activeDuration < 0) {
            throw new SdpException("The active Duration is <0");
        }
        if (this.activeDuration == null) {
            this.activeDuration = new TypedTime();
        }
        this.activeDuration.setTime(activeDuration);
    }

    public int[] getOffsetArray() throws SdpParseException {
        LinkedList linkedList = getOffsets();
        int[] result = new int[linkedList.size()];
        for (int i = 0; i < linkedList.size(); i++) {
            result[i] = ((TypedTime) linkedList.get(i)).getTime();
        }
        return result;
    }

    public void setOffsetArray(int[] offsets) throws SdpException {
        for (int time : offsets) {
            TypedTime typedTime = new TypedTime();
            typedTime.setTime(time);
            addOffset(typedTime);
        }
    }

    public boolean getTypedTime() throws SdpParseException {
        return true;
    }

    public void setTypedTime(boolean typedTime) {
    }

    public String encode() {
        StringBuilder retval = new StringBuilder();
        retval.append(SDPFieldNames.REPEAT_FIELD).append(this.repeatInterval.encode()).append(Separators.SP).append(this.activeDuration.encode());
        ListIterator li = this.offsets.listIterator();
        while (li.hasNext()) {
            retval.append(Separators.SP).append(((TypedTime) li.next()).encode());
        }
        retval.append(Separators.NEWLINE);
        return retval.toString();
    }

    public Object clone() {
        RepeatField retval = (RepeatField) super.clone();
        if (this.repeatInterval != null) {
            retval.repeatInterval = (TypedTime) this.repeatInterval.clone();
        }
        if (this.activeDuration != null) {
            retval.activeDuration = (TypedTime) this.activeDuration.clone();
        }
        if (this.offsets != null) {
            retval.offsets = (SDPObjectList) this.offsets.clone();
        }
        return retval;
    }
}
