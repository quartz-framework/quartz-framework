package xyz.quartzframework.core.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import xyz.quartzframework.core.bean.annotation.NoProxy;
import xyz.quartzframework.core.bean.annotation.Provide;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;
import xyz.quartzframework.core.context.annotation.ContextBootstrapper;

import java.net.URLClassLoader;

@NoProxy
@ContextBootstrapper
@RequiredArgsConstructor
public class ResourceLoaderContextBootstrapper {

    @Provide
    @ActivateWhenBeanMissing(ResourceLoader.class)
    ResourceLoader resourceLoader(URLClassLoader loader) {
        return new DefaultResourceLoader(loader);
    }
}