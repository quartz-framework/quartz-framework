package xyz.quartzframework.beans.strategy;

import xyz.quartzframework.aop.NoProxy;
import xyz.quartzframework.beans.definition.metadata.TypeMetadata;

@NoProxy
public interface BeanNameStrategy {

    String generateBeanName(TypeMetadata metadata);

}