package me.lucko.helper.terminable;

public final class Terminables {

    public static Terminable combine(Terminable... terminables) {
        if (terminables.length == 0) {
            return Terminable.EMPTY;
        }
        if (terminables.length == 1) {
            return terminables[0];
        }

        TerminableRegistry registry = TerminableRegistry.create();
        for (Terminable terminable : terminables) {
            terminable.register(registry);
        }
        return registry;
    }

    private Terminables() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
