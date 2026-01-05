package controlleur;

import model.Domaine;

public class CreateDomaineCommand {
    public static final int TYPE_DOMAINE = 3;
    
    public int type;
    public String name;
    public Domaine domaine;
    
    public CreateDomaineCommand(String name, Domaine domaine) {
        this.type = TYPE_DOMAINE;
        this.name = name;
        this.domaine = domaine;
    }
    
    @Override
    public String toString() {
        return "Création de DOMAINE: " + name + " " + domaine.toString();
    }
}
