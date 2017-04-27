
package me.lucko.helper.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utilities for {@link Sound}s.
 * <p>
 * Created on Jan 15, 2017.
 * @author FakeNeth
 */
public class SoundUtil {

	/**
	 * Plays the specified {@link Sound} at the specified {@link Location}.
	 * @param sound The {@link Sound}.
	 * @param location The {@link Location} to play the {@link Sound} at.
	 */
	public static void play(final Sound sound, final Location location) {
		play(sound, 1, 4, location);
	}

	/**
	 * Plays the specified {@link Sound} at the specified {@link Location}.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param location The {@link Location} to play the {@link Sound} at.
	 */
	public static void play(final Sound sound, final Number pitch, final Location location) {
		play(sound, pitch, 4, location);
	}

	/**
	 * Plays the specified {@link Sound} at the specified {@link Location}.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param volume The volume.
	 * @param location The {@link Location} to play the {@link Sound} at.
	 */
	public static void play(final Sound sound, final Number pitch, final Number volume, final Location location) {
		location.getWorld().playSound(location, sound, volume.floatValue(), pitch.floatValue());
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link Player}s.
	 * @param sound The {@link Sound}.
	 * @param players The {@link Player}s.
	 */
	public static void play(final Sound sound, final Player... players) {
		play(sound, 1, 4, players);
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link Player}s.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param players The {@link Player}s.
	 */
	public static void play(final Sound sound, final Number pitch, final Player... players) {
		play(sound, pitch, 4, players);
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link Player}s.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param volume The volume.
	 * @param players The {@link Player}s.
	 */
	public static void play(final Sound sound, final Number pitch, final Number volume, final Player... players) {
		for (Player player : players) {
			player.playSound(player.getLocation(), sound, volume.floatValue(), pitch.floatValue());
		}
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link CommandSender}s.
	 * @param sound The {@link Sound}.
	 * @param senders The {@link CommandSender}s.
	 */
	public static void play(final Sound sound, final CommandSender... senders) {
		play(sound, 1, 4, senders);
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link CommandSender}s.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param senders The {@link CommandSender}s.
	 */
	public static void play(final Sound sound, final Number pitch, final CommandSender... senders) {
		play(sound, pitch, 4, senders);
	}

	/**
	 * Plays the specified {@link Sound} to the specified {@link CommandSender}s.
	 * @param sound The {@link Sound}.
	 * @param pitch The pitch.
	 * @param volume The volume.
	 * @param senders The {@link CommandSender}s.
	 */
	public static void play(final Sound sound, final Number pitch, final Number volume, final CommandSender... senders) {
		for (CommandSender sender : senders) {
			if (sender instanceof Player) {
				final Player player = (Player) sender;
				player.playSound(player.getLocation(), sound, volume.floatValue(), pitch.floatValue());
			}
		}
	}

}
