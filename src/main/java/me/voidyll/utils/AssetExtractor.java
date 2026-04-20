package me.voidyll.utils;

import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

/**
 * Extracts the Server/ asset pack from inside the plugin JAR to the plugin's
 * data directory so Hytale's asset loader can read them as loose files.
 * This runs once on startup, making the plugin a single-JAR deployment.
 */
public class AssetExtractor {

    private static final String ASSET_PREFIX = "Server/";
    private static final String VERSION_FILE = ".hytide_assets_version";

    private final Path jarPath;
    private final Path dataDirectory;
    private final HytaleLogger logger;
    private final String pluginVersion;

    public AssetExtractor(Path jarPath, Path dataDirectory, HytaleLogger logger, String pluginVersion) {
        this.jarPath = jarPath;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.pluginVersion = pluginVersion;
    }

    /**
     * Extracts assets if needed (version mismatch or missing).
     * Returns true if extraction occurred, false if skipped.
     */
    public boolean extractIfNeeded() {
        Path versionFile = dataDirectory.resolve(VERSION_FILE);
        Path serverDir = dataDirectory.resolve("Server");

        // Check if extraction is needed
        if (Files.exists(versionFile) && Files.exists(serverDir)) {
            try {
                String installedVersion = Files.readString(versionFile).trim();
                if (pluginVersion.equals(installedVersion)) {
                    logger.at(Level.INFO).log("[HyTide] Assets already up to date (v%s), skipping extraction.", pluginVersion);
                    return false;
                }
                logger.at(Level.INFO).log("[HyTide] Asset version mismatch (installed: %s, plugin: %s), re-extracting...", installedVersion, pluginVersion);
            } catch (IOException e) {
                logger.at(Level.INFO).log("[HyTide] Could not read version file, re-extracting assets...");
            }
        } else {
            logger.at(Level.INFO).log("[HyTide] Assets not found, extracting for first time...");
        }

        return extractAssets(versionFile, serverDir);
    }

    private boolean extractAssets(Path versionFile, Path serverDir) {
        try {
            // Clean existing Server directory for a fresh extract
            if (Files.exists(serverDir)) {
                deleteDirectory(serverDir);
            }

            // Open the JAR as a filesystem and copy Server/ contents
            URI jarUri = URI.create("jar:" + jarPath.toUri());
            try (FileSystem jarFs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                Path jarServerRoot = jarFs.getPath(ASSET_PREFIX);

                if (!Files.exists(jarServerRoot)) {
                    logger.at(Level.WARNING).log("[HyTide] No Server/ directory found inside JAR, skipping asset extraction.");
                    return false;
                }

                int[] count = {0};
                Files.walkFileTree(jarServerRoot, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetDir = serverDir.resolve(jarServerRoot.relativize(dir).toString());
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = serverDir.resolve(jarServerRoot.relativize(file).toString());
                        try (InputStream in = Files.newInputStream(file);
                             OutputStream out = Files.newOutputStream(targetFile,
                                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            in.transferTo(out);
                        }
                        count[0]++;
                        return FileVisitResult.CONTINUE;
                    }
                });

                // Write version marker
                Files.writeString(versionFile, pluginVersion,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                logger.at(Level.INFO).log("[HyTide] Extracted %d asset files (v%s).", count[0], pluginVersion);
                return true;
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("[HyTide] Failed to extract assets: %s", e.getMessage());
            return false;
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
