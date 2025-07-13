package xyz.quartzframework.data.storage;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import xyz.quartzframework.beans.factory.QuartzBeanFactory;
import xyz.quartzframework.data.annotation.Storage;
import xyz.quartzframework.data.annotation.SuperStorage;
import xyz.quartzframework.data.query.QueryParser;
import xyz.quartzframework.data.util.GenericTypeUtil;
import xyz.quartzframework.data.util.ProxyFactoryUtil;

import java.net.URLClassLoader;
import java.util.Arrays;

@RequiredArgsConstructor
public class DefaultStorageFactory implements StorageFactory {

    private final QueryParser queryParser;

    private final URLClassLoader classLoader;

    private final QuartzBeanFactory beanFactory;

    @Override
    @SuppressWarnings("unchecked")
    public <E, ID> SimpleStorage<E, ID> create(Class<? extends SimpleStorage<E, ID>> storageInterface) {
        if (!storageInterface.isAnnotationPresent(Storage.class)) {
            throw new IllegalArgumentException(storageInterface.getName() + " is not annotated with @Storage");
        }
        Class<?> superInterface = Arrays.stream(storageInterface.getInterfaces())
                .filter(i -> i.isAnnotationPresent(SuperStorage.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @SuperStorage found in " + storageInterface.getName()));
        val annotation = superInterface.getAnnotation(SuperStorage.class);
        if (annotation == null) {
            throw new IllegalArgumentException(storageInterface.getName() + " is not annotated with @SuperStorage");
        }
        Class<?> implClass = annotation.value();
        Class<?>[] types = GenericTypeUtil.resolve(storageInterface, SimpleStorage.class);
        if (types == null || types.length != 2) {
            throw new IllegalArgumentException(storageInterface.getName() + " is not a supported storage interface");
        }
        Class<E> entityType = (Class<E>) types[0];
        Class<ID> idType = (Class<ID>) types[1];
        Object bean = beanFactory.getBean(implClass);
        if (bean instanceof StorageProvider provider) {
            val target = provider.create(entityType, idType);
            val interceptors = Arrays.stream(annotation.interceptors()).map(beanFactory::getBean).toArray(MethodInterceptor[]::new);
            val proxyFactory = ProxyFactoryUtil.createProxyFactory(queryParser, target, entityType, storageInterface, provider.getQueryExecutor(target), interceptors);
            return (SimpleStorage<E, ID>) proxyFactory.getProxy(classLoader);
        }
        throw new IllegalStateException("Provided class " + implClass.getName() + " is not a StorageProvider");
    }

    @Override
    public Class<?> resolveEntityType(Class<?> storageInterface) {
        Class<?>[] types = GenericTypeUtil.resolve(storageInterface, SimpleStorage.class);
        if (types != null && types.length == 2) {
            return types[0];
        }
        throw new IllegalArgumentException("Cannot resolve entity type from: " + storageInterface.getName());
    }

    @Override
    public Class<?> resolveIdType(Class<?> storageInterface) {
        Class<?>[] types = GenericTypeUtil.resolve(storageInterface, SimpleStorage.class);
        if (types != null && types.length == 2) {
            return types[1];
        }
        throw new IllegalArgumentException("Cannot resolve ID type from: " + storageInterface.getName());
    }
}