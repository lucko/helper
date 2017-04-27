
package me.lucko.helper.version;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.base.Throwables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * Created on Apr 12, 2017.
 * @author FakeNeth
 */
public class Version_1_11_R1 implements VersionImpl {

	private static Class<?> skullMetaClass;
	private static Field profileField;

	static {
		try {
			skullMetaClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
			profileField = skullMetaClass.getDeclaredField("profile");
			profileField.setAccessible(true);
		}
		catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public SkullMeta applySkullTexture(final SkullMeta meta, final String texture) {

		final GameProfile profile = new GameProfile(UUID.randomUUID(), null);

		byte[] encodedData = null;

		if (StringUtils.containsIgnoreCase(texture, "textures.minecraft.net/texture")) {
			encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", texture).getBytes());
		}
		else {
			encodedData = texture.getBytes();
		}

		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

		try {
			profileField.set(meta, profile);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			Throwables.propagate(e);
		}

		return meta;
	}

	@Override
	public String getSkullTexture(final SkullMeta meta) {

		GameProfile profile = null;
		try {
			profile = (GameProfile) profileField.get(meta);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			Throwables.propagate(e);
		}

		for (final Property property : profile.getProperties().get("textures")) {
			if (property.getName().equals("textures") && !property.hasSignature()) return property.getValue();
		}

		return null;
	}

}
