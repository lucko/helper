package me.lucko.helper.command;

import java.util.HashMap;

public class CooldownProvider {
    private static HashMap<String, Long> cooldowns = new HashMap<>();

    /**
     * Add a user command to the cooldowns map.
     * @param s Identifying string.
     * @param l Expiry time.
     */
    public static void addCooldown(String s, Long l) {cooldowns.put(s, l);}

    /**
     * Returns expiry time.
     * @param s Identifying string.
     * @return
     */
    public static Long getCooldown(String s) {return cooldowns.get(s);}

    /**
     * Removes an entry from the cooldowns map.
     * @param s Identifying string.
     */
    public static void removeCooldown(String s) {cooldowns.remove(s);}
}
