package me.lucko.helper.plugin.ap;

import javax.annotation.Nonnull;

public @interface PluginCommand {

    /**
     * The name of a command the plugin wishes to register.
     *
     * @return The name of a command the plugin wishes to register.
     */
    @Nonnull
    String name();

    /**
     * A short description of what the command does.
     *
     * @return A short description of what the command does.
     */
    @Nonnull
    String description() default "";

    /**
     * Alternate command names a user may use instead.
     *
     * @return Alternate command names a user may use instead.
     */
    @Nonnull
    String[] aliases() default {};

    /**
     * The most basic permission node required to use the command.
     *
     * @return The most basic permission node required to use the command.
     */
    @Nonnull
    String permission() default "";

    /**
     * A no-permission message.
     *
     * @return A no-permission message.
     */
    @Nonnull
    String permissionMessage() default "";

    /**
     * A short description of how to use this command.
     *
     * @return A short description of how to use this command.
     */
    @Nonnull
    String usage() default "";
}
