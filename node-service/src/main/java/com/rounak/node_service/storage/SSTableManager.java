package com.rounak.node_service.storage;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class SSTableManager {

    private int fileIndex = 0;
    @Value("${node.id}")
    private String nodeId;

    private File getNodeDir() {

        String basePath = System.getProperty("user.dir");
        File dir = new File(basePath + "/data/" + nodeId);
        dir.mkdirs();
        return dir;
    }

    public void loadExistingFiles() {

        File[] files = getNodeDir().listFiles();

        if (files == null) return;

        int max = -1;

        for (File file : files) {

            String name = file.getName();

            if (name.startsWith("sstable_") && name.endsWith(".txt")) {

                try {
                    String num = name
                            .replace("sstable_", "")
                            .replace(".txt", "");

                    int index = Integer.parseInt(num);

                    max = Math.max(max, index);

                } catch (Exception ignored) {}
            }
        }

        fileIndex = max + 1;

        System.out.println("Detected existing SSTables. Next fileIndex = " + fileIndex);
    }

    public void flush(TreeMap<String, String> memTable) {

        try {
            File file = new File(getNodeDir(), "sstable_" + fileIndex++ + ".txt");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Map.Entry<String, String> entry : memTable.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }

            writer.close();
            System.out.println(nodeId +
                    " flushed data into " + file.getName());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String read(String key) {

        for (int i = fileIndex - 1; i >= 0; i--) {

            File file = new File(
                    getNodeDir(),
                    "sstable_" + i + ".txt"
            );

            if (!file.exists()) continue;

            try (BufferedReader reader =
                         new BufferedReader(new FileReader(file))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    String[] parts = line.split(":", 2);

                    if (parts[0].equals(key)) {
                        return parts[1];
                    }
                }

            } catch (Exception ignored) {}
        }

        return null;
    }

    public void compact() {

        System.out.println("Compaction started...");
        Map<String, String> merged = new HashMap<>();

        // 1. Read all SSTables (old → new)
        for (int i = 0; i < fileIndex; i++) {

            File file = new File(getNodeDir(), "sstable_" + i + ".txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    String key = parts[0];
                    String value = parts[1];

                    // overwrite → latest wins
                    merged.put(key, value);
                }

            } catch (Exception ignored) {}
        }

        // 2. Remove tombstones
        merged.entrySet().removeIf(e -> e.getValue().equals("__DELETED__"));

        // new compacted file
        File newFile = new File(getNodeDir(), "sstable_compacted.txt"
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {

            for (Map.Entry<String, String> entry : merged.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 4. Delete old SSTables
        for (int i = 0; i < fileIndex; i++) {
            new File(getNodeDir(), "sstable_" + i + ".txt").delete();
        }

        // rename compacted -> sstable_0
        newFile.renameTo(new File(getNodeDir(), "sstable_0.txt"));

        fileIndex = 1;
        System.out.println(nodeId + " compaction completed");
    }

    public int getFileCount() {
            File[] files = getNodeDir().listFiles(
                    (dir,name) -> name.startsWith("sstable_")
                            && name.endsWith(".txt")
            );

            return files == null ? 0 : files.length;
        }
}