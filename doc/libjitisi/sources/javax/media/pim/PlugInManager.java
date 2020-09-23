package javax.media.pim;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Codec;
import javax.media.Demultiplexer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.Renderer;
import net.sf.fmj.registry.Registry;
import net.sf.fmj.utility.LoggerSingleton;

public class PlugInManager extends javax.media.PlugInManager {
    private static boolean TRACE = false;
    private static final Logger logger = LoggerSingleton.logger;
    private static final HashMap<String, PlugInInfo>[] pluginMaps = new HashMap[]{new HashMap(), new HashMap(), new HashMap(), new HashMap(), new HashMap()};
    private static Registry registry = Registry.getInstance();

    static {
        for (int i = 0; i < 5; i++) {
            List<String> classList = registry.getPluginList(i + 1);
            HashMap<String, PlugInInfo> pluginMap = pluginMaps[i];
            for (String className : classList) {
                PlugInInfo info = getPluginInfo(className);
                if (info != null) {
                    pluginMap.put(info.className, info);
                }
            }
        }
    }

    public static synchronized boolean addPlugIn(String classname, Format[] in, Format[] out, int type) {
        boolean z = false;
        synchronized (PlugInManager.class) {
            try {
                Class.forName(classname);
                if (find(classname, type) == null) {
                    PlugInInfo plugInInfo = new PlugInInfo(classname, in, out);
                    List<String> classList = registry.getPluginList(type);
                    HashMap<String, PlugInInfo> pluginMap = pluginMaps[type - 1];
                    classList.add(classname);
                    pluginMap.put(classname, plugInInfo);
                    registry.setPluginList(type, classList);
                    z = true;
                }
            } catch (ClassNotFoundException e) {
                logger.finer("addPlugIn failed for nonexistant class: " + classname);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Unable to addPlugIn for " + classname + " due to inability to get its class: " + t, t);
            }
        }
        return z;
    }

    public static synchronized void commit() throws IOException {
        synchronized (PlugInManager.class) {
            registry.commit();
        }
    }

    private static synchronized PlugInInfo find(String classname, int type) {
        PlugInInfo info;
        synchronized (PlugInManager.class) {
            info = (PlugInInfo) pluginMaps[type - 1].get(classname);
        }
        return info;
    }

    private static final PlugInInfo getPluginInfo(String pluginName) {
        try {
            Format[] in;
            Format[] out;
            Demultiplexer pluginObject = Class.forName(pluginName).newInstance();
            if (pluginObject instanceof Demultiplexer) {
                in = pluginObject.getSupportedInputContentDescriptors();
                out = null;
            } else if (pluginObject instanceof Codec) {
                Codec codec = (Codec) pluginObject;
                in = codec.getSupportedInputFormats();
                out = codec.getSupportedOutputFormats(null);
            } else if (pluginObject instanceof Multiplexer) {
                Multiplexer mux = (Multiplexer) pluginObject;
                in = mux.getSupportedInputFormats();
                out = mux.getSupportedOutputContentDescriptors(null);
            } else if (pluginObject instanceof Renderer) {
                in = ((Renderer) pluginObject).getSupportedInputFormats();
                out = null;
            } else if (pluginObject instanceof Effect) {
                Effect effect = (Effect) pluginObject;
                in = effect.getSupportedInputFormats();
                out = effect.getSupportedOutputFormats(null);
            } else {
                logger.warning("Unknown plugin type: " + pluginObject + " for plugin " + pluginName);
                return null;
            }
            return new PlugInInfo(pluginName, in, out);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.fine("Problem loading plugin " + pluginName + ": " + t);
                return null;
            }
        }
    }

    public static synchronized Vector<String> getPlugInList(Format input, Format output, int type) {
        Vector<String> result;
        synchronized (PlugInManager.class) {
            if (TRACE) {
                logger.info("getting plugin list...");
            }
            if (isValid(type)) {
                result = new Vector();
                Vector<String> classList = getVector(type);
                HashMap<String, PlugInInfo> pluginMap = pluginMaps[type - 1];
                for (int i = 0; i < classList.size(); i++) {
                    PlugInInfo plugInInfo = (PlugInInfo) pluginMap.get((String) classList.get(i));
                    if (plugInInfo != null) {
                        boolean match;
                        if (input != null) {
                            if (plugInInfo.inputFormats != null) {
                                match = false;
                                for (Format matches : plugInInfo.inputFormats) {
                                    if (input.matches(matches)) {
                                        match = true;
                                        break;
                                    }
                                }
                                if (!match) {
                                }
                            }
                        }
                        if (output != null) {
                            if (plugInInfo.outputFormats != null) {
                                match = false;
                                for (Format matches2 : plugInInfo.outputFormats) {
                                    if (output.matches(matches2)) {
                                        match = true;
                                        break;
                                    }
                                }
                                if (!match) {
                                }
                            }
                        }
                        result.add(plugInInfo.className);
                    }
                }
            } else {
                result = new Vector();
            }
        }
        return result;
    }

    public static synchronized Format[] getSupportedInputFormats(String className, int type) {
        Format[] formatArr;
        synchronized (PlugInManager.class) {
            PlugInInfo pi = find(className, type);
            if (pi == null) {
                formatArr = null;
            } else {
                formatArr = pi.inputFormats;
            }
        }
        return formatArr;
    }

    public static synchronized Format[] getSupportedOutputFormats(String className, int type) {
        Format[] formatArr;
        synchronized (PlugInManager.class) {
            PlugInInfo pi = find(className, type);
            if (pi == null) {
                formatArr = null;
            } else {
                formatArr = pi.outputFormats;
            }
        }
        return formatArr;
    }

    private static Vector<String> getVector(int type) {
        if (!isValid(type)) {
            return null;
        }
        List<String> classList = registry.getPluginList(type);
        Vector<String> result = new Vector();
        result.addAll(classList);
        return result;
    }

    private static boolean isValid(int type) {
        return type >= 1 && type <= 5;
    }

    public static synchronized boolean removePlugIn(String classname, int type) {
        boolean result;
        synchronized (PlugInManager.class) {
            List<String> classList = registry.getPluginList(type);
            result = classList.remove(classname) | (pluginMaps[type + -1].remove(classname) != null ? 1 : 0);
            registry.setPluginList(type, classList);
        }
        return result;
    }

    public static synchronized void setPlugInList(Vector plugins, int type) {
        synchronized (PlugInManager.class) {
            registry.setPluginList(type, plugins);
        }
    }

    private PlugInManager() {
    }
}
