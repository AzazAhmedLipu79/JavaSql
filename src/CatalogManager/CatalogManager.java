package CatalogManager;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption; 

import Config.FileConstants;
import Util.*;


public class CatalogManager {
    // Singleton instance
    private static CatalogManager instance;

    // Private constructor prevents external instantiation
    private CatalogManager() {
        // Initialize any state, load catalogs from disk etc.
    }

    // Public method to provide access to the single instance
    public static synchronized CatalogManager getInstance() {
        if (instance == null) {
            instance = new CatalogManager();
        }
        return instance;
    }

    public boolean databaseExists(String dbName) {
    Logger.debug("Checking if database exists: " + dbName);

        // check if database folder exists
        Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
        if (!Files.exists(dbPath)) {
            Logger.debug("Database does not exist: " + dbName);
            return false;
        }

        // If we reach here, the database exists
        Logger.debug("Database exists: " + dbName);
        return true;
    }

    public boolean tableExists(String dbName, String tableName) {
    System.out.println("Checking if table exists: " + tableName + " in database: " + dbName);
    Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
    Path tableDir = dbPath.resolve(tableName);
    return Files.exists(tableDir);
    }

    public void createDatabase(String dbName) {
        if (dbName == null || dbName.isBlank()) {
            System.out.println("Database name cannot be empty");
            return;
        }

    Logger.info("Creating database: " + dbName);

        // Source is the blueprint "databases" folder containing all templates
        // Path source = Paths.get("../../" + FileConstants.db_blueprint_path, "databases");
        Path source = AccessDataFolder.getDataFilePath(FileConstants.db_blueprint_path).resolve("databases");

        // Destination is the new database folder named dbName
        Path destination = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);

        if (!Files.exists(source)) {

            Logger.error("Source blueprint folder does not exist: " + source);

            return;
        }

        try {
            Logger.debug("Creating database directory: " + destination);
            Files.createDirectories(destination);

            // Walk through source directory and copy each file/directory recursively
            Files.walk(source).forEach(path -> {
                try {
                    Path relativePath = source.relativize(path);
                    Path targetPath = destination.resolve(relativePath);

                    if (Files.isDirectory(path)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy: " + path, e);
                }
            });

            Logger.info("Database created successfully: " + dbName);

        } catch (IOException e) {
            Logger.error("Error creating database: " + e.getMessage());
        }
    }

    public void dropDatabase(String dbName) {
    Logger.info("Dropping database: " + dbName);
        if (dbName == null || dbName.isBlank()) {
            System.out.println("Database name cannot be empty");
            return;
        }
        Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
        if (!Files.exists(dbPath)) {
            System.out.println("Database does not exist: " + dbName);
            return;
        }
        try {
            try (java.util.stream.Stream<Path> stream = Files.walk(dbPath)) {
                stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                      .forEach(p -> {
                          try { Files.deleteIfExists(p); } catch (IOException ex) { throw new RuntimeException(ex); }
                      });
            }
            System.out.println("Database dropped successfully: " + dbName);
        } catch (IOException | RuntimeException e) {
            System.out.println("Error dropping database: " + e.getMessage());
        }
    }

    public void createTable(String dbName, String tableName) {
    Logger.info("Creating table: " + tableName + " in database: " + dbName);
        Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
        if (!Files.exists(dbPath)) {
            Logger.error("Database does not exist: " + dbName);
            return;
        }
    Path tableDir = dbPath.resolve(tableName);
    Path page0 = tableDir.resolve("page_0.csv");
        try {
            Files.createDirectories(tableDir.resolve("meta"));
            Files.createDirectories(tableDir);
            if (!Files.exists(page0)) {
                Files.write(page0, ("id\n").getBytes());
            }
            Logger.info("Table created successfully: " + tableName);
        } catch (IOException e) {
            Logger.error("Error creating table: " + e.getMessage());
        }
    }

    public void dropTable(String dbName, String tableName) {
    Logger.info("Dropping table: " + tableName + " from database: " + dbName);
        Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
        Path tableDir = dbPath.resolve(tableName);
        try {
            if (Files.exists(tableDir)) {
                try (java.util.stream.Stream<Path> stream = Files.walk(tableDir)) {
                    stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                          .forEach(p -> {
                              try { Files.deleteIfExists(p); } catch (IOException ex) { throw new RuntimeException(ex); }
                          });
                }
                Logger.info("Table dropped successfully: " + tableName);
            } else {
                Logger.warn("Table not found: " + tableName);
            }
        } catch (IOException | RuntimeException e) {
            Logger.error("Error dropping table: " + e.getMessage());
        }
    }

    // List available databases (directories under storage/databases)
    public java.util.List<String> listDatabases() {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            java.nio.file.Path base = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path);
            if (java.nio.file.Files.exists(base)) {
                try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(base)) {
                    stream.filter(java.nio.file.Files::isDirectory)
                          .forEach(p -> out.add(p.getFileName().toString()));
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing databases: " + e.getMessage());
        }
        java.util.Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    // List tables for a given database. Uses directory-per-table layout; skips meta and files.
    public java.util.List<String> listTables(String dbName) {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            java.nio.file.Path dbPath = AccessDataFolder.getDataFilePath(FileConstants.db_storage_path).resolve(dbName);
            if (!java.nio.file.Files.exists(dbPath)) {
                System.out.println("Database does not exist: " + dbName);
                return out;
            }
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(dbPath)) {
                stream.filter(java.nio.file.Files::isDirectory)
                      .map(p -> p.getFileName().toString())
                      .filter(name -> !name.equalsIgnoreCase("meta"))
                      .forEach(out::add);
            }
        } catch (Exception e) {
            System.out.println("Error listing tables: " + e.getMessage());
        }
        java.util.Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }
}
