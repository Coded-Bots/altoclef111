package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import baritone.api.BaritoneAPI;
import baritone.api.process.IFollowProcess;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class KillAndLootTask extends ResourceTask {

    private final Class _toKill;

    private final Task _killTask;
    private IFollowProcess _b;

    private List<Class<? extends Entity>> classes = new ArrayList<>();

    public KillAndLootTask(Class toKill, Predicate<Entity> shouldKill, ItemTarget... itemTargets) {
        super(itemTargets.clone());
        _toKill = toKill;
        _killTask = new KillEntitiesTask(shouldKill, _toKill);
    }

    public KillAndLootTask(Class toKill, ItemTarget... itemTargets) {
        super(itemTargets.clone());
        _toKill = toKill;
        _killTask = new KillEntitiesTask(_toKill);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        classes.add((Class<? extends Entity>) _toKill);
        //_b = BaritoneAPI.getProvider().getPrimaryBaritone().getFollowProcess();
        //_b.follow( e -> classes.stream().anyMatch(c -> c.isInstance(e)));
        mod.getClientBaritone().getFollowProcess().follow( e -> classes.stream().anyMatch(c -> c.isInstance(e)));
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (!mod.getEntityTracker().entityFound(_toKill)) {
            if (isInWrongDimension(mod)) {
                setDebugState("Going to correct dimension.");
                return getToCorrectDimensionTask(mod);
            }
            setDebugState("Searching for mob...");
            //if (_b!=null && _b.isActive()) return null;
            if (mod.getClientBaritone().getFollowProcess().isActive()) return null;
            //return new TimeoutWanderTask(9999999);
        }
        // We found the mob!
        return _killTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getFollowProcess().onLostControl();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof KillAndLootTask task) {
            return task._toKill.equals(_toKill);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Collect items from " + _toKill.toGenericString();
    }
}
