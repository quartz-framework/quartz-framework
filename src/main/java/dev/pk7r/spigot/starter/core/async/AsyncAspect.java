package dev.pk7r.spigot.starter.core.async;

import dev.pk7r.spigot.starter.core.exception.AsyncException;
import dev.pk7r.spigot.starter.core.task.TaskFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class AsyncAspect {

    private final TaskFactory taskFactory;
    
    @Around("@annotation(async)")
    public Object handleAsync(final ProceedingJoinPoint pjp, Async async) {
        log.info("Handling async aspect");
        val executor = async.executorName();
        val method = ((MethodSignature) pjp.getSignature()).getMethod();
        val returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE)) {
            taskFactory.submit(executor, () -> {
                try {
                    pjp.proceed();
                } catch (Throwable throwable) {
                    throw new AsyncException("Exception in @Async void method", throwable);
                }
            });
            return null;
        }
        if (Future.class.isAssignableFrom(returnType) || CompletableFuture.class.isAssignableFrom(returnType)) {
            try {
                return pjp.proceed();
            } catch (Throwable throwable) {
                throw new AsyncException("Exception in @Async method returning Future/CompletableFuture", throwable);
            }
        }
        throw new AsyncException("@Async methods must return void, Future<T> or CompletableFuture<T>");
    }
}