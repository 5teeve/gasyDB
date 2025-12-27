package utils;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attribut;
import model.Domaine;
import model.Relation;

public class DataPersistence {
    private static final String DATA_DIR = "data";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public DataPersistence() {
        createDataDirectory();
    }
    
    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Erreur création répertoire data: " + e.getMessage());
        }
    }
    
    public void saveInsert(String table, String[] colonnes, String[] valeurs) {
        saveInsert(table, colonnes, valeurs, "data");
    }
    
    public void saveInsert(String table, String[] colonnes, String[] valeurs, String databasePath) {
        String filename = databasePath + java.io.File.separator + table + ".txt";
        try {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < valeurs.length; i++) {
                line.append(valeurs[i]);
                if (i < valeurs.length - 1) line.append("|");
            }
            
            Files.write(
                Paths.get(filename),
                (line.toString() + "\n").getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            
            System.out.println("[Persistence] Données sauvegardées dans " + filename);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }
    
    public void createTableSchema(String table, String[] colonnes) {
        createTableSchema(table, colonnes, "data");
    }
    
    public void createTableSchema(String table, String[] colonnes, String databasePath) {
        String filename = databasePath + java.io.File.separator + table + "_schema.txt";
        try {
            String header = String.join("|", colonnes);
            Files.write(Paths.get(filename), (header + "\n").getBytes());
            System.out.println("[Persistence] Schéma créé pour " + table + " dans " + databasePath);
        } catch (IOException e) {
            System.err.println("Erreur création schéma: " + e.getMessage());
        }
    }
    
    public void createTableSchema(String table, String[] colonnes, String[] types) {
        createTableSchema(table, colonnes, types, "data");
    }
    
    public void createTableSchema(String table, String[] colonnes, String[] types, String databasePath) {
        String filename = databasePath + java.io.File.separator + table + "_schema.txt";
        try {
            String header = String.join("|", colonnes);
            String typeRow = String.join("|", types);
            Files.write(Paths.get(filename), (header + "\n" + typeRow + "\n").getBytes());
            System.out.println("[Persistence] Schéma créé pour " + table + " avec types dans " + databasePath);
        } catch (IOException e) {
            System.err.println("Erreur création schéma: " + e.getMessage());
        }
    }
    
    public void createTableSchemaWithDomains(String table, model.Attribut[] attributs) {
        createTableSchemaWithDomains(table, attributs, "data");
    }
    
    public void createTableSchemaWithDomains(String table, model.Attribut[] attributs, String databasePath) {
        String filename = databasePath + java.io.File.separator + table + "_schema.txt";
        try {
            StringBuilder header = new StringBuilder();
            StringBuilder domains = new StringBuilder();
            
            for (int i = 0; i < attributs.length; i++) {
                if (i > 0) {
                    header.append("|");
                    domains.append("|");
                }
                header.append(attributs[i].getNom());
                domains.append(attributs[i].getDomaine().serialize());
            }
            
            Files.write(Paths.get(filename), (header.toString() + "\n" + domains.toString() + "\n").getBytes());
            System.out.println("[Persistence] Schéma créé pour " + table + " avec domaines complets dans " + databasePath);
        } catch (IOException e) {
            System.err.println("Erreur création schéma: " + e.getMessage());
        }
    }
    
    public void createDatabase(String dbName) {
        try {
            String dbPath = DATA_DIR + File.separator + dbName;
            Files.createDirectories(Paths.get(dbPath));
            
            String metaFile = dbPath + File.separator + "database.meta";
            Files.write(Paths.get(metaFile), ("DATABASE=" + dbName + "\n").getBytes());
            
            System.out.println("[Persistence] Base de données créée: " + dbName);
        } catch (IOException e) {
            System.err.println("Erreur création BD: " + e.getMessage());
        }
    }
    
    public void createTable(String tableName) {
        createTable(tableName, "data");
    }
    
    public void createTable(String tableName, String databasePath) {
        try {
            String tableFile = databasePath + java.io.File.separator + tableName + ".txt";
            if (!Files.exists(Paths.get(tableFile))) {
                Files.createFile(Paths.get(tableFile));
                System.out.println("[Persistence] Table créée: " + tableName + " dans " + databasePath);
            } else {
                System.out.println("[Persistence] Table existante: " + tableName + " dans " + databasePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur création table: " + e.getMessage());
        }
    }
    
    public Path findFileInDatabaseIgnoreCase(String fileName, String databasePath) {
        try {
            Path dir = Paths.get(databasePath);
            if (!Files.exists(dir)) {
                return null;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    if (path.getFileName().toString().equalsIgnoreCase(fileName)) {
                        return path;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur recherche fichier " + fileName + ": " + e.getMessage());
        }
        return null;
    }
    
    public Path findFileIgnoreCase(String fileName) {
        return findFileInDatabaseIgnoreCase(fileName, "data");
    }
    
    public Relation loadRelationFromFile(String tableName) {
        return loadRelationFromFile(tableName, "data");
    }
    
    public Relation loadRelationFromFile(String tableName, String databasePath) {
        try {
            String schemaFileName = tableName + "_schema.txt";
            String dataFileName = tableName + ".txt";
            
            Path schemaPath = findFileInDatabaseIgnoreCase(schemaFileName, databasePath);
            Path dataPath = findFileInDatabaseIgnoreCase(dataFileName, databasePath);
            
            if (schemaPath == null) {
                System.err.println("[Persistence] Schéma non trouvé pour " + tableName);
                return null;
            }
            
            // Lire le schéma
            List<String> schemaLines = Files.readAllLines(schemaPath);
            if (schemaLines.size() < 1) {
                System.err.println("[Persistence] Schéma vide pour " + tableName);
                return null;
            }
            
            String[] columnNames = schemaLines.get(0).split("\\|");
            String[] columnDomains = schemaLines.size() > 1 ? schemaLines.get(1).split("\\|") : new String[columnNames.length];
            
            // Créer les attributs avec leurs domaines
            Attribut[] attributs = new Attribut[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                String columnName = columnNames[i].trim();
                String domainSpec = i < columnDomains.length && columnDomains[i] != null ? columnDomains[i].trim() : "String||";
                
                // Deserialiser le domaine
                Domaine domaine = Domaine.deserialize(columnName, domainSpec);
                attributs[i] = new Attribut(columnName, domaine);
            }
            
            // Créer la relation
            Relation relation = new Relation(tableName, attributs);
            
            // Charger les données si le fichier existe
            if (dataPath != null) {
                List<String> dataLines = Files.readAllLines(dataPath);
                for (String line : dataLines) {
                    if (line.trim().isEmpty()) continue;
                    
                    String[] values = line.split("\\|");
                    Object[] tuple = new Object[values.length];
                    
                    for (int i = 0; i < values.length && i < attributs.length; i++) {
                        tuple[i] = parseValue(values[i].trim(), attributs[i].getDomaine().getType());
                    }
                    
                    relation.ajouterTuple(tuple);
                }
                System.out.println("[Persistence] Relation " + tableName + " chargée avec " + relation.getNbTuples() + " tuples");
            }
            
            return relation;
        } catch (IOException e) {
            System.err.println("Erreur chargement relation: " + e.getMessage());
            return null;
        }
    }
    
    private Class<?> parseType(String type) {
        switch(type.toLowerCase()) {
            case "integer": return Integer.class;
            case "double": return Double.class;
            case "boolean": return Boolean.class;
            default: return String.class;
        }
    }
    
    private Object parseValue(String value, Class<?> type) {
        if (value == null || value.isEmpty()) return null;
        
        try {
            if (type == Integer.class) {
                return Integer.parseInt(value);
            } else if (type == Double.class) {
                return Double.parseDouble(value);
            } else if (type == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur parsing valeur: " + value + " pour type " + type.getSimpleName());
        }
        return value;
    }
    
    public List<String> getAllTableNames() {
        return getAllTableNamesFromDatabase("data");
    }
    
    public List<String> getAllTableNamesFromDatabase(String databasePath) {
        List<String> tableNames = new ArrayList<>();
        try {
            Path dataPath = Paths.get(databasePath);
            if (!Files.exists(dataPath)) {
                return tableNames;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataPath)) {
                for (Path path : stream) {
                    String fileName = path.getFileName().toString();
                    if (Files.isRegularFile(path) && fileName.endsWith(".txt") && !fileName.endsWith("_schema.txt")) {
                        String tableName = fileName.substring(0, fileName.length() - 4);
                        tableNames.add(tableName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture répertoire " + databasePath + ": " + e.getMessage());
        }
        return tableNames;
    }
    
    public String deleteTable(String tableName) {
        return deleteTable(tableName, "data");
    }
    
    public String deleteTable(String tableName, String databasePath) {
        try {
            boolean dataDeleted = false;
            boolean schemaDeleted = false;
            
            String dataFileName = tableName + ".txt";
            Path dataPath = findFileInDatabaseIgnoreCase(dataFileName, databasePath);
            if (dataPath != null) {
                Files.delete(dataPath);
                dataDeleted = true;
                System.out.println("[Persistence] Fichier de données supprimé: " + dataPath);
            }
            
            String schemaFileName = tableName + "_schema.txt";
            Path schemaPath = findFileInDatabaseIgnoreCase(schemaFileName, databasePath);
            if (schemaPath != null) {
                Files.delete(schemaPath);
                schemaDeleted = true;
                System.out.println("[Persistence] Fichier de schéma supprimé: " + schemaPath);
            }
            
            String message;
            if (dataDeleted && schemaDeleted) {
                message = "Table '" + tableName + "' supprimée avec succès (fichiers de données et schéma)";
            } else if (dataDeleted) {
                message = "Table '" + tableName + "' supprimée (fichier de données uniquement)";
            } else if (schemaDeleted) {
                message = "Table '" + tableName + "' supprimée (fichier de schéma uniquement)";
            } else {
                message = "Aucun fichier trouvé pour la table '" + tableName + "'";
            }
            
            System.out.println("[Persistence] " + message);
            return message;
        } catch (IOException e) {
            String errorMsg = "Erreur suppression table '" + tableName + "': " + e.getMessage();
            System.err.println(errorMsg);
            return errorMsg;
        }
    }
    
    public Map<String, Relation> loadAllTables() {
        return loadAllTablesFromDatabase("data");
    }
    
    public Map<String, Relation> loadAllTablesFromDatabase(String databasePath) {
        Map<String, Relation> tables = new HashMap<>();
        List<String> tableNames = getAllTableNamesFromDatabase(databasePath);
        
        for (String tableName : tableNames) {
            Relation relation = loadRelationFromFile(tableName, databasePath);
            if (relation != null) {
                tables.put(tableName, relation);
                System.out.println("[DataPersistence] Table chargée: " + tableName);
            }
        }
        
        return tables;
    }
}


