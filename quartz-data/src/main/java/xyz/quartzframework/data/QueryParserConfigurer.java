package xyz.quartzframework.data;

import lombok.RequiredArgsConstructor;
import xyz.quartzframework.beans.support.annotation.Preferred;
import xyz.quartzframework.beans.support.annotation.Provide;
import xyz.quartzframework.data.query.CompositeQueryParser;
import xyz.quartzframework.stereotype.Configurer;

@Configurer(force = true)
@RequiredArgsConstructor
public class QueryParserConfigurer {

    @Provide
    @Preferred
    CompositeQueryParser queryParser() {
        return new CompositeQueryParser();
    }
}