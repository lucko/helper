
package me.lucko.helper.version;

import org.bukkit.Bukkit;

import com.google.common.base.Throwables;

/**
 * Created on Apr 12, 2017.
 * @author FakeNeth
 */
public class VersionSpecific {

	private static final VersionImpl INSTANCE = load();

	public static VersionImpl get() {
		return INSTANCE;
	}

	private static VersionImpl load() {
		final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		try {
			return (VersionImpl) Class.forName(String.format("me.lucko.helper.version.Version_%s", version)).newInstance();
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Throwables.propagate(e);
			return null;
		}
	}

}
