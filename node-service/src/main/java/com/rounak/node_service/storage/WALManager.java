package com.rounak.node_service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class WALManager {

    @Value("${node.id}")
    private String nodeId;

    private Path getWalPath() {

        String basePath = System.getProperty("user.dir");

        File dir = new File(basePath + "/data/" + nodeId);
        dir.mkdirs();

        return Path.of(
                basePath + "/data/" + nodeId + "/wal.log"
        );
    }

    public void write(String key, String value) {
        try {
            Files.write(
                    getWalPath(),
                    (key + ":" + value + "\n").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("WAL write failed");
        }
    }

    public void clear() {
        try {
            Files.write(
                    getWalPath(),
                    new byte[0],
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException("WAL clear failed");
        }
    }
}
