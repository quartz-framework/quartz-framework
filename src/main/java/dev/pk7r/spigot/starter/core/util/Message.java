package dev.pk7r.spigot.starter.core.util;

import de.themoep.minedown.MineDown;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public final class Message {

    private static final Pattern VALID_PARAM = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Override
    public String toString() {
        return source;
    }

    private final String source;

    public Message(String source) {
        this.source = source;
    }

    public Message colored() {
        return new Message(ChatColor.translateAlternateColorCodes('&', source));
    }

    public BaseComponent[] formatted() {
        return new MineDown(Objects.requireNonNull(source)).toComponent();
    }

    public Message set(String param, String value) {
        if (!VALID_PARAM.matcher(param).matches()) {
            return this;
        }
        return new Message(source.replace("{" + param + "}", value));
    }

    public static Message parse(String message) {
        return new Message(message);
    }
}
