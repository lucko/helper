package me.lucko.helper.terminable.module;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;

import javax.annotation.Nonnull;

public interface ClosableTerminableModule extends TerminableModule, Terminable {

    @Override
    void setup(@Nonnull TerminableConsumer consumer);

    @Override
    void close() throws Exception;
}
