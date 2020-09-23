package gov.nist.javax.sdp.fields;

import java.lang.reflect.Field;
import org.jitsi.gov.nist.core.GenericObject;
import org.jitsi.gov.nist.core.GenericObjectList;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smackx.FormField;

public abstract class SDPObject extends GenericObject implements SDPFieldNames {
    protected static final String CORE_PACKAGE = "gov.nist.core";
    protected static final String SDPFIELDS_PACKAGE = "gov.nist.javax.sdp.fields";

    public abstract String encode();

    /* access modifiers changed from: protected */
    public void sprint(String s) {
        super.sprint(s);
    }

    /* access modifiers changed from: protected */
    public void initSprint() {
        this.stringRepresentation = "";
    }

    public String toString() {
        return encode();
    }

    /* access modifiers changed from: protected */
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }

    public boolean equals(Object that) {
        if (!getClass().equals(that.getClass())) {
            return false;
        }
        Field[] fields = getClass().getDeclaredFields();
        Field[] hisfields = that.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Field g = hisfields[i];
            if (f.getModifiers() != 2) {
                Class fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            if (fname.compareTo("int") == 0) {
                                if (f.getInt(this) != g.getInt(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("short") == 0) {
                                if (f.getShort(this) != g.getShort(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("char") == 0) {
                                if (f.getChar(this) != g.getChar(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("long") == 0) {
                                if (f.getLong(this) != g.getLong(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo(FormField.TYPE_BOOLEAN) == 0) {
                                if (f.getBoolean(this) != g.getBoolean(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("double") == 0) {
                                if (f.getDouble(this) != g.getDouble(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("float") == 0 && f.getFloat(this) != g.getFloat(that)) {
                                return false;
                            }
                        } else if (g.get(that) == f.get(this)) {
                            continue;
                        } else if (f.get(this) == null && g.get(that) != null) {
                            return false;
                        } else {
                            if (g.get(that) == null && f.get(that) != null) {
                                return false;
                            }
                            if (!f.get(this).equals(g.get(that))) {
                                return false;
                            }
                        }
                    } catch (IllegalAccessException ex1) {
                        InternalErrorHandler.handleException(ex1);
                    }
                }
            }
        }
        return true;
    }

    public String debugDump() {
        this.stringRepresentation = "";
        Class myclass = getClass();
        sprint(myclass.getName());
        sprint("{");
        Field[] fields = myclass.getDeclaredFields();
        for (Field f : fields) {
            if (f.getModifiers() != 2) {
                Class fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    sprint(fieldName + Separators.COLON);
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            sprint(fname + Separators.COLON);
                            if (fname.compareTo("int") == 0) {
                                sprint(f.getInt(this));
                            } else if (fname.compareTo("short") == 0) {
                                sprint(f.getShort(this));
                            } else if (fname.compareTo("char") == 0) {
                                sprint(f.getChar(this));
                            } else if (fname.compareTo("long") == 0) {
                                sprint(f.getLong(this));
                            } else if (fname.compareTo(FormField.TYPE_BOOLEAN) == 0) {
                                sprint(f.getBoolean(this));
                            } else if (fname.compareTo("double") == 0) {
                                sprint(f.getDouble(this));
                            } else if (fname.compareTo("float") == 0) {
                                sprint(f.getFloat(this));
                            }
                        } else if (Class.forName("org.jitsi.gov.nist.core.GenericObject").isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                sprint(((GenericObject) f.get(this)).debugDump(this.indentation + 1));
                            } else {
                                sprint("<null>");
                            }
                        } else if (!Class.forName("org.jitsi.gov.nist.core.GenericObjectList").isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                sprint(f.get(this).getClass().getName() + Separators.COLON);
                            } else {
                                sprint(fieldType.getName() + Separators.COLON);
                            }
                            sprint("{");
                            if (f.get(this) != null) {
                                sprint(f.get(this).toString());
                            } else {
                                sprint("<null>");
                            }
                            sprint("}");
                        } else if (f.get(this) != null) {
                            sprint(((GenericObjectList) f.get(this)).debugDump(this.indentation + 1));
                        } else {
                            sprint("<null>");
                        }
                    } catch (IllegalAccessException e) {
                    } catch (ClassNotFoundException ex) {
                        System.out.println("Cound not find " + ex.getMessage());
                        ex.printStackTrace();
                        System.exit(0);
                    }
                }
            }
        }
        sprint("}");
        return this.stringRepresentation;
    }

    public boolean match(Object other) {
        if (other == null) {
            return true;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        GenericObject that = (GenericObject) other;
        Field[] fields = getClass().getDeclaredFields();
        Field[] hisfields = other.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Field g = hisfields[i];
            if (f.getModifiers() != 2) {
                Class fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            if (fname.compareTo("int") == 0) {
                                if (f.getInt(this) != g.getInt(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("short") == 0) {
                                if (f.getShort(this) != g.getShort(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("char") == 0) {
                                if (f.getChar(this) != g.getChar(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("long") == 0) {
                                if (f.getLong(this) != g.getLong(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo(FormField.TYPE_BOOLEAN) == 0) {
                                if (f.getBoolean(this) != g.getBoolean(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("double") == 0) {
                                if (f.getDouble(this) != g.getDouble(that)) {
                                    return false;
                                }
                            } else if (fname.compareTo("float") == 0 && f.getFloat(this) != g.getFloat(that)) {
                                return false;
                            }
                        }
                        Object myObj = f.get(this);
                        Object hisObj = g.get(that);
                        if (hisObj == myObj) {
                            return true;
                        }
                        if (hisObj != null && myObj == null) {
                            return false;
                        }
                        if ((hisObj instanceof String) && (myObj instanceof String)) {
                            if (((String) myObj).compareToIgnoreCase((String) hisObj) != 0) {
                                return false;
                            }
                        } else if (hisObj != null && GenericObject.isMySubclass(myObj.getClass()) && GenericObject.isMySubclass(hisObj.getClass()) && myObj.getClass().equals(hisObj.getClass()) && ((GenericObject) hisObj).getMatcher() != null) {
                            return ((GenericObject) hisObj).getMatcher().match(((GenericObject) myObj).encode());
                        } else if (GenericObject.isMySubclass(myObj.getClass()) && !((GenericObject) myObj).match(hisObj)) {
                            return false;
                        } else {
                            if (GenericObjectList.isMySubclass(myObj.getClass()) && !((GenericObjectList) myObj).match(hisObj)) {
                                return false;
                            }
                        }
                    } catch (IllegalAccessException ex1) {
                        InternalErrorHandler.handleException(ex1);
                    }
                }
            }
        }
        return true;
    }

    public String dbgPrint(int indent) {
        int save = this.indentation;
        this.indentation = indent;
        String retval = toString();
        this.indentation = save;
        return retval;
    }
}
