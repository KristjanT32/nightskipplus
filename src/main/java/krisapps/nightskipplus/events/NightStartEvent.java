package krisapps.nightskipplus.events;


import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NightStartEvent extends Event {

    static final HandlerList handlers = new HandlerList();

    private World world;

    public NightStartEvent(World world) {
        this.world = world;
    }

    static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public World getWorld() {
        return world;
    }
}
