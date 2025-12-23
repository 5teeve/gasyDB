package controlleur;

import model.Attribut;

public class CreateCommand {
    public static final int TYPE_DATABASE = 1;
    public static final int TYPE_TABLE = 2;
    
    public int type;
    public String name;
    public Attribut[] attributs; 
    
    public CreateCommand(int type, String name) {
        this.type = type;
        this.name = name;
        this.attributs = null;
    }
    
     public CreateCommand(int type, String name, Attribut[] attributs) {
        this.type = type;
        this.name = name;
        this.attributs = attributs;
    }
    
    @Override
    public String toString() {
        String typeStr = (type == TYPE_DATABASE) ? "DATABASE" : "TABLE";
        return "Création de " + typeStr + ": " + name;
    }
}
