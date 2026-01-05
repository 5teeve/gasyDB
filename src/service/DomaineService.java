package service;

import model.Domaine;
import utils.DataPersistence;
import java.util.HashMap;
import java.util.Map;

public class DomaineService {
    private Map<String, Domaine> domaines;
    private DataPersistence persistence;
    private String currentDatabase;
    
    public DomaineService(DataPersistence persistence) {
        this.domaines = new HashMap<>();
        this.persistence = persistence;
        this.currentDatabase = "default";
    }
    
    public void createDomaine(String name, Domaine domaine) throws Exception {
        if (domaines.containsKey(name)) {
            throw new IllegalArgumentException("Le domaine '" + name + "' existe déjà");
        }
        
        domaines.put(name, domaine);
        
        // Persister le domaine
        String databasePath = "data" + java.io.File.separator + currentDatabase;
        persistence.saveDomaine(name, domaine, databasePath);
    }
    
    public Domaine getDomaine(String name) {
        return domaines.get(name);
    }
    
    public boolean domaineExists(String name) {
        return domaines.containsKey(name);
    }
    
    public Map<String, Domaine> getAllDomaines() {
        return new HashMap<>(domaines);
    }
    
    public void removeDomaine(String name) throws Exception {
        if (!domaines.containsKey(name)) {
            throw new IllegalArgumentException("Le domaine '" + name + "' n'existe pas");
        }
        
        domaines.remove(name);
        
        // Supprimer le fichier de persistance
        String databasePath = "data" + java.io.File.separator + currentDatabase;
        persistence.deleteDomaine(name, databasePath);
    }
    
    public void setCurrentDatabase(String databaseName) {
        this.currentDatabase = databaseName;
        loadDomaines();
    }
    
    public void loadDomaines() {
        domaines.clear();
        String databasePath = "data" + java.io.File.separator + currentDatabase;
        Map<String, Domaine> loadedDomaines = persistence.loadAllDomaines(databasePath);
        domaines.putAll(loadedDomaines);
    }
    
    public String getCurrentDatabase() {
        return currentDatabase;
    }
}
