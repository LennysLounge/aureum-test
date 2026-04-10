package io.github.lennyslounge.aureum;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Config {

    private final Properties properties = new Properties();
    private final Properties defaultProperties = new Properties();
    private final File configFile;

    public Config() {
        File f = new File("aureum.properties");
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                properties.load(fis);
                // read the file again but with comments removed to
                // find any properties that were commented out.
                String fileWithoutComments = Files.readAllLines(f.toPath()).stream()
                        .map(line -> line.startsWith("#")
                                ? line.substring(1)
                                : line
                        ).collect(Collectors.joining("\n"));
                defaultProperties.load(new StringReader(fileWithoutComments));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            configFile = f;
        } else {
            configFile = null;
        }
    }

    public String getString(String key, String defaultValue, String... description) {
        return get(key, defaultValue, s -> s, s -> s, description);
    }

    public int getInteger(String key, int defaultValue, String description) {
        return get(key, defaultValue, String::valueOf, Integer::parseInt, description);
    }

    public boolean getBoolean(String key, boolean defaultValue, String description) {
        return get(key, defaultValue, String::valueOf, Boolean::parseBoolean, description);
    }

    public <T> T get(String key,
                     T defaultValue,
                     Function<T, String> serializer,
                     Function<String, T> deserializer,
                     String... description) {
        if (properties.containsKey(key)) {
            return deserializer.apply(properties.getProperty(key));
        }
        if (!defaultProperties.containsKey(key)) {
            saveDefault(key, serializer.apply(defaultValue), description);
        }
        return defaultValue;
    }

    private void saveDefault(String key, Object defaultValue, String... description) {
        String defaultValueAsString = String.valueOf(defaultValue);
        properties.put(key, defaultValueAsString);

        if (configFile != null) {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(configFile, true)))) {
                if (configFile.length() == 0) {
                    writer.println("# ==============================================================================");
                    writer.println("# AUREUM CONFIGURATION");
                    writer.println("# ==============================================================================");
                    writer.println("# This file allows you to override Aureum's configuration locally.");
                    writer.println("#");
                    writer.println("# Settings here override the defaults in the code. Since these are usually");
                    writer.println("# specific to your machine (like which diff tool you prefer), it makes sense");
                    writer.println("# to keep this file out of your repository by adding it to .gitignore.");
                    writer.println("#");
                    writer.println("# HOW THIS FILE WORKS:");
                    writer.println("# 1. Discovering options:   New configuration options are automatically added to");
                    writer.println("#                           this file as commented-out defaults when you run tests.");
                    writer.println("# 2. To override a setting: Uncomment the line (remove the '#') and change the value.");
                    writer.println("# 3. To use the default:    Leave the line commented out or delete it.");
                    writer.println("#");
                    writer.println("# If you make a mistake, simply delete the line or clear the entire file, and");
                    writer.println("# Aureum will regenerate the defaults on the next run.");
                    writer.println("# ==============================================================================");
                }

                writer.println("");
                for (String descriptionLine : description) {
                    writer.println("# " + descriptionLine);
                }

                writer.print("#" + key + " = ");
                String[] valueParts = defaultValueAsString.split("\\r?\\n|\\r");
                if (valueParts.length == 1) {
                    writer.println(defaultValueAsString);
                } else {
                    writer.println("\\");
                    for (int i = 0; i < valueParts.length; i++) {
                        writer.print("#  " + valueParts[i]);
                        if (i != valueParts.length - 1) {
                            writer.print("\\");
                        }
                        writer.println("");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
