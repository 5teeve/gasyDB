package service;

import java.util.Map;
import java.util.TreeMap;
import model.Relation;
import utils.DataPersistence;

public class TableService {
    private Map<String, Relation> relations;
    private DataPersistence persistence;
    private String currentDatabase;
    
    public TableService(DataPersistence persistence) {
        this.relations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.persistence = persistence;
        this.currentDatabase = "data"; 
    }
    
    public void setCurrentDatabase(String databaseName) {
        this.currentDatabase = "data" + java.io.File.separator + databaseName;
    }
    
    public void addTable(String name, Relation relation) {
        relations.put(name, relation);
        persistence.createTableSchemaWithDomains(name, relation.getAttributs(), currentDatabase);
        persistence.createTable(name, currentDatabase);
    }
    
    public void removeTable(String tableName) {
        relations.remove(tableName);
        persistence.deleteTable(tableName, currentDatabase);
    }
    
    public Relation getTable(String tableName) {
        return relations.get(tableName);
    }
    
    public boolean tableExists(String tableName) {
        return relations.containsKey(tableName);
    }
    
    public Map<String, Relation> getAllTables() {
        return new TreeMap<>(relations);
    }
    
    public void loadAllTables() {
        Map<String, Relation> loadedTables = persistence.loadAllTablesFromDatabase(currentDatabase);
        relations.clear();
        relations.putAll(loadedTables);
    }
    
    public int getTableCount() {
        return relations.size();
    }
}
