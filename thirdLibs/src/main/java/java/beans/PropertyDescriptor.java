package java.beans;

import java.lang.reflect.Method;

public class PropertyDescriptor {
    private String name;
    private Class<?> objectClass;

    public PropertyDescriptor(String name, Class<?> objectClass) {
        this.name = name;
        this.objectClass = objectClass;
    }

    public Class<?> getPropertyType() {
        Class<?> cls = null;
        try {
            return this.objectClass.getMethod("get" + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1), new Class[0]).getReturnType();
        } catch (NoSuchMethodException | SecurityException e) {
            return cls;
        }
    }

    public Method getWriteMethod() {
        Method method = null;
        try {
            return this.objectClass.getMethod("set" + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1), new Class[]{getPropertyType()});
        } catch (NoSuchMethodException | SecurityException e) {
            return method;
        }
    }
}
