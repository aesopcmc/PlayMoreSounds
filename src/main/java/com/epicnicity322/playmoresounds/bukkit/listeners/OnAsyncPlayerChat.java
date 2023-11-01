/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public final class OnAsyncPlayerChat extends PMSListener {
    private static final @NotNull Logger logger = new Logger("&f[&4PlayMoreSounds&f]");

    private final @NotNull HashMap<String, HashSet<PlayableRichSound>> filtersAndCriteria = new HashMap<>();

    public OnAsyncPlayerChat(@NotNull PlayMoreSounds plugin) {
        super(plugin);
    }

    static boolean matchesFilter(String filter, String criteria, String message) {
        return switch (filter) {
            case "Starts With" -> message.startsWith(criteria);
            case "Ends With" -> message.endsWith(criteria);
            case "Contains SubString" -> message.toLowerCase().contains(criteria.toLowerCase());
            case "Contains" -> message.toLowerCase().matches(".*\\b" + Pattern.quote(criteria.toLowerCase()) + "\\b.*");
            case "Equals Ignore Case" -> message.equalsIgnoreCase(criteria);
            case "Equals Exactly" -> message.equals(criteria);
            default -> false;
        };
    }

    @Override
    public @NotNull String getName() {
        return "Player Chat";
    }

    @Override
    public void load() {
        filtersAndCriteria.clear();

        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var chatTriggers = Configurations.CHAT_SOUNDS.getConfigurationHolder().getConfiguration();

        for (Map.Entry<String, Object> filter : chatTriggers.getNodes().entrySet()) {
            if (filter.getValue() instanceof ConfigurationSection filterSection) {
                HashSet<PlayableRichSound> criteria = new HashSet<>();

                for (Map.Entry<String, Object> criterion : filterSection.getNodes().entrySet()) {
                    if (criterion.getValue() instanceof ConfigurationSection criterionSection) {

                        if (criterionSection.getBoolean("Enabled").orElse(false)) {
                            criteria.add(getRichSound(criterionSection));
                        }
                    }
                }

                if (!criteria.isEmpty()) filtersAndCriteria.put(filter.getKey(), criteria);
            }
        }

        setRichSound(getRichSound(sounds.getConfigurationSection(getName())));

        if (getRichSound() != null || !filtersAndCriteria.isEmpty()) {
            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        //logger.log("监听到玩家输入消息1");
        var message = event.getMessage();
        var player = event.getPlayer();
        boolean defaultSound = getRichSound() != null;

        filterLoop:
        for (Map.Entry<String, HashSet<PlayableRichSound>> filter : filtersAndCriteria.entrySet()) {
            //logger.log("进入循环filtersAndCriteria");
            for (var criteria : filter.getValue()) {
                ConfigurationSection criteriaSection = criteria.getSection();

                if (!event.isCancelled() || !criteria.isCancellable()) {
                    if (matchesFilter(filter.getKey(), criteriaSection.getName(), message)) {
                        Bukkit.getScheduler().runTask(plugin, () -> criteria.play(player));

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                            defaultSound = false;

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Other Filters").orElse(false))
                            break filterLoop;
                    }
                }
            }
        }
        //logger.log("监听播放参数： defaultSound:"+ defaultSound + ", isCancelled:"+!event.isCancelled() +", isCancellable:"+!getRichSound().isCancellable());
        if (defaultSound && (!event.isCancelled() || !getRichSound().isCancellable())) {
            //logger.log("播放111");
            Bukkit.getScheduler().runTask(plugin, () -> getRichSound().play(player));
        }
    }
}
