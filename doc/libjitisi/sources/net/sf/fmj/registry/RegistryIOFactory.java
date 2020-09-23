package net.sf.fmj.registry;

class RegistryIOFactory {
    public static final int PROPERTIES = 1;
    public static final int XML = 0;

    RegistryIOFactory() {
    }

    public static final RegistryIO createRegistryIO(int type, RegistryContents contents) {
        switch (type) {
            case 0:
                return new XMLRegistryIO(contents);
            case 1:
                return new PropertiesRegistryIO(contents);
            default:
                throw new IllegalArgumentException();
        }
    }
}
