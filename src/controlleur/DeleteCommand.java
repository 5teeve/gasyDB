package controlleur;

public class DeleteCommand {
    public String target;
    public boolean isDatabase;
    
    public DeleteCommand(String target) {
        this.target = target;
        this.isDatabase = target.toUpperCase().startsWith("DATABASE ");
        if (isDatabase) {
            this.target = target.substring(9).trim();
        }
    }
    
    @Override
    public String toString() {
        if (isDatabase) {
            return "Suppression de la base de données '" + target + "'";
        } else {
            return "Suppression de la table '" + target + "'";
        }
    }
}
