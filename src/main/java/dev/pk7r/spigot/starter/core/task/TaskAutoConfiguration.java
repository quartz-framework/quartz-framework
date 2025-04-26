package dev.pk7r.spigot.starter.core.task;

import dev.pk7r.spigot.starter.core.annotation.AutoConfiguration;
import dev.pk7r.spigot.starter.core.annotation.Bean;
import dev.pk7r.spigot.starter.core.annotation.Value;
import dev.pk7r.spigot.starter.core.annotation.condition.ConditionalOnMissingBean;
import lombok.val;

@AutoConfiguration(force = true)
public class TaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ScheduledTaskExecutorService.class)
    ScheduledTaskExecutorService scheduledTaskExecutorService(@Value("${plugin.default-task-pool.size:5}") int poolSize) {
        return new DefaultScheduledTaskExecutorService(poolSize);
    }

    @Bean
    @ConditionalOnMissingBean(TaskFactory.class)
    TaskFactory taskFactory(ScheduledTaskExecutorService scheduledTaskExecutorService) {
        val factory = new DefaultTaskFactory();
        factory.register("default", scheduledTaskExecutorService);
        return factory;
    }
}
