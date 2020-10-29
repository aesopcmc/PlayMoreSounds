/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class ConfirmSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull HashMap<CommandSender, LinkedHashMap<Runnable, String>> pendingConfirmation = new HashMap<>();

    public static void addPendingConfirmation(@NotNull CommandSender sender, @NotNull Runnable confirmation, @NotNull String description)
    {
        LinkedHashMap<Runnable, String> confirmations;

        if (pendingConfirmation.containsKey(sender))
            confirmations = pendingConfirmation.get(sender);
        else
            confirmations = new LinkedHashMap<>();

        confirmations.put(confirmation, description);
        pendingConfirmation.put(sender, confirmations);
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Confirm").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "confirm";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.confirm";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        LinkedHashMap<Runnable, String> confirmations = pendingConfirmation.get(sender);

        if (confirmations == null || confirmations.isEmpty()) {
            lang.send(sender, lang.get("Confirm.Error.Nothing Pending"));
            return;
        }

        long id = 1;

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                lang.send(sender, lang.get("Confirm.List.Header"));

                for (Map.Entry<Runnable, String> confirmation : confirmations.entrySet())
                    lang.send(sender, false, lang.get("Confirm.List.Confirmation").replace("<id>", Long.toString(id++))
                            .replace("<description>", confirmation.getValue()));

                return;
            } else if (StringUtils.isNumeric(args[1])) {
                id = Long.parseLong(args[1]);
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", "[list|id]"));
                return;
            }
        }

        long l = 1;

        for (Runnable runnable : new LinkedHashSet<>(confirmations.keySet())) {
            if (l++ == id) {
                runnable.run();
                confirmations.remove(runnable);
                break;
            }
        }
    }
}
