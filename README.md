# helper [![Build Status](https://ci.lucko.me/job/helper/badge/icon)](https://ci.lucko.me/job/helper/)
A utility to reduce boilerplate code in Bukkit plugins. This is just for my own use, but is open source'd on the off chance that someone else can make use of some of it.

## Features
### [`Events`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/Events.java)
helper adds a functional event handling utility. The class lets you subscribe to an event with one simple line of code.
```java
Events.subscribe(PlayerJoinEvent.class).handler(e -> e.setJoinMessage(""));
```

It also allows for more advanced handling.
```java
Events.subscribe(PlayerJoinEvent.class)
        .expireAfter(2, TimeUnit.MINUTES) // expire after 2 mins
        .expireAfter(1) // or after the event has been called 1 time
        .filter(e -> !e.getPlayer().isOp())
        .handler(e -> e.getPlayer().sendMessage("Wew! You were first to join the server since it restarted!"));
```

The implementation provides a couple of default filters.
```java
Events.subscribe(PlayerMoveEvent.class, EventPriority.MONITOR)
        .filter(Events.DEFAULT_FILTERS.ignoreCancelled())
        .filter(Events.DEFAULT_FILTERS.ignoreSameBlock())
        .handler(e -> {
            // handle
        });
```

You can also merge events together into the same handler.
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

You can also use the built-in cooldown utility to limit how quickly events are listened to.
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
            // play some gadget effect
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.CAT_PURR, 1.0f, 1.0f);
            e.getPlayer().playEffect(EntityEffect.FIREWORK_EXPLODE);
        });
```

### [`Scheduler`](https://github.com/lucko/helper/blob/master/src/main/java/me/lucko/helper/Scheduler.java)
The scheduler class provides easy static access to the Bukkit Scheduler. It also adds methods to retrieve synchronous and asynchronous executor instances. All future methods return `CompletableFuture`s, allowing for easy use of callbacks and use of the Completion Stage API.

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

    someRepeatingTask();
}, 20L, 20L);
```

Completion stages can be cool too.
```java
Scheduler.callAsync(() -> {
    // Some expensive lookup
    Thread.sleep(1000L);
    return "something";
}).thenAcceptAsync(s -> {
    // Some main thread task
    Bukkit.broadcastMessage(s);
}, Scheduler.sync());
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

ItemStackBuilder provides a number of methods for creating item stacks easily, and can be used anywhere. (not just in GUIs!)

The GUI class also provides a number of methods which allow you to
* Define "fallback" menus to be opened when the current menu is closed
* Setup ticker tasks to run whilst the menu remains open
* Add invalidation tasks to be called when the menu is closed
* Manipulate ClickTypes to only fire events when a certain type is used

### Menu Scheming
There is also a menu scheming system, which allows for menus to be easily themed with border items. For example...
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

The mask values determine which slots in each row will be transformed. For example, in row 2, only the first 2 and last 2 slots are transformed. The scheme values relate to the data values of the glass panes. 

## Repo
You can either install the standalone helper plugin your server, or shade the classes into your project.

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
        <version>1.3.2</version>
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
    compile ("me.lucko:helper:1.3.2")
}
```
