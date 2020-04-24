package me.lucko.helper.command.tabcomplete;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class TabCompleter {

    public static TabCompleter create() {
        return new TabCompleter();
    }

    private final Map<Integer, CompletionSupplier> suppliers = new HashMap<>();
    private int from = Integer.MAX_VALUE;

    private TabCompleter() { }

    public TabCompleter at(int position, CompletionSupplier supplier) {
        Preconditions.checkState(position < this.from);
        this.suppliers.put(position, supplier);
        return this;
    }

    public TabCompleter from(int position, CompletionSupplier supplier) {
        Preconditions.checkState(this.from == Integer.MAX_VALUE);
        this.suppliers.put(position, supplier);
        this.from = position;
        return this;
    }

    public List<String> complete(List<String> args) {
        int lastIndex = 0;
        String partial;

        // nothing entered yet
        if (args.isEmpty() || (partial = args.get((lastIndex = args.size() - 1))).trim().isEmpty()) {
            return getCompletions(lastIndex, "");
        }

        // started typing something
        return getCompletions(lastIndex, partial);
    }

    private List<String> getCompletions(int position, String partial) {
        if (position >= this.from) {
            return this.suppliers.get(this.from).supplyCompletions(partial);
        }

        return this.suppliers.getOrDefault(position, CompletionSupplier.EMPTY).supplyCompletions(partial);
    }

    static Predicate<String> startsWithIgnoreCase(String prefix) {
        return string -> {
            if (string.length() < prefix.length()) {
                return false;
            }
            return string.regionMatches(true, 0, prefix, 0, prefix.length());
        };
    }

}
