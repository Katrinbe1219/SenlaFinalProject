package org.example.core;

import jakarta.transaction.Transactional;
import org.example.core.services.RecalculationService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRuunerCustom implements ApplicationListener<ContextRefreshedEvent> {
    private final RecalculationService service;
    public ApplicationRuunerCustom(RecalculationService service) {
        this.service = service;
    }
    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        service.recalculationScheduled();
    }
}
