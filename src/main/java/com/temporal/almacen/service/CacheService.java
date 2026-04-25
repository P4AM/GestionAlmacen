package com.temporal.almacen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * Actualiza quirúrgicamente un elemento dentro de una lista cacheada.
     * Si la lista no existe en caché, no hace nada (se llenará en la próxima
     * petición).
     */
    public <T> void updateListInCache(String cacheName, T newItem, Function<T, Object> idExtractor) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null)
            return;

        try {
            // Obtenemos la lista actual (usamos SimpleKey.EMPTY porque @Cacheable sin
            // argumentos usa eso)
            List<T> currentList = cache.get(SimpleKey.EMPTY, List.class);

            if (currentList != null) {
                // Creamos una nueva lista para evitar problemas de inmutabilidad si los hubiera
                List<T> updatedList = new ArrayList<>(currentList);

                Object newId = idExtractor.apply(newItem);

                // Eliminamos la versión vieja si existe y añadimos la nueva
                updatedList.removeIf(item -> idExtractor.apply(item).equals(newId));
                updatedList.add(newItem);

                // Re-ordenar alfabéticamente si es posible (asumimos que tienen nombre para
                // este ERP)
                try {
                    updatedList.sort((a, b) -> {
                        try {
                            String nameA = (String) a.getClass().getMethod("getNombre").invoke(a);
                            String nameB = (String) b.getClass().getMethod("getNombre").invoke(b);
                            return nameA.compareToIgnoreCase(nameB);
                        } catch (Exception e) {
                            return 0;
                        }
                    });
                } catch (Exception e) {
                    /* No es sorteable por nombre, ignoramos */ }

                // Guardamos la lista actualizada de vuelta en la RAM
                cache.put(SimpleKey.EMPTY, updatedList);
                log.debug("Caché '{}' actualizada incrementalmente para el ID: {}", cacheName, newId);
            }
        } catch (Exception e) {
            log.error("Error actualizando caché incremental '{}': {}", cacheName, e.getMessage());
            // Si algo falla, lo mejor es invalidar para asegurar consistencia
            cache.evict(SimpleKey.EMPTY);
        }
    }

    /**
     * Elimina un elemento específico de una lista cacheada.
     */
    public <T> void removeFromListInCache(String cacheName, Object id, Function<T, Object> idExtractor) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null)
            return;

        List<T> currentList = cache.get(SimpleKey.EMPTY, List.class);
        if (currentList != null) {
            List<T> updatedList = new ArrayList<>(currentList);
            updatedList.removeIf(item -> idExtractor.apply(item).equals(id));
            cache.put(SimpleKey.EMPTY, updatedList);
        }
    }

    /**
     * Limpia una caché por completo.
     */
    public void evict(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
