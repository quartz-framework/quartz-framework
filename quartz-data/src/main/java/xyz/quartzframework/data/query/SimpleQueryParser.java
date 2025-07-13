package xyz.quartzframework.data.query;

import xyz.quartzframework.data.storage.StorageDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleQueryParser implements QueryParser {

    private final List<QueryParser> parsers = new ArrayList<>();

    public SimpleQueryParser(QueryParser... parsers) {
        this.parsers.addAll(Arrays.asList(parsers));
        this.parsers.add(new MethodQueryParser());
        this.parsers.add(new QQLQueryParser());
    }

    @Override
    public DynamicQueryDefinition parse(Method method, StorageDefinition storageDefinition) {
        for (QueryParser parser : parsers) {
            if (parser.supports(method)) {
                return parser.parse(method, storageDefinition);
            }
        }
        throw new IllegalStateException("No QueryParser could handle method: " + method.getName());
    }

    @Override
    public boolean supports(Method method) {
        return parsers.stream().anyMatch(p -> p.supports(method));
    }

    @Override
    public String queryString(Method method) {
        for (QueryParser parser : parsers) {
            if (parser.supports(method)) {
                return parser.queryString(method);
            }
        }
        throw new IllegalStateException("No QueryParser could handle method query string: " + method.getName());
    }
}