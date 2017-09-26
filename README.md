![alt text](https://i.imgur.com/zllxTFp.png "Banner")
# helper [![Build Status](https://ci.lucko.me/job/helper/badge/icon)](https://ci.lucko.me/job/helper/)
A utility to reduce boilerplate code in Bukkit plugins. It gets boring writing the same old stuff again and again. :)

### Modules
##### [`helper`](https://github.com/lucko/helper/tree/master/helper): The main helper project
[![Artifact](https://img.shields.io/badge/build-artifact-green.svg)](https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper/target/helper.jar) [![Dependency Info](https://img.shields.io/badge/api-dependency_info-orange.svg)](https://github.com/lucko/helper#helper) [![JavaDoc](https://img.shields.io/badge/api-javadoc-blue.svg)](https://lucko.me/helper/javadoc/helper/)

##### [`helper-sql`](https://github.com/lucko/helper/tree/master/helper-sql): Provides SQL datasources using HikariCP.
[![Artifact](https://img.shields.io/badge/build-artifact-green.svg)](https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper-sql/target/helper-sql.jar) [![Dependency Info](https://img.shields.io/badge/api-dependency_info-orange.svg)](https://github.com/lucko/helper#helper-sql) [![JavaDoc](https://img.shields.io/badge/api-javadoc-blue.svg)](https://lucko.me/helper/javadoc/helper-sql/)

##### [`helper-redis`](https://github.com/lucko/helper/tree/master/helper-redis): Provides Redis clients and implements the helper Messaging system using Jedis.
[![Artifact](https://img.shields.io/badge/build-artifact-green.svg)](https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper-redis/target/helper-redis.jar) [![Dependency Info](https://img.shields.io/badge/api-dependency_info-orange.svg)](https://github.com/lucko/helper#helper-redis) [![JavaDoc](https://img.shields.io/badge/api-javadoc-blue.svg)](https://lucko.me/helper/javadoc/helper-redis/)

##### [`helper-mongo`](https://github.com/lucko/helper/tree/master/helper-mongo): Provides MongoDB datasources.
[![Artifact](https://img.shields.io/badge/build-artifact-green.svg)](https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper-mongo/target/helper-mongo.jar) [![Dependency Info](https://img.shields.io/badge/api-dependency_info-orange.svg)](https://github.com/lucko/helper#helper-mongo) [![JavaDoc](https://img.shields.io/badge/api-javadoc-blue.svg)](https://lucko.me/helper/javadoc/helper-mongo/)

##### [`helper-lilypad`](https://github.com/lucko/helper/tree/master/helper-lilypad): Implements the helper Messaging system using LilyPad.
[![Artifact](https://img.shields.io/badge/build-artifact-green.svg)](https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper-lilypad/target/helper-lilypad.jar)

## Feature Overview

* [`Events`](#events) - functional event handling and flexible listener registration
* [`Scheduler`](#scheduler) - easy access to the Bukkit scheduler
* [`Promise`](#promise) - a chain of operations (Futures) executing between both sync and async threads
* [`Metadata`](#metadata) - metadata with generic types, automatically expiring values and more
* [`Messenger`](#messenger) - message channel abstraction
* [`Commands`](#commands) - create commands using the builder pattern
* [`Scoreboard`](#scoreboard) - asynchronous scoreboard using ProtocolLib
* [`GUI`](#gui) - lightweight by highly adaptable and flexible menu abstraction
* [`Menu Scheming`](#menu-scheming) - easily design menu layouts without having to worry about slot ids
* [`Plugin Annotations`](#plugin-annotations) - automatically create plugin.yml files for your projects using annotations
* [`Maven Annotations`](#maven-annotations) - download & install maven dependencies at runtime
* [`Terminables`](#terminables) - a family of interfaces to help easily manipulate objects which can be unregistered, stopped, or gracefully halted
* [`Serialization`](#serialization) - immutable and GSON compatible alternatives for common Bukkit objects
* [`Bungee Messaging`](#bungee-messaging) - wrapper for BungeeCord's plugin messaging API

... and much more!

* [**How to add helper to your project**](#using-helper-in-your-project)
* [**Standalone plugin download**](https://ci.lucko.me/job/helper/)


## Features
### [`Events`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/Events.java)
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


### [`Scheduler`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/Scheduler.java)
The scheduler class provides easy static access to the Bukkit Scheduler. All future methods return `Promise`s, allowing for easy use of callbacks.

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


### [`Promise`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/promise/Promise.java)
A `Promise` is an object that acts as a proxy for a result that is initially unknown, usually because the computation of its value is yet incomplete.

The concept very closely resembles the Java [`CompletionStage`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html) and [`CompletableFuture`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) APIs.

The main differences between CompletableFutures and Promises are:

* The ability to switch seamlessly between the main 'Server thread' and asynchronous tasks
* The ability to delay an action by a number of game ticks

Promises are really easy to use. To demonstrate how they work, consider this simple reward system. The task flow looks a bit like this.

1. Announce to players that rewards are going to be given out. (sync)
2. Get a list of usernames who are due a reward (async)
3. Convert these usernames to UUIDs (async)
4. Get a set of Player instances for the given UUIDs (sync)
5. Give out the rewards to the online players (sync)
6. Notify the reward storage that these players have been given a reward

Using the Promise API, this might look something like this...

```java
RewardStorage storage = getRewardStorage();

Promise<List<Player>> rewardPromise = Promise.start()
        .thenRunSync(() -> Bukkit.broadcastMessage("Getting ready to reward players in 10 seconds!"))
        // wait 10 seconds, then start
        .thenRunDelayedSync(() -> Bukkit.broadcastMessage("Starting now!"), 200L)
        // get the players which need to be rewarded
        .thenApplyAsync(n -> storage.getPlayersToReward())
        // convert to uuids
        .thenApplyAsync(storage::getUuidsFromUsernames)
        // get players from the uuids
        .thenApplySync(uuids -> {
            List<Player> players = new ArrayList<>();
            for (UUID uuid : uuids) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    players.add(player);
                }
            }
            return players;
        });

// give out the rewards sync
rewardPromise.thenAcceptSync(players -> {
    for (Player player : players) {
        storage.giveReward(player);
    }
});

// notify
rewardPromise.thenAcceptSync(players -> {
    for (Player player : players) {
        storage.announceSuccess(player.getUniqueId());
    }
});
```

However, then consider how this might look if you were just using nested runnables...

```java
RewardStorage storage = getRewardStorage();

Bukkit.getScheduler().runTask(this, () -> {

    Bukkit.broadcastMessage("Getting ready to reward players in 10 seconds!");

    Bukkit.getScheduler().runTaskLater(this, () -> {
        Bukkit.broadcastMessage("Starting now!");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            List<String> playersToReward = storage.getPlayersToReward();
            Set<UUID> uuids = storage.getUuidsFromUsernames(playersToReward);

            Bukkit.getScheduler().runTask(this, () -> {
                List<Player> players = new ArrayList<>();
                for (UUID uuid : uuids) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        players.add(player);
                    }
                }

                for (Player player : players) {
                    storage.giveReward(player);
                }

                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    for (Player player : players) {
                        storage.announceSuccess(player.getUniqueId());
                    }
                });
            });
        });
    }, 200L);
});
```

I'll leave it for you to decide which is better. :smile:


### [`Metadata`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/metadata)
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


### [`Messenger`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/messaging)
helper provides a Messenger abstraction utility, which consists of a few key classes.

* [`Messenger`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/Messenger.java) - an object which manages messaging Channels
* [`Channel`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/Channel.java) - represents an individual messaging channel. Facilitates sending a message to the channel, or creating a ChannelAgent
* [`ChannelAgent`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/ChannelAgent.java) - an agent for interacting with channel messaging streams. Allows you to add/remove ChannelListeners to a channel
* [`ChannelListener`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/ChannelListener.java) - an object listening to messages sent on a given channel

The system is very easy to use, and cuts out a lot of the boilerplate code which usually goes along with using PubSub systems.

As an example, here is a super simple global player messaging system.

```java
public class GlobalMessengerPlugin extends ExtendedJavaPlugin {

    @Override
    public void onEnable() {
        // get the Messenger
        Messenger messenger = getService(Messenger.class);

        // Define the channel data model.
        class PlayerMessage {
            UUID uuid;
            String username;
            String message;

            public PlayerMessage(UUID uuid, String username, String message) {
                this.uuid = uuid;
                this.username = username;
                this.message = message;
            }
        }

        // Get the channel
        Channel<PlayerMessage> channel = messenger.getChannel("pms", PlayerMessage.class);

        // Listen for chat events, and send a message to our channel.
        Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST)
                .filter(Events.DEFAULT_FILTERS.ignoreCancelled())
                .handler(e -> {
                    e.setCancelled(true);
                    channel.sendMessage(new PlayerMessage(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getMessage()));
                });

        // Get an agent from the channel.
        ChannelAgent<PlayerMessage> channelAgent = channel.newAgent();
        channelAgent.register(this);

        // Listen for messages sent on the channel.
        channelAgent.addListener((agent, message) -> {
            Scheduler.runSync(() -> {
                Bukkit.broadcastMessage("Player " + message.username + " says " + message.message);
            });
        });
    }
}
```

You can either integrate messenger into your own existing messaging system (using [`AbstractMessenger`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/AbstractMessenger.java), or, use **helper-redis**, which implements Messenger using Jedis and the Redis PubSub system.


### [`Commands`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/command)
helper provides a very simple command abstraction, designed to reduce some of the boilerplate needed when writing simple commands.

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
        .handler(c -> {
            Player other = c.arg(0).parseOrFail(Player.class);
            Player sender = c.sender();
            String message = c.args().subList(1, c.args().size()).stream().collect(Collectors.joining(" "));

            other.sendMessage("[" + sender.getName() + " --> you] " + message);
            sender.sendMessage("[you --> " + sender.getName() + "] " + message);
        })
        .register(this, "msg");
```

All invalid usage/permission/argument messages can be altered when the command is built.
```java
Commands.create()
        .assertConsole("&cUse the console to shutdown the server!")
        .assertUsage("[countdown]")
        .handler(c -> {
            ConsoleCommandSender sender = c.sender();
            int delay = c.arg(0).parse(Integer.class).orElse(5);

            sender.sendMessage("Performing graceful shutdown!");

            Scheduler.runTaskRepeatingSync(task -> {
                int countdown = delay - task.getTimesRan();

                if (countdown <= 0) {
                    Bukkit.shutdown();
                    return;
                }

                Players.forEach(p -> p.sendMessage("Server restarting in " + countdown + " seconds!"));
            }, 20L, 20L);
        })
        .register(this, "shutdown");
```


### [`Scoreboard`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/scoreboard)
helper includes a thread safe scoreboard system, allowing you to easily setup & update custom teams and objectives. It is written directly at the packet level, meaning it can be safely used from asynchronous tasks.

For example....
```java
MetadataKey<ScoreboardObjective> SCOREBOARD_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);

BiConsumer<Player, ScoreboardObjective> updater = (p, obj) -> {
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
            Scoreboard sb = GlobalScoreboard.get();
            ScoreboardObjective obj = sb.createPlayerObjective(e.getPlayer(), "null", DisplaySlot.SIDEBAR);
            Metadata.provideForPlayer(e.getPlayer()).put(SCOREBOARD_KEY, obj);

            updater.accept(e.getPlayer(), obj);
        });

Scheduler.runTaskRepeatingAsync(() -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        MetadataMap metadata = Metadata.provideForPlayer(player);
        ScoreboardObjective obj = metadata.getOrNull(SCOREBOARD_KEY);
        if (obj != null) {
            updater.accept(player, obj);
        }
    }
}, 3L, 3L);
```


### [`GUI`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/menu/Gui.java)
helper provides a highly adaptable and flexible GUI abstraction class.

All you have to do is extend `Gui` and override the `#redraw` method.

To demonstrate how the class works, I wrote a simple typewriter menu.
```java
public class TypewriterGui extends Gui {

    // the display book
    private static final MenuScheme DISPLAY = new MenuScheme().mask("000010000");

    // the keyboard buttons
    private static final MenuScheme BUTTONS = new MenuScheme()
            .mask("000000000")
            .mask("000000001")
            .mask("111111111")
            .mask("111111111")
            .mask("011111110")
            .mask("000010000");

    // we're limited to 9 keys per line, so add 'P' one line above.
    private static final String KEYS = "PQWERTYUIOASDFGHJKLZXCVBNM";

    private StringBuilder message = new StringBuilder();

    public TypewriterGui(Player player) {
        super(player, 6, "&7Typewriter");
    }

    @Override
    public void redraw() {

        // perform initial setup.
        if (isFirstDraw()) {

            // when the GUI closes, send the resultant message to the player
            bindRunnable(() -> getPlayer().sendMessage("Your typed message was: " + message.toString()));

            // place the buttons
            MenuPopulator populator = BUTTONS.newPopulator(this);
            for (char keyChar : KEYS.toCharArray()) {
                populator.accept(ItemStackBuilder.of(Material.CLAY_BALL)
                        .name("&f&l" + keyChar)
                        .lore("")
                        .lore("&7Click to type this character")
                        .build(() -> {
                            message.append(keyChar);
                            redraw();
                        }));
            }

            // space key
            populator.accept(ItemStackBuilder.of(Material.CLAY_BALL)
                    .name("&f&lSPACE")
                    .lore("")
                    .lore("&7Click to type this character")
                    .build(() -> {
                        message.append(" ");
                        redraw();
                    }));
        }

        // update the display every time the GUI is redrawn.
        DISPLAY.newPopulator(this).accept(ItemStackBuilder.of(Material.BOOK)
                .name("&f" + message.toString() + "&7_")
                .lore("")
                .lore("&f> &7Use the buttons below to type your message.")
                .lore("&f> &7Hit ESC when you're done!")
                .buildItem().build());
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


### [`Menu Scheming`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/menu/scheme)
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

The scheming system can also be used alongside a `MenuPopulator`, which uses the scheme to add items to the Gui programatically.

```java
@Override
public void redraw() {
    MenuScheme scheme = new MenuScheme().mask("000111000");
    MenuPopulator populator = scheme.newPopulator(this);

    populator.accept(ItemStackBuilder.of(Material.PAPER).name("Item 1").buildItem().build());
    populator.accept(ItemStackBuilder.of(Material.PAPER).name("Item 2").buildItem().build());
    populator.accept(ItemStackBuilder.of(Material.PAPER).name("Item 3").buildItem().build());
}
```


### [`Plugin Annotations`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/plugin/ap/Plugin.java)
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


### [`Maven Annotations`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/maven/MavenLibrary.java)
helper includes a system which allows you to magically download dependencies for your plugins at runtime.

This means you don't have to shade MBs of libraries into your jar. It's as simple as adding an annotation to your plugins class.

```java
@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.4.2")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "9.4.1212")
public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onLoad() {

        // Downloads and installs all dependencies into the classloader!
        // Not necessary if you extend helper's "ExtendedJavaPlugin" instead of "JavaPlugin"
        LibraryLoader.loadAll(this);
    }
}
```


### [`Terminables`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/terminable)
Terminables are a way to easily cleanup active objects in plugins when a shutdown or reset is needed.

The system consists of a few key interfaces.

* [`Terminable`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/terminable/Terminable.java) - The main interface. An object that can be unregistered, stopped, or gracefully halted.
* [`TerminableConsumer`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/terminable/TerminableConsumer.java) - An object which binds with and registers Terminables.
* [`CompositeTerminable`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/terminable/composite/CompositeTerminable.java) - An object which itself contains/has a number of Terminables, but does not register them internally.
* [`CompositeTerminableConsumer`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/terminable/composite/CompositeTerminableConsumer.java) - A bit like a TerminableConsumer, just for CompositeTerminables
* [`TerminableRegistry`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/terminable/registry/TerminableRegistry.java) - An object which is a Terminable itself, but also a TerminableConsumer & CompositeTerminableConsumer, all in one!

Terminables are a really important part of helper, as so much of the utility is accessible from a static context. Terminables are a way to tame these floating, globally built handlers, and register them with the plugin instance.

`ExtendedJavaPlugin` implements TerminableConsumer & CompositeTerminableConsumer, which lets you register Terminables and CompositeTerminables to the plugin. These are all terminated automagically when the plugin disables.

To demonstrate, I'll first define a new CompositeTerminable. Think of this as a conventional Listener class in a regular plugin.
```java
public class DemoListener implements CompositeTerminable {

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {

        Events.subscribe(PlayerJoinEvent.class)
                .filter(e -> e.getPlayer().hasPermission("silentjoin"))
                .handler(e -> e.setJoinMessage(null))
                .bindWith(consumer);

        Events.subscribe(PlayerQuitEvent.class)
                .filter(e -> e.getPlayer().hasPermission("silentquit"))
                .handler(e -> e.setQuitMessage(null))
                .bindWith(consumer);

    }
}
```

Notice the `.bindWith(...)` calls? All Terminables have this method added via default in the interface. It lets you register that specific terminable with a consumer.

In order to setup our DemoListener, we need a CompositeTerminableConsumer. Luckily, ExtendedJavaPlugin implements this for us!

```java
public class DemoPlugin extends ExtendedJavaPlugin {

    @Override
    protected void enable() {

        // either of these is fine (but don't use both!)
        new DemoListener().bindWith(this);

        bindComposite(new DemoListener());

    }
}
```

### [`Serialization`](https://github.com/lucko/helper/tree/master/helper/src/main/java/me/lucko/helper/serialize)
helper provides a few classes with are useful when trying to serialize plugin data. It makes use of Google's GSON to convert from Java Objects to JSON.

* [`Position`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/Position.java) - similar to Bukkit's location, but without pitch/yaw
* [`BlockPosition`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/BlockPosition.java) - the location of a block within a world
* [`ChunkPosition`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/ChunkPosition.java) - the location of a chunk within a world
* [`Region`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/Region.java) - the area bounded by two Positions
* [`BlockRegion`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/BlockRegion.java) - the area bounded by two BlockPositions
* [`ChunkRegion`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/ChunkRegion.java) - the area bounded by two ChunkPositions
* [`Direction`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/Direction.java) - the yaw and pitch
* [`Point`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/Point.java) - a position + a direction

And finally, [`Serializers`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/Serializers.java), containing serializers for ItemStacks and Inventories.

There is also an abstraction for conducting file I/O. [`FileStorageHandler`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/serialize/FileStorageHandler.java) is capable of handling the initial creation of storage files, as well as automatically creating backups and saving when the server stops.

It's as simple as creating a class to handle serialization/deserialization, and then calling a method when you want to load/save data.

```java
public class DemoStorageHandler extends FileStorageHandler<Map<String, String>> {
    private static final Splitter SPLITTER = Splitter.on('=');

    public DemoStorageHandler(JavaPlugin plugin) {
        super("demo", ",json", plugin.getDataFolder());
    }

    @Override
    protected Map<String, String> readFromFile(Path path) {
        Map<String, String> data = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            // read all the data from the file.
            reader.lines().forEach(line -> {
                Iterator<String> it = SPLITTER.split(line).iterator();
                data.put(it.next(), it.next());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void saveToFile(Path path, Map<String, String> data) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : data.entrySet()) {
                writer.write(e.getKey() + "=" + e.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Then, to save/load, just create an instance of the handler, and use the provided methods.
```java
DemoStorageHandler handler = new DemoStorageHandler(this);
handler.save(ImmutableMap.of("some key", "some value"));

// or, to save a backup of the previous file too
handler.saveAndBackup(ImmutableMap.of("some key", "some value"));
```

helper also provides a handler which uses Gson to serialize the data.
```java
GsonStorageHandler<List<String>> gsonHandler = new GsonStorageHandler<>("data", ".json", getDataFolder(), new TypeToken<List<String>>(){});
gsonHandler.save(ImmutableList.of("some key", "some value"));
```


### [`Bungee Messaging`](https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/messaging/bungee/BungeeMessaging.java)
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

You will need to add my maven repository to your build script, or install helper locally.
#### Maven
```xml
<repositories>
    <repository>
        <id>luck-repo</id>
        <url>http://repo.lucko.me/</url>
    </repository>
</repositories>
```

#### Gradle
```gradle
repositories {
    maven {
        name "luck-repo"
        url "http://repo.lucko.me/"
    }
}
```

Then, you can add dependencies for each helper module.

### helper
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>me.lucko</groupId>
        <artifactId>helper</artifactId>
        <version>3.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
dependencies {
    compile ("me.lucko:helper:3.0.0")
}
```

### helper-sql
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>me.lucko</groupId>
        <artifactId>helper-sql</artifactId>
        <version>1.0.4</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
dependencies {
    compile ("me.lucko:helper-sql:1.0.4")
}
```

### helper-redis
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>me.lucko</groupId>
        <artifactId>helper-redis</artifactId>
        <version>1.0.5</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
dependencies {
    compile ("me.lucko:helper-redis:1.0.5")
}
```

### helper-mongo
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>me.lucko</groupId>
        <artifactId>helper-mongo</artifactId>
        <version>1.0.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
dependencies {
    compile ("me.lucko:helper-mongo:1.0.2")
}
```