package javax.media.bean.playerbean;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.jitsi.android.util.java.awt.Image;

public class MediaPlayerBeanInfo extends SimpleBeanInfo {
    private final PropertyDescriptor[] propertyDescriptors;

    private static PropertyDescriptor buildPropertyDescriptor(Class<?> clazz, String name, String displayName, Class<?> propertyEditorClass, boolean bound) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(name, clazz);
        pd.setDisplayName(displayName);
        pd.setPropertyEditorClass(propertyEditorClass);
        pd.setBound(bound);
        return pd;
    }

    public MediaPlayerBeanInfo() {
        try {
            this.propertyDescriptors = new PropertyDescriptor[]{buildPropertyDescriptor(MediaPlayer.class, "mediaLocation", "media location", MediaPlayerMediaLocationEditor.class, true), buildPropertyDescriptor(MediaPlayer.class, "controlPanelVisible", "show control panel", null, true), buildPropertyDescriptor(MediaPlayer.class, "cachingControlVisible", "show caching control", null, true), buildPropertyDescriptor(MediaPlayer.class, "fixedAspectRatio", "fixedAspectRatio", null, true), buildPropertyDescriptor(MediaPlayer.class, "playbackLoop", "loop", null, true), buildPropertyDescriptor(MediaPlayer.class, "volumeLevel", "volume", MediaPlayerVolumePropertyEditor.class, true), buildPropertyDescriptor(MediaPlayer.class, "background", "background", null, false), buildPropertyDescriptor(MediaPlayer.class, "foreground", "foreground", null, false), buildPropertyDescriptor(MediaPlayer.class, "font", "font", null, false)};
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor result = new BeanDescriptor(MediaPlayer.class);
        result.setName("MediaPlayer");
        result.setDisplayName("MediaPlayer Bean");
        result.setShortDescription("MediaPlayer Bean");
        return result;
    }

    public int getDefaultPropertyIndex() {
        return 1;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        throw new Error(new IntrospectionException("Method \"controllerUpdate\" should have argument \"ControllerUpdateEvent\""));
    }

    public Image getIcon(int ic) {
        return null;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors;
    }
}
