package controlleur;

public class ShowCommand {
    public String type; // TABLE ou DATABASE
    public String target; 
    public boolean showAll;
    
    public ShowCommand(String type, String target) {
        this.type = type.toUpperCase();
        this.target = target;
        this.showAll = target.equals("*");
    }
    
    @Override
    public String toString() {
        if (showAll) {
            if ("TABLE".equals(type)) {
                return "Afficher toutes les tables";
            } else if ("DATABASE".equals(type)) {
                return "Afficher toutes les bases de données";
            }
        } else {
            if ("TABLE".equals(type)) {
                return "Afficher la table '" + target + "'";
            } else if ("DATABASE".equals(type)) {
                return "Afficher la base de données '" + target + "'";
            }
        }
        return "Afficher " + type + " '" + target + "'";
    }
}
