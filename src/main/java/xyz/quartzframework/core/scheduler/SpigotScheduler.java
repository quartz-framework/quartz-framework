package xyz.quartzframework.core.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.quartzframework.core.annotation.Inject;
import xyz.quartzframework.core.annotation.Injectable;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanPresent;
import xyz.quartzframework.core.session.PlayerSession;

@Injectable
@ActivateWhenBeanPresent(BukkitScheduler.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpigotScheduler implements Scheduler {

    private final PlayerSession session;

    private final Plugin plugin;

    private final BukkitScheduler scheduler;

    @Override
    public int scheduleSyncDelayedTask(Runnable task, long delay) {
        return scheduler.scheduleSyncDelayedTask(plugin, session.wrap(task), delay);
    }

    @Override
    public int scheduleSyncDelayedTask(Runnable task) {
        return scheduler.scheduleSyncDelayedTask(plugin, session.wrap(task));
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        return scheduler.scheduleSyncRepeatingTask(plugin, session.wrap(task), delay, period);
    }

    @Override
    public void cancelTask(int taskId) {
        scheduler.cancelTask(taskId);
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        return scheduler.isCurrentlyRunning(taskId);
    }

    @Override
    public boolean isQueued(int taskId) {
        return scheduler.isQueued(taskId);
    }
}