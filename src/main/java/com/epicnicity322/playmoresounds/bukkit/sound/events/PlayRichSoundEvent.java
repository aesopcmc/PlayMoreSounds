package com.epicnicity322.playmoresounds.bukkit.sound.events;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a config sound is played. Rich sounds can only be played through configuration sections.
 *
 * @see PlaySoundEvent
 */
public class PlayRichSoundEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Player player;
    private final @NotNull RichSound richSound;
    private boolean cancelled;
    private @NotNull Location location;

    public PlayRichSoundEvent(@Nullable Player player, @NotNull Location location, @NotNull RichSound richSound)
    {
        this.player = player;
        this.location = location;
        this.richSound = richSound;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * This is the source player. This player may be null because the sound may not be played by a player.
     *
     * @return The source player.
     */
    public @Nullable Player getPlayer()
    {
        return player;
    }

    /**
     * This is NOT the final location of the sound. This is the source location, the location where the sound was
     * asked to play. But this location may change depending on radius and on {@link SoundOptions}.
     *
     * @return The source location of the sound.
     */
    public @NotNull Location getLocation()
    {
        return location;
    }

    /**
     * This won't be the final location of the sound. This is the source location, the location where the sound was
     * asked to play. But this location may change depending on radius and on {@link SoundOptions}.
     *
     * @param location The location that you want the sound to take as source.
     */
    public void setLocation(@NotNull Location location)
    {
        this.location = location;
    }

    /**
     * This is the instance of the configuration sound as object. You may use this to check which section was used to play
     * the sound and change the sounds that will play.
     *
     * @return The rich sound object that will play.
     */
    public @NotNull RichSound getRichSound()
    {
        return richSound;
    }
}
