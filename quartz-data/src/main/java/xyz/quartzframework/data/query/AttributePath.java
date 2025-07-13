package xyz.quartzframework.data.query;

public record AttributePath(String raw, String name, CaseFunction caseFunction) {

    public boolean ignoreCase() {
        return caseFunction != CaseFunction.NONE;
    }

    public String applyCaseFunction(String value) {
        if (value == null) return null;
        return switch (caseFunction) {
            case LOWER -> value.toLowerCase();
            case UPPER -> value.toUpperCase();
            case NONE -> value;
        };
    }
}