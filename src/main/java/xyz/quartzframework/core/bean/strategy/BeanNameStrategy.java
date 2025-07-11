package xyz.quartzframework.core.bean.strategy;

import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.definition.metadata.TypeMetadata;

@NoProxy
public interface BeanNameStrategy {

    String generateBeanName(TypeMetadata metadata);

}