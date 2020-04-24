package me.lucko.helper.command.tabcomplete;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CompletionSupplier {

    CompletionSupplier EMPTY = partial -> Collections.emptyList();

    static CompletionSupplier startsWith(String... strings) {
        return startsWith(() -> Arrays.stream(strings));
    }

    static CompletionSupplier startsWith(Collection<String> strings) {
        return startsWith(strings::stream);
    }

    static CompletionSupplier startsWith(Supplier<Stream<String>> stringsSupplier) {
        return partial -> stringsSupplier.get()
                .filter(TabCompleter.startsWithIgnoreCase(partial))
                .collect(Collectors.toList());
    }

    List<String> supplyCompletions(String partial);

}
