
package me.lucko.helper.version;

import org.bukkit.inventory.meta.SkullMeta;

/**
 * Version specific methods. Feel free to implement this for other versions if needed.
 * <p>
 * Created on Apr 12, 2017.
 * @author FakeNeth
 */
public interface VersionImpl {

	/**
	 * Applies the specified texture to the specified {@link SkullMeta}.
	 * @param meta The {@link SkullMeta}.
	 * @param texture The skull texture URL (or base64 encoded json texture).
	 * @return The modified {@link SkullMeta}.
	 */
	SkullMeta applySkullTexture(final SkullMeta meta, final String texture);

	/**
	 * Gets the skull texutre associated with the specified {@link SkullMeta}.
	 * @param meta The {@link SkullMeta}.
	 * @return The texture url, or null if none is associated.
	 */
	String getSkullTexture(final SkullMeta meta);

}
