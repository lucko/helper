/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.extras;

import me.lucko.helper.bossbar.BossBarFactory;
import me.lucko.helper.npc.NpcManager;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.signprompt.SignPromptFactory;

/**
 * Provides some extra utilities which are too specific for inclusion in the main project.
 */
public class ExtrasPlugin extends ExtendedJavaPlugin {

    @Override
    protected void enable() {
        if (getServer().getPluginManager().isPluginEnabled("Citizens")) {
            NpcManager npcManager = bind(new NpcManagerImpl());
            provideService(NpcManager.class, npcManager);
        }

        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            SignPromptFactory signPromptFactory = new SignPromptFactoryImpl();
            provideService(SignPromptFactory.class, signPromptFactory);
        }

        if (getServer().getPluginManager().isPluginEnabled("ViaVersion")) {
            BossBarFactory bossBarFactory = new ViaBossBarFactory();
            provideService(BossBarFactory.class, bossBarFactory);
        } else if (classExists("org.bukkit.boss.BossBar")) {
            BossBarFactory bossBarFactory = new BukkitBossBarFactory(getServer());
            provideService(BossBarFactory.class, bossBarFactory);
        }
    }

    private static boolean classExists(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
