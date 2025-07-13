package quartzframework.session;

import java.util.Map;

/**
 * Service that provides a key-value storage for each sender.
 */
public interface SessionService<S> {

    /**
     * Return the session of the current sender in the context
     * {@link SenderSession}
     *
     * @return the session of the sender in the context
     */
    Map<String, Object> current();

    /**
     * Return the current session of {@param sender}
     *
     * @param sender the {@link S sender} to get the session from
     * @return the session of {@param sender}
     */
    Map<String, Object> of(S sender);

}