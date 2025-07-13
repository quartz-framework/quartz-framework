package quartzframework.exception;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {

    private final String permission;

    public PermissionDeniedException(String permission, String message) {
        super(message != null ? message : "Sender didn't satisfied the condition: " + permission);
        this.permission = permission;
    }
}