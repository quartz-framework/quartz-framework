package xyz.quartzframework.core.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import xyz.quartzframework.core.annotation.*;
import xyz.quartzframework.core.condition.annotation.ActivateWhenBeanMissing;

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