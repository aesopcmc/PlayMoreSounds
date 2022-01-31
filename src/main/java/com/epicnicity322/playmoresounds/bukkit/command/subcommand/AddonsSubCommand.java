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

package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.AddonsInventory;
import com.epicnicity322.playmoresounds.bukkit.util.UniqueRunnable;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class AddonsSubCommand extends Command implements Helpable
{
    public static final @NotNull HashSet<PMSAddon> ADDONS_TO_UNINSTALL = new HashSet<>();
    private static @Nullable HashMap<PMSAddon, UUID> uninstallConfirmationUUIDs;

    @Override
    public @NotNull String getName()
    {
        return "addons";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"addon"};
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.addons";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Addons").replace("<label>", label));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("playmoresounds.addons.list")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                lang.send(sender, lang.get("Addons.List.Header"));

                HashSet<PMSAddon> addons = PlayMoreSounds.getAddonManager().getAddons();
                StringBuilder data = new StringBuilder();
                int count = 0;

                for (PMSAddon addon : addons) {
                    data.append(addon.isLoaded() ? "&a" : "&c").append(addon.getDescription().getName());

                    if (++count != addons.size()) {
                        data.append(lang.get("Addons.List.Separator", "&f, "));
                    }
                }

                lang.send(sender, false, data.toString());
            } else if (args[1].equalsIgnoreCase("uninstall")) {
                if (!sender.hasPermission("playmoresounds.addons.uninstall")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }
                if (args.length < 3) {
                    lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "uninstall <addon>"));
                    return;
                }

                String addonName = join(args);

                if (ADDONS_TO_UNINSTALL.removeIf(addon -> addon.getDescription().getName().equals(addonName))) {
                    lang.send(sender, lang.get("Addons.Uninstall.Cancel").replace("<addon>", addonName));
                } else {
                    PMSAddon addon = null;

                    for (PMSAddon a : PlayMoreSounds.getAddonManager().getAddons()) {
                        if (a.getDescription().getName().equals(addonName)) {
                            addon = a;
                            break;
                        }
                    }

                    if (addon == null) {
                        lang.send(sender, lang.get("Addons.Error.Not Found").replace("<addon>", addonName).replace("<label>", label));
                        return;
                    }

                    if (uninstallConfirmationUUIDs == null) uninstallConfirmationUUIDs = new HashMap<>();

                    UUID uuid = uninstallConfirmationUUIDs.get(addon);

                    if (uuid == null) {
                        UUID newUUID = UUID.randomUUID();

                        uninstallConfirmationUUIDs.put(addon, newUUID);
                        uuid = newUUID;
                    }

                    PMSAddon finalAddon = addon;
                    ConfirmSubCommand.addPendingConfirmation(sender, new UniqueRunnable(uuid)
                    {
                        @Override
                        public void run()
                        {
                            ADDONS_TO_UNINSTALL.add(finalAddon);
                            uninstallConfirmationUUIDs.remove(finalAddon);
                            lang.send(sender, lang.get("Addons.Uninstall.Success").replace("<addon>", addonName));
                        }
                    }, lang.get("Addons.Uninstall.Confirmation.Description").replace("<addon>", addonName));
                    lang.send(sender, lang.get("Addons.Uninstall.Confirmation.Chat").replace("<addon>", addonName).replace("<label>", label));
                }
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "<list|uninstall>"));
            }

            return;
        }

        if (!(sender instanceof HumanEntity)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }
        if (!sender.hasPermission("playmoresounds.addons.inventory")) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "<list|uninstall>"));
            return;
        }

        new AddonsInventory().openInventory((HumanEntity) sender);
    }

    private String join(String[] args)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 2; i < args.length; ++i)
            builder.append(" ").append(args[i]);

        return builder.toString().trim();
    }
}