// src/main/java/com/tickets/backend/config/CacheConfig.java
package com.tickets.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "usuariosActivos",
            "usuariosInactivos", 
            "tecnicosActivos",
            "tecnicosPorCategoria",
            "supervisoresPorCategoria",
            "usuariosActivosBusqueda",
            "usuariosInactivosBusqueda"
        );
        return cacheManager;
    }
}