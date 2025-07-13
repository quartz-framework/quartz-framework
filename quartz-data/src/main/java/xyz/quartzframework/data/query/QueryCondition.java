package xyz.quartzframework.data.query;

import lombok.Getter;
import lombok.Setter;

@Getter
public class QueryCondition {

    private final AttributePath attribute;
    private final Operation operation;

    private final String rawCondition;
    private final String rawValue;

    @Setter
    private boolean or = false;

    private final boolean ignoreCase;

    public QueryCondition(
            String rawCondition,
            AttributePath attribute,
            Operation operation,
            String rawValue,
            boolean ignoreCase
    ) {
        this.rawCondition = rawCondition;
        this.attribute = attribute;
        this.operation = operation;
        this.rawValue = rawValue;
        this.ignoreCase = ignoreCase;
    }

    public QueryCondition(
            String rawProperty,
            String attributeName,
            Operation operation,
            String rawValue,
            boolean ignoreCase,
            CaseFunction caseFunction
    ) {
        this(
                rawProperty + " " + operation + " " + rawValue,
                new AttributePath(rawProperty, attributeName, caseFunction),
                operation,
                rawValue,
                ignoreCase
        );
    }

    public String getAttributeName() {
        return attribute.name();
    }

    public String getRawAttribute() {
        return attribute.raw();
    }

    public CaseFunction getCaseFunction() {
        return attribute.caseFunction();
    }

    public boolean isCaseInsensitive() {
        return ignoreCase || attribute.ignoreCase();
    }
}