package com.rounak.consistent_hashing.config;

import com.rounak.consistent_hashing.model.Node;
import com.rounak.consistent_hashing.service.HashRingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLoader {

    @Value("${nodeAddresses:}")
    private String nodeAddresses;

    @Autowired
    private HashRingService hashRingService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        if (nodeAddresses.isBlank()) return;

        String[] urls = nodeAddresses.split(",");

        for (int i = 0; i < urls.length; i++) {
            hashRingService.addNode(
                    new Node("Node" + (i+1), urls[i].trim())
            );
        }

        System.out.println("Docker nodes auto-registered");
    }
}
