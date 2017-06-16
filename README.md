![alt text](https://i.imgur.com/zllxTFp.png "Banner")
# helper [![Build Status](https://ci.lucko.me/job/helper/badge/icon)](https://ci.lucko.me/job/helper/)
A utility to reduce boilerplate code in Bukkit plugins. It gets boring writing the same old stuff again and again. :)

## Feature Overview

* [`Events`](#events) - functional event handling and flexible listener registration
* [`Scheduler`](#scheduler) - scheduler programming with CompletableFutures
* [`Metadata`](#metadata) - metadata with generic types, automatically expiring values and more
* [`Commands`](#commands) - create commands using the builder pattern
* [`Scoreboard`](#scoreboard) - asynchronous scoreboard using ProtocolLib
* [`Plugin Annotations`](#plugin-annotations) - automatically create plugin.yml files for your projects using annotations
* [`GUI`](#gui) - lightweight by highly adaptable and flexible menu abstraction
* [`Menu Scheming`](#menu-scheming) - easily design menu layouts without having to worry about slot ids
* [`Serialization`](#serialization) - immutable and GSON compatible alternatives for common Bukkit objects
* [`Bungee Messaging`](#bungee-messaging) - wrapper for BungeeCord's plugin messaging API

... and much more!

* [**How to add helper to your project**](#using-helper-in-your-project)
* [**Standalone plugin download**](https://ci.lucko.me/job/helper/)


## Features
### [`Events`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/Events.java)
helper adds a functional event handling utility. It allows you to dynamically register event listeners on the fly, without having to break out of logic, or define listeners as their own method.

Instead of *implementing Listener*, creating a *new method* annotated with *@EventHandler*, and *registering* your listener with the plugin manager, with helper, you can subscribe to an event with one simple line of code. This allows you to define multiple listeners in the same class, and register then selectively.

```java
Events.subscribe(PlayerJoinEvent.class).handler(e -> e.setJoinMessage(""));
```

It also allows for more advanced handling. You can set listeners to automatically expire after a set duration, or after they've been called a number of times. When constructing the listener, you can use Java 8 Stream-esque `#filter` predicates to refine the handling, as opposed to lines of `if ... return` statements.

```java
Events.subscribe(PlayerJoinEvent.class)
        .expireAfter(2, TimeUnit.MINUTES) // expire after 2 mins
        .expireAfter(1) // or after the event has been called 1 time
        .filter(e -> !e.getPlayer().isOp())
        .handler(e -> e.getPlayer().sendMessage("Wew! You were first to join the server since it restarted!"));
```

The implementation provides a selection of default filters.
```java
Events.subscribe(PlayerMoveEvent.class, EventPriority.MONITOR)
        .filter(Events.DEFAULT_FILTERS.ignoreCancelled())
        .filter(Events.DEFAULT_FILTERS.ignoreSameBlock())
        .handler(e -> {
            // handle
        });
```

You can also merge events together into the same handler, without having to define the handler twice.
```java
Events.merge(PlayerEvent.class, PlayerQuitEvent.class, PlayerKickEvent.class)
        .filter(e -> !e.getPlayer().isOp())
        .handler(e -> {
            Bukkit.broadcastMessage("Player " + e.getPlayer() + " has left the server!");
        });
```

This also works when events don't share a common interface or super class.
```java
Events.merge(Player.class)
        .bindEvent(PlayerDeathEvent.class, PlayerDeathEvent::getEntity)
        .bindEvent(PlayerQuitEvent.class, PlayerEvent::getPlayer)
        .handler(e -> {
            // poof!
            e.getLocation().getWorld().createExplosion(e.getLocation(), 1.0f);
        });
```

Events handling can be done alongside the Cooldown system, allowing you to easily define time restrictions on certain events.
```java
Events.subscribe(PlayerInteractEvent.class)
        .filter(e -> e.getAction() == Action.RIGHT_CLICK_AIR)
        .filter(PlayerInteractEvent::hasItem)
        .filter(e -> e.getItem().getType() == Material.BLAZE_ROD)
        .withCooldown(
                CooldownCollection.create(t -> t.getPlayer().getName(), Cooldown.of(10, TimeUnit.SECONDS)),
                (cooldown, e) -> {
                    e.getPlayer().sendMessage("This gadget is on cooldown! (" + cooldown.remainingTime(TimeUnit.SECONDS) + " seconds left)");
                })
        .handler(e -> {
            // play some spooky gadget effect
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.CAT_PURR, 1.0f, 1.0f);
            e.getPlayer().playEffect(EntityEffect.FIREWORK_EXPLODE);
        });
```


### [`Scheduler`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/Scheduler.java)
The scheduler class provides easy static access to the Bukkit Scheduler. All future methods return `CompletableFuture`s, allowing for easy use of callbacks and use of the Completion Stage API.

It also exposes asynchronous and synchronous `Executor` instances.

```java
Scheduler.runLaterSync(() -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (!player.isOp()) {
            player.sendMessage("Hi!");
        }
    }
}, 10L);
```

It also provides a `Task` class, allowing for fine control over the status of repeating events.
```java
Scheduler.runTaskRepeatingSync(task -> {
    if (task.getTimesRan() >= 10) {
        task.stop();
        return;
    }

    // some repeating task
}, 20L, 20L);
```

Completion stages can be cool too.
```java
Scheduler.callAsync(() -> {

    // Do some expensive lookup or i/o
    Thread.sleep(1000L);

    return "something";
}).thenAcceptAsync(s -> {

    // Back on the main server thread with the result from the lookup
    Bukkit.broadcastMessage(s);

}, Scheduler.sync());
```


### [`Metadata`](https://github.com/lucko/helper/tree/master/src/main/java/me/lucko/helper/metadata)
helper provides an alternate system to the Bukkit Metadata API. The main benefits over Bukkit are the use of generic types and automatically expiring, weak or soft values.

The metadata API can be easily integrated with the Event system, thanks to some default filters.
```java
MetadataKey<Boolean> IN_ARENA_KEY = MetadataKey.createBooleanKey("in-arena");

Events.subscribe(PlayerQuitEvent.class)
        .filter(Events.DEFAULT_FILTERS.playerHasMetadata(IN_ARENA_KEY))
        .handler(e -> {
            // clear their inventory if they were in an arena
            e.getPlayer().getInventory().clear();
        });

Events.subscribe(ArenaEnterEvent.class)
        .handler(e -> Metadata.provideForPlayer(e.getPlayer()).put(IN_ARENA_KEY, true));

Events.subscribe(ArenaLeaveEvent.class)
        .handler(e -> Metadata.provideForPlayer(e.getPlayer()).remove(IN_ARENA_KEY));
```

MetadataKeys can also use generic types with guava's TypeToken.
```java
MetadataKey<Set<UUID>> FRIENDS_KEY = MetadataKey.create("friends-list", new TypeToken<Set<UUID>>(){});

Events.subscribe(PlayerQuitEvent.class)
        .handler(e -> {
            Player p = e.getPlayer();

            Set<UUID> friends = Metadata.provideForPlayer(p).getOrDefault(FRIENDS_KEY, Collections.emptySet());
            for (UUID friend : friends) {
                Player pl = Bukkit.getPlayer(friend);
                if (pl != null) {
                    pl.sendMessage("Your friend " + p.getName() + " has left!"); // :(
                }
            }
        });
```

Values can automatically expire, or be backed with Weak or Soft references.
```java
MetadataKey<Player> LAST_ATTACKER = MetadataKey.create("combat-tag", Player.class);

Events.subscribe(EntityDamageByEntityEvent.class)
        .filter(e -> e.getEntity() instanceof Player)
        .filter(e -> e.getDamager() instanceof Player)
        .handler(e -> {
            Player damaged = ((Player) e.getEntity());
            Player damager = ((Player) e.getDamager());

            Metadata.provideForPlayer(damaged).put(LAST_ATTACKER, ExpiringValue.of(damager, 1, TimeUnit.MINUTES));
        });

Events.subscribe(PlayerDeathEvent.class)
        .handler(e -> {
            Player p = e.getEntity();
            MetadataMap metadata = Metadata.provideForPlayer(p);

            Optional<Player> player = metadata.get(LAST_ATTACKER);
            player.ifPresent(pl -> {
                // give the last attacker all of the players experience levels
                pl.setTotalExperience(pl.getTotalExperience() + p.getTotalExperience());
                p.setTotalExperience(0);
                metadata.remove(LAST_ATTACKER);
            });
        });
```

Unlike Bukkit's system, metadata will be removed automatically when a player leaves the server, meaning you need-not worry about creating accidental memory leaks from left over metadata. The API also supports attaching metadata to blocks, worlds and other entities.


### [`Commands`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/Commands.java)
helper provides a very simple command abstraction, designed to reduce some of the boilerplate needed when writing simple commands.

It doesn't have support for automatic argument parsing, sub commands, or anything like that. It's only purpose is removing the bloat from writing simple commands.

Specifically:
* Checking if the sender is a player/console sender, and then automatically casting.
* Checking for permission status
* Checking for argument usage
* Checking if the sender is able to use the command.
* Easily parsing arguments (not just a String[] like the Bukkit interface)

For example, a simple /msg command condenses down into only a few lines.

```java
Commands.create()
        .assertPermission("message.send")
        .assertPlayer()
        .assertUsage("<player> <message>")
        .assertArgument(0, s -> Bukkit.getPlayerExact(s) != null, "&e{arg} is not online!")
        .handler(c -> {
            Player other = Bukkit.getPlayerExact(c.getArg(0));
            Player sender = c.getSender();
            String message = c.getArgs().subList(1, c.getArgs().size()).stream().collect(Collectors.joining(" "));

            other.sendMessage("[" + sender.getName() + " --> you] " + message);
            sender.sendMessage("[you --> " + sender.getName() + "] " + message);

        })
        .register(this, "msg");
```

All invalid usage/permission/argument messages can be altered when the command is built.
```java
Commands.create()
        .assertConsole("&cNice try ;)")
        .handler(c -> {
            ConsoleCommandSender sender = c.getSender();

            sender.sendMessage("Performing graceful shutdown!");
            Scheduler.runTaskRepeatingSync(task -> {
                int countdown = 5 - task.getTimesRan();

                if (countdown <= 0) {
                    Bukkit.shutdown();
                    return;
                }

                Players.forEach(p -> p.sendMessage("Server restarting in " + countdown + " seconds!"));

            }, 20L, 20L);
        })
        .register(this, "shutdown");
```


### [`Scoreboard`](https://github.com/lucko/helper/tree/master/src/main/java/me/lucko/helper/scoreboard)
helper includes a thread safe scoreboard system, allowing you to easily setup & update custom teams and objectives. It is written directly at the packet level, meaning it can be safely used from asynchronous tasks.

For example....
```java
MetadataKey<PacketScoreboardObjective> SCOREBOARD_KEY = MetadataKey.create("scoreboard", PacketScoreboardObjective.class);

BiConsumer<Player, PacketScoreboardObjective> updater = (p, obj) -> {
    obj.setDisplayName("&e&lMy Server &7(" + Bukkit.getOnlinePlayers().size() + "&7)");
    obj.applyLines(
            "&7Hi and welcome",
            "&f" + p.getName(),
            "",
            "&eRank: &f" + getRankName(p),
            "&eSome data:" + getSomeData(p)
    );
};

Events.subscribe(PlayerJoinEvent.class)
        .handler(e -> {
            // register a new scoreboard for the player when they join
            PacketScoreboard sb = Scoreboard.get();
            PacketScoreboardObjective obj = sb.createPlayerObjective(e.getPlayer(), "null", DisplaySlot.SIDEBAR);
            Metadata.provideForPlayer(e.getPlayer()).put(SCOREBOARD_KEY, obj);

            updater.accept(e.getPlayer(), obj);
        });

Scheduler.runTaskRepeatingAsync(() -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        MetadataMap metadata = Metadata.provideForPlayer(player);
        PacketScoreboardObjective obj = metadata.getOrNull(SCOREBOARD_KEY);
        if (obj != null) {
            updater.accept(player, obj);
        }
    }
}, 3L, 3L);
```


### [`Plugin Annotations`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/plugin/ap/Plugin.java)
With helper, you can automagically create the standard `plugin.yml` files at compile time using annotation processing.

Simply annotate your main class with `@Plugin` and fill in the name and version. The processor will take care of the rest!

```java
@Plugin(name = "MyPlugin", version = "1.0.0")
public class MyPlugin extends JavaPlugin {
    
}
```

The annotation also supports defining load order, setting a description and website, and defining (soft) dependencies. Registering commands and permissions is not necessary with helper, as `ExtendedJavaPlugin` provides a method for registering these at runtime.

```java
@Plugin(
        name = "MyPlugin", 
        version = "1.0",
        description = "A cool plugin",
        load = PluginLoadOrder.STARTUP,
        authors = {"Luck", "Some other guy"},
        website = "www.example.com",
        depends = {@PluginDependency("Vault"), @PluginDependency(value = "ASpecialPlugin", soft = true)},
        loadBefore = {"SomePlugin", "SomeOtherPlugin"}
)
public class MyPlugin extends JavaPlugin {

}
```


### [`GUI`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/menu/Gui.java)
helper provides a very simple yet functional GUI abstraction class.

All you have to do is extend `Gui` and override the `#redraw` method.
```java
public class SimpleGui extends Gui {
    public SimpleGui(Player player) {
        super(player, 1, "&eSome simple gamemode GUI");
    }

    @Override
    public void redraw() {
        addItem(ItemStackBuilder.of(Material.STONE_SWORD)
                .name("&eChange to survival!")
                .enchant(Enchantment.FIRE_ASPECT, 2)
                .lore("")
                .lore("&7Change your gamemode to &dsurvival!")
                .build(() -> getPlayer().setGameMode(GameMode.SURVIVAL)));

        addItem(ItemStackBuilder.of(Material.GRASS)
                .name("&eChange to creative!")
                .lore("")
                .lore("&7Change your gamemode to &dcreative!")
                .build(() -> getPlayer().setGameMode(GameMode.CREATIVE)));

        addItem(ItemStackBuilder.of(Material.GOLD_BOOTS)
                .name("&eChange to adventure!")
                .enchant(Enchantment.PROTECTION_FALL, 3)
                .lore("")
                .lore("&7Change your gamemode to &dadventure!")
                .build(() -> getPlayer().setGameMode(GameMode.ADVENTURE)));
    }
}
```

You can call the `#redraw` method from within click callbacks to easily change the menu structure as players interact with the menu.

ItemStackBuilder provides a number of methods for creating item stacks easily, and can be used anywhere. (not just in GUIs!)

The GUI class also provides a number of methods which allow you to
* Define "fallback" menus to be opened when the current menu is closed
* Setup ticker tasks to run whilst the menu remains open
* Add invalidation tasks to be called when the menu is closed
* Manipulate ClickTypes to only fire events when a certain type is used
* Create automatically paginated views in a "dictionary" style


### [`Menu Scheming`](https://github.com/lucko/helper/tree/master/src/main/java/me/lucko/helper/menu/scheme)
MenuScheme allows you to easily apply layouts to GUIs without having to think about slot ids.
```java
@Override
public void redraw() {
    new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111111111")
            .mask("110000011")
            .mask("100000001")
            .mask("100000001")
            .mask("110000011")
            .mask("111111111")
            .scheme(14, 14, 1, 0, 10, 0, 1, 14, 14)
            .scheme(14, 0, 0, 14)
            .scheme(10, 10)
            .scheme(10, 10)
            .scheme(14, 0, 0, 14)
            .scheme(14, 14, 1, 0, 10, 0, 1, 14, 14)
            .apply(this);
}
```

The above scheme translates into this menu.

![](https://i.imgur.com/sERK75D.png)

The mask values determine which slots in each row will be transformed. The scheme values relate to the data values of the glass panes.

### [`Serialization`](https://github.com/lucko/helper/tree/master/src/main/java/me/lucko/helper/serialize)
helper provides a few classes with are useful when trying to serialize plugin data. It makes use of Google's GSON to convert from Java Objects to JSON.

* [`Position`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/Position.java) - similar to Bukkit's location, but without pitch/yaw
* [`BlockPosition`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/BlockPosition.java) - the location of a block within a world
* [`ChunkPosition`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/ChunkPosition.java) - the location of a chunk within a world
* [`Region`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/Region.java) - the area bounded by two Positions
* [`BlockRegion`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/BlockRegion.java) - the area bounded by two BlockPositions
* [`ChunkRegion`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/ChunkRegion.java) - the area bounded by two ChunkPositions
* [`Direction`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/Direction.java) - the yaw and pitch
* [`Point`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/Point.java) - a position + a direction

And finally, [`Serializers`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/Serializers.java), containing serializers for ItemStacks and Inventories.

There is also an abstraction for conducting file I/O. [`FileStorageHandler`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/serialize/FileStorageHandler.java) is capable of handling the initial creation of storage files, as well as automatically creating backups and saving when the server stops.


### [`Bungee Messaging`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/network/BungeeMessaging.java)
helper provides a wrapper class for the BungeeCord Plugin Messaging API, providing callbacks to read response data.

It handles the messaging channels behind the scenes and simply runs the provided callback when the data is returned.

For example...
```java
// sends the player to server "hub"
Player player;
BungeeMessaging.connect(player "hub");
```

And for calls which return responses, the data is captured automatically and returned via the callback.
```java
// requests the global player count and then broadcasts it to all players
BungeeMessaging.playerCount(BungeeMessaging.ALL_SERVERS, count -> Bukkit.broadcastMessage("There are " + count + " players online!"));
```

The class also provides a way to use the "Forward" channel.
```java
// prepare some data to send
ByteArrayDataOutput buf = ByteStreams.newDataOutput();
buf.writeUTF(getServerName());
buf.writeUTF("Hey!");

// send the data
BungeeMessaging.forward(BungeeMessaging.ONLINE_SERVERS, "my-special-channel", buf);

// listen for any messages sent on the special channel
BungeeMessaging.registerForwardCallback("my-special-channel", buf -> {
    String server = buf.readUTF();
    String message = buf.readUTF();

    Log.info("Server " + server + " says " + message);
    return false;
});
```


## Using helper in your project
You can either install the standalone helper plugin on your server, or shade the classes into your project.

#### Maven
```xml
<repositories>
    <repository>
        <id>luck-repo</id>
        <url>http://repo.lucko.me/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.lucko</groupId>
        <artifactId>helper</artifactId>
        <version>1.5.9</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
repositories {
    maven {
        name "luck-repo"
        url "http://repo.lucko.me/"
    }
}

dependencies {
    compile ("me.lucko:helper:1.5.9")
}
```

#### Ant
Pffft, who uses that.
