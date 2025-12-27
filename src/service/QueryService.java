package service;

import controlleur.AlterTableCommand;
import controlleur.CreateCommand;
import controlleur.DeleteCommand;
import controlleur.InsertCommand;
import controlleur.QueryCommand;
import controlleur.ShowCommand;
import controlleur.UpdateCommand;
import java.util.Map;
import model.Attribut;
import model.Relation;
import utils.DataPersistence;

public class QueryService {
    private TableService tableService;
    private DatabaseService databaseService;
    private DataPersistence persistence;
    
    public QueryService(TableService tableService, DatabaseService databaseService, DataPersistence persistence) {
        this.tableService = tableService;
        this.databaseService = databaseService;
        this.persistence = persistence;
    }
    
    public Object executeSelect(QueryCommand cmd) throws Exception {
        Relation r = tableService.getTable(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        Relation resultat = r;
        
        if (cmd.condition != null && !cmd.condition.isEmpty()) {
            resultat = resultat.selection(cmd.condition);
        }
        
        if (cmd.colonnes != null && cmd.colonnes.length > 0 && !cmd.colonnes[0].equals("*")) {
            resultat = resultat.projection(cmd.colonnes);
        }
        
        return resultat;
    }
    
    public void executeInsert(InsertCommand cmd) throws Exception {
        Relation r = tableService.getTable(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        String[] colonnesAUtiliser = cmd.colonnes;
        if (colonnesAUtiliser == null) {
            Attribut[] attributs = r.getAttributs();
            colonnesAUtiliser = new String[attributs.length];
            for (int i = 0; i < attributs.length; i++) {
                colonnesAUtiliser[i] = attributs[i].getNom();
            }
        }
        
        if (colonnesAUtiliser.length != cmd.valeurs.length) {
            throw new IllegalArgumentException(
                "Nombre de colonnes (" + colonnesAUtiliser.length + ") différent du nombre de valeurs (" + cmd.valeurs.length + ")"
            );
        }
        
        Object[] tuple = convertValuesToObjects(cmd.valeurs, colonnesAUtiliser, r);
        r.ajouterTuple(tuple);
        
        // Persister l'insertion
        persistence.saveInsert(cmd.table, colonnesAUtiliser, cmd.valeurs, getCurrentDatabasePath());
    }
    
    private String getCurrentDatabasePath() {
        return "data" + java.io.File.separator + databaseService.getCurrentDatabase();
    }
    
    public void executeCreate(CreateCommand cmd) throws Exception {
        if (cmd.type == CreateCommand.TYPE_DATABASE) {
            databaseService.createDatabase(cmd.name);
        } else if (cmd.type == CreateCommand.TYPE_TABLE) {
            if (cmd.attributs != null && cmd.attributs.length > 0) {
                Relation relation = new Relation(cmd.name, cmd.attributs);
                tableService.addTable(cmd.name, relation);
            }
        }
    }
    
    public void executeDelete(DeleteCommand cmd) throws Exception {
        if (cmd.isDatabase) {
            databaseService.deleteDatabase(cmd.target);
        } else {
            if (!tableService.tableExists(cmd.target)) {
                throw new IllegalArgumentException("Table '" + cmd.target + "' introuvable");
            }
            tableService.removeTable(cmd.target);
        }
    }
    
    public void executeUse(String dbName) throws Exception {
        databaseService.useDatabase(dbName);
        // Recharger les tables depuis la nouvelle base de données
        tableService.loadAllTables();
    }
    
    public Object executeShow(ShowCommand cmd) throws Exception {
        if ("TABLE".equals(cmd.type)) {
            if (cmd.showAll) {
                return tableService.getAllTables();
            } else {
                Relation r = tableService.getTable(cmd.target);
                if (r == null) {
                    throw new IllegalArgumentException("Table '" + cmd.target + "' introuvable");
                }
                return r;
            }
        } else if ("DATABASE".equals(cmd.type)) {
            if (cmd.showAll) {
                return databaseService.getAllDatabases();
            } else {
                boolean exists = databaseService.databaseExists(cmd.target);
                if (!exists) {
                    throw new IllegalArgumentException("Base de données '" + cmd.target + "' introuvable");
                }
                return "Base de données '" + cmd.target + "' existe";
            }
        } else {
            throw new IllegalArgumentException("Type de commande SHOW non supporté: " + cmd.type);
        }
    }
    
    public void executeUpdate(UpdateCommand cmd) throws Exception {
        Relation r = tableService.getTable(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        // Vérifier que les colonnes existent
        for (String colonne : cmd.colonnes) {
            if (!r.hasAttribute(colonne)) {
                throw new IllegalArgumentException("Colonne '" + colonne + "' introuvable dans la table '" + cmd.table + "'");
            }
        }
        
        // Convertir les valeurs
        Object[] valeurs = convertValuesToObjects(cmd.valeurs, cmd.colonnes, r);
        
        // Appliquer les mises à jour
        if (cmd.condition != null && !cmd.condition.isEmpty()) {
            // Avec condition: mise à jour sélective
            r.updateTuples(cmd.colonnes, valeurs, cmd.condition);
        } else {
            // Sans condition: mise à jour de tous les tuples
            r.updateAllTuples(cmd.colonnes, valeurs);
        }
        
        // Persister les modifications
        saveRelationToFile(cmd.table, r);
    }
    
    public void executeAlterTable(AlterTableCommand cmd) throws Exception {
        Relation r = tableService.getTable(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        if (cmd.type == AlterTableCommand.TYPE_ADD_COLUMN) {
            // Vérifier que la colonne n'existe pas déjà
            if (r.hasAttribute(cmd.columnName)) {
                throw new IllegalArgumentException("Colonne '" + cmd.columnName + "' existe déjà dans la table '" + cmd.table + "'");
            }
            
            // Ajouter la colonne
            r.addAttribute(cmd.attribut);
            
            // Persister les modifications
            saveRelationToFile(cmd.table, r);
        } else if (cmd.type == AlterTableCommand.TYPE_DROP_COLUMN) {
            // Vérifier que la colonne existe
            if (!r.hasAttribute(cmd.columnName)) {
                throw new IllegalArgumentException("Colonne '" + cmd.columnName + "' introuvable dans la table '" + cmd.table + "'");
            }
            
            // Supprimer la colonne
            r.removeAttribute(cmd.columnName);
            
            // Persister les modifications
            saveRelationToFile(cmd.table, r);
        } else {
            throw new IllegalArgumentException("Type d'opération ALTER TABLE non supporté: " + cmd.type);
        }
    }
    
    private void saveRelationToFile(String tableName, Relation relation) throws Exception {
        String databasePath = getCurrentDatabasePath();
        
        // Sauvegarder le schéma
        persistence.createTableSchemaWithDomains(tableName, relation.getAttributs(), databasePath);
        
        // Sauvegarder les données (écraser le fichier existant)
        String dataFile = databasePath + java.io.File.separator + tableName + ".txt";
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get(dataFile), 
                new byte[0], // Vider le fichier d'abord
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.CREATE
            );
            
            // Écrire tous les tuples
            for (int i = 0; i < relation.getNbTuples(); i++) {
                Object[] tuple = relation.getTuples()[i];
                StringBuilder line = new StringBuilder();
                
                for (int j = 0; j < tuple.length; j++) {
                    if (tuple[j] != null) {
                        line.append(tuple[j].toString());
                    }
                    if (j < tuple.length - 1) {
                        line.append("|");
                    }
                }
                
                java.nio.file.Files.write(
                    java.nio.file.Paths.get(dataFile),
                    (line.toString() + "\n").getBytes(),
                    java.nio.file.StandardOpenOption.APPEND
                );
            }
            
            System.out.println("[QueryService] Relation " + tableName + " sauvegardée avec " + relation.getNbTuples() + " tuples");
        } catch (Exception e) {
            throw new Exception("Erreur sauvegarde relation " + tableName + ": " + e.getMessage());
        }
    }
    
    private Object[] convertValuesToObjects(String[] valeurs, String[] colonnes, Relation relation) {
        Object[] result = new Object[valeurs.length];
        Map<String, Attribut> attrMap = new java.util.HashMap<>();
        
        for (Attribut attr : relation.getAttributs()) {
            attrMap.put(attr.getNom(), attr);
        }
        
        for (int i = 0; i < valeurs.length; i++) {
            String val = valeurs[i];
            String colonneNom = colonnes[i];
            Attribut attribut = attrMap.get(colonneNom);
            
            Class<?> expectedType = (attribut != null) ? attribut.getDomaine().getType() : String.class;
            
            result[i] = convertValue(val, expectedType);
        }
        return result;
    }
    
    private Object convertValue(String val, Class<?> expectedType) {
        if (val == null || val.isEmpty()) {
            return null;
        }
        
        try {
            if (expectedType == Boolean.class) {
                return Boolean.parseBoolean(val);
            } else if (expectedType == Integer.class) {
                return Integer.parseInt(val);
            } else if (expectedType == Double.class) {
                return Double.parseDouble(val);
            } else {
                return val;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Cannot convert '" + val + "' to " + expectedType.getSimpleName()
            );
        }
    }
}
