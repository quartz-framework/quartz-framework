package xyz.quartzframework.beans.strategy;

import org.springframework.beans.factory.annotation.Qualifier;
import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;
import xyz.quartzframework.beans.support.annotation.NamedInstance;

@NoProxy
public class DefaultBeanNameStrategy implements BeanNameStrategy {

    @Override
    public String generateBeanName(TypeMetadata metadata) {
        return metadata
                .getAnnotation(NamedInstance.class)
                .or(() -> metadata.getAnnotation(Qualifier.class))
                .map(a -> a.getAttribute("value", String.class))
                .orElseGet(() -> {
                    if (metadata.isMethodBean()) {
                        return metadata.getDeclaredByClass() + "#" + metadata.getDeclaredByMethod();
                    } else {
                        return lowerCamelCase(metadata.getRawName());
                    }
                });
    }

    private String lowerCamelCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}