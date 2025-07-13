package xyz.quartzframework.data.query;

import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class QuerySubstitution {

    private final boolean named;
    private final boolean literal;
    private final String rawExpression;

    @Nullable
    private final String nameOrIndex;

    @Nullable
    private final Object literalValue;

    public static QuerySubstitution named(String name, String rawExpression) {
        return new QuerySubstitution(true, false, rawExpression, name, null);
    }

    public static QuerySubstitution positional(String index, String rawExpression) {
        return new QuerySubstitution(false, false, rawExpression, index, null);
    }

    public static QuerySubstitution literal(Object value, String rawExpression) {
        return new QuerySubstitution(false, true, rawExpression, null, value);
    }

    private QuerySubstitution(
            boolean named,
            boolean literal,
            String rawExpression,
            @Nullable String nameOrIndex,
            @Nullable Object literalValue
    ) {
        this.named = named;
        this.literal = literal;
        this.rawExpression = rawExpression;
        this.nameOrIndex = nameOrIndex;
        this.literalValue = literalValue;
    }

    public boolean isPositional() {
        return !named && !literal;
    }
}