package xyz.quartzframework.data.query;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.quartzframework.data.storage.StorageDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CompositeQueryParser implements QueryParser {

    private final List<QueryParser> parsers = new ArrayList<>();

    public void register(QueryParser parser) {
        parsers.add(parser);
        log.debug("Registered query parser {}", parser.getClass().getSimpleName());
    }

    @PreDestroy
    public void destroy() {
        parsers.clear();
    }

    @Override
    public DynamicQueryDefinition parse(Method method, StorageDefinition storageDefinition) {
        for (QueryParser parser : parsers) {
            if (parser.getClass().equals(CompositeQueryParser.class)) {
                continue;
            }
            if (parser.supports(method)) {
                return parser.parse(method, storageDefinition);
            }
        }
        throw new IllegalStateException("No QueryParser could handle method: " + method.getName());
    }

    @Override
    public boolean supports(Method method) {
        return true;
    }

    @Override
    public String queryString(Method method) {
        for (QueryParser parser : parsers) {
            if (parser.supports(method)) {
                return parser.queryString(method);
            }
        }
        throw new IllegalStateException("No QueryParser could handle method: " + method.getName());
    }
}