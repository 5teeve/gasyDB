package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class DatabaseService {
    private String currentDatabase;
    private Map<String, String> databasePaths;
    
    public DatabaseService() {
        this.currentDatabase = "default";
        this.databasePaths = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        initializeDefaultDatabase();
    }
    
    private void initializeDefaultDatabase() {
        String defaultPath = "data";
        databasePaths.put("default", defaultPath);
        ensureDatabaseExists(defaultPath);
    }
    
    public void createDatabase(String dbName) throws IOException {
        String dbPath = "data" + File.separator + dbName;
        Path path = Paths.get(dbPath);
        
        if (Files.exists(path)) {
            throw new IOException("La base de données '" + dbName + "' existe déjà");
        }
        
        Files.createDirectories(path);
        
        String metaFile = dbPath + File.separator + "database.meta";
        Files.write(Paths.get(metaFile), ("DATABASE=" + dbName + "\n").getBytes());
        
        databasePaths.put(dbName, dbPath);
        System.out.println("[DatabaseService] Base de données créée: " + dbName);
    }
    
    public void useDatabase(String dbName) throws IOException {
        String dbPath = "data" + File.separator + dbName;
        Path path = Paths.get(dbPath);
        
        if (!Files.exists(path)) {
            throw new IOException("Base de données '" + dbName + "' introuvable");
        }
        
        this.currentDatabase = dbName;
        System.out.println("[DatabaseService] Base de données active: " + dbName);
    }
    
    public void deleteDatabase(String dbName) throws IOException {
        String dbPath = "data" + File.separator + dbName;
        Path path = Paths.get(dbPath);
        
        if (!Files.exists(path)) {
            throw new IOException("Base de données '" + dbName + "' introuvable");
        }
        
        deleteDirectory(path);
        databasePaths.remove(dbName);
        
        // Si c'était la base active, revenir à default
        if (currentDatabase.equals(dbName)) {
            this.currentDatabase = "default";
        }
        
        System.out.println("[DatabaseService] Base de données supprimée: " + dbName);
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.delete(path);
    }
    
    private void ensureDatabaseExists(String dbPath) {
        Path path = Paths.get(dbPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("[DatabaseService] Répertoire créé: " + dbPath);
            } catch (IOException e) {
                System.err.println("[DatabaseService] Erreur création répertoire: " + e.getMessage());
            }
        }
    }
    
    public String getCurrentDatabase() {
        return currentDatabase;
    }
    
    public List<String> getAllDatabases() {
        List<String> databases = new ArrayList<>();
        try {
            Path dataPath = Paths.get("data");
            if (!Files.exists(dataPath)) {
                return databases;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataPath)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        Path metaFile = path.resolve("database.meta");
                        if (Files.exists(metaFile)) {
                            databases.add(path.getFileName().toString());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture répertoire data/: " + e.getMessage());
        }
        return databases;
    }
    
    public boolean databaseExists(String dbName) {
        Path dbPath = Paths.get("data", dbName);
        Path metaFile = dbPath.resolve("database.meta");
        return Files.exists(dbPath) && Files.exists(metaFile);
    }
    
    public String getDatabasePath(String dbName) {
        return databasePaths.get(dbName);
    }
}
