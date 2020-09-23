package javax.sound.sampled;

public abstract class Control {
    private Type type;

    public static class Type {
        private String name;

        protected Type(String name) {
            this.name = name;
        }
    }

    public Type getType() {
        return this.type;
    }
}
