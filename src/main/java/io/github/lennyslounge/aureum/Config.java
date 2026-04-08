package io.github.lennyslounge.aureum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Config {

    private final Properties properties = new Properties();
    private final File configFile;

    public Config() {
        File f = new File("aureum.properties");
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            configFile = f;
        } else {
            configFile = null;
        }
    }

    public String getString(String key, String defaultValue, String description) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        saveDefault(key, defaultValue, description);
        return defaultValue;
    }

    public int getInteger(String key, int defaultValue, String description) {
        if (properties.containsKey(key)) {
            return Integer.parseInt(properties.getProperty(key));
        }
        saveDefault(key, defaultValue, description);
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue, String description) {
        if (properties.containsKey(key)) {
            return Boolean.parseBoolean(properties.getProperty(key));
        }
        saveDefault(key, defaultValue, description);
        return defaultValue;
    }

    private void saveDefault(String key, Object defaultValue, String description) {
        String defaultValueAsString = String.valueOf(defaultValue);
        properties.put(key, defaultValueAsString);

        if (configFile != null) {
            try {
                Files.write(
                        Paths.get(configFile.getAbsolutePath()),
                        String.format("# %s (default = %s)%n%s = %s%n%n",
                                        description,
                                        defaultValueAsString,
                                        key,
                                        defaultValue)
                                .getBytes(),
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
