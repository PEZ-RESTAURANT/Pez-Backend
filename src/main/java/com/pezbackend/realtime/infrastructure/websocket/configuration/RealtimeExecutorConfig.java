package com.pezbackend.realtime.infrastructure.websocket.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para habilitar la ejecución asíncrona de eventos.
 * Proporciona un pool de hilos específico para el envío en tiempo real.
 */
@Configuration
@EnableAsync
public class RealtimeExecutorConfig {

    /**
     * Define el TaskExecutor personalizado para la propagación de eventos en tiempo real.
     */
    @Bean(name = "realtimeEventExecutor")
    public Executor realtimeEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("WsRealtimeThread-");
        executor.initialize();
        return executor;
    }
}
