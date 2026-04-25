package com.temporal.almacen.service;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CacheRefreshEvent extends ApplicationEvent {

    private final String[] cachesToRefresh;

    public CacheRefreshEvent(Object source, String... cachesToRefresh) {
        super(source);
        this.cachesToRefresh = cachesToRefresh;
    }
}
