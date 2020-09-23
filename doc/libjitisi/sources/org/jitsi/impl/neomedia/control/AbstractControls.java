package org.jitsi.impl.neomedia.control;

import javax.media.Controls;
import org.jitsi.util.Logger;

public abstract class AbstractControls implements Controls {
    private static final Logger logger = Logger.getLogger(AbstractControls.class);

    public Object getControl(String controlType) {
        return getControl(this, controlType);
    }

    public static Object getControl(Controls controlsImpl, String controlType) {
        Object[] controls = controlsImpl.getControls();
        if (controls != null && controls.length > 0) {
            Class<?> controlClass;
            try {
                controlClass = Class.forName(controlType);
            } catch (ClassNotFoundException cnfe) {
                controlClass = null;
                logger.warn("Failed to find control class " + controlType, cnfe);
            }
            if (controlClass != null) {
                for (Object control : controls) {
                    if (controlClass.isInstance(control)) {
                        return control;
                    }
                }
            }
        }
        return null;
    }

    public static <T> T queryInterface(Controls controlsImpl, Class<T> controlType) {
        if (controlsImpl == null) {
            return null;
        }
        T control = controlsImpl.getControl(controlType.getName());
        if (control == null && controlType.isInstance(controlsImpl)) {
            return controlsImpl;
        }
        return control;
    }

    public static Object queryInterface(Controls controlsImpl, String controlType) {
        if (controlsImpl == null) {
            return null;
        }
        Object control = controlsImpl.getControl(controlType);
        if (control != null) {
            return control;
        }
        Class<?> controlClass;
        try {
            controlClass = Class.forName(controlType);
        } catch (ClassNotFoundException cnfe) {
            controlClass = null;
            logger.warn("Failed to find control class " + controlType, cnfe);
        }
        if (controlClass == null || !controlClass.isInstance(controlsImpl)) {
            return control;
        }
        return controlsImpl;
    }
}
