package xyz.quartzframework.session;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import xyz.quartzframework.aop.NoProxy;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@NoProxy
@RequiredArgsConstructor
public abstract class SenderSession<S, P> {

    public static final String CONSOLE_SENDER_ID = "*console*";

    private final Map<Long, String> senderRefs = new ConcurrentHashMap<>();

    /**
     * Set the current sender in the thread context.
     *
     * @param sender The new {@link S} of the context.
     */
    void setSender(S sender) {
        val threadId = Thread.currentThread().getId();
        if (sender == null) {
            senderRefs.remove(threadId);
            return;
        }
        senderRefs.put(threadId, getSenderId(sender));
    }

    /**
     * Convenience method to return the current sender as a {@link P}
     *
     * @return the current {@link P} in the context if present, {@code null} otherwise
     */
    public abstract P getPlayer();

    /**
     * Retrieve the current {@param sender} of the context.
     *
     * @return The current sender of the context.
     */
    public S getSender() {
        val senderRef = senderRefs.get(Thread.currentThread().getId());
        return getSenderFromId(senderRef);
    }

    /**
     * Get the unique id available for the player in the context.
     *
     * @return the sender id
     */
    public String getSenderId() {
        return getSenderId(getSender());
    }

    /**
     * Run a {@param function} with a specific {@param sender} in the context
     *
     * @param sender   The sender to be set at the context
     * @param function The code to be executed
     * @return the value returned by the function
     */
    public <T, SE extends S> T runWithSender(SE sender, Function<SE, T> function) {
        val oldSender = getSender();
        setSender(sender);
        try {
            return function.apply(sender);
        } finally {
            setSender(oldSender);
        }
    }

    /**
     * Run a {@param function} with a specific {@param sender} in the context
     *
     * @param sender   The sender to be set at the context
     * @param supplier The code to be executed
     * @return the value returned by the function
     */
    public <T, SE extends S> T runWithSender(SE sender, Supplier<T> supplier) {
        return runWithSender(sender, (Function<SE, T>) (s) -> supplier.get());
    }

    /**
     * Run a {@param function} with a specific {@param sender} in the context
     *
     * @param sender   The sender to be set at the context
     * @param function The code to be executed
     */
    public <SE extends S> void runWithSender(SE sender, Consumer<SE> function) {
        runWithSender(sender, (s) -> {
            function.accept(s);
            return null;
        });
    }

    /**
     * Run a {@param runnable} with a specific {@param sender} in the context
     *
     * @param sender   The sender to be set at the context
     * @param runnable The code to be executed
     */
    public <SE extends S> void runWithSender(SE sender, Runnable runnable) {
        runWithSender(sender, (s) -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Wrap a {@param supplier} to keep the current context
     *
     * @param supplier The supplier to be wrapped
     * @return The wrapped supplier
     */
    public <T> Supplier<T> wrap(Supplier<T> supplier) {
        val senderId = getSenderId();
        return () -> runWithSender(getSenderFromId(senderId), supplier);
    }

    /**
     * Wrap a {@param callable} to keep the current context
     *
     * @param callable The callable to be wrapped
     * @return The wrapped callable
     */
    public <T> Callable<T> wrap(Callable<T> callable) {
        val senderId = getSenderId();
        return () -> runWithSender(getSenderFromId(senderId), () -> call(callable));
    }

    @SneakyThrows
    private <V> V call(Callable<V> callable) {
        return callable.call();
    }

    /**
     * Wrap a {@param runnable} to keep the current context
     *
     * @param runnable The runnable to be wrapped
     * @return The wrapped runnable
     */
    public Runnable wrap(Runnable runnable) {
        val senderId = getSenderId();
        return () -> runWithSender(getSenderFromId(senderId), runnable);
    }

    /**
     * Wrap a {@param consumer} to keep the current context
     *
     * @param consumer The consumer to be wrapped
     * @return The wrapped consumer
     */
    public <T> Consumer<T> wrap(Consumer<T> consumer) {
        val senderId = getSenderId();
        return (v) -> runWithSender(getSenderFromId(senderId), () -> consumer.accept(v));
    }

    /**
     * Wrap a {@param function} to keep the current context
     *
     * @param function The function to be wrapped
     * @return The wrapped function
     */
    public <T, R> Function<T, R> wrap(Function<T, R> function) {
        val senderId = getSenderId();
        return (v) -> runWithSender(getSenderFromId(senderId), () -> function.apply(v));
    }

    /**
     * Get the unique id available for the {@param sender}.
     * If the server is in online mode, it will return the {@param sender} UUID, otherwise will return the player username in lower case.
     *
     * @param sender the sender to get the id from
     * @return the sender id, null if null sender input
     */
    public abstract String getSenderId(S sender);

    /**
     * Return the {@link S} associated to the {@param id}
     *
     * @param id the id of the sender
     * @return the sender associated with {@param id}, null if null id input
     */
    public abstract S getSenderFromId(String id);
}