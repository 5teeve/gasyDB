package controlleur;

public class UpdateCommand {
    public String table;
    public String[] colonnes;
    public String[] valeurs;
    public String condition;
    
    public UpdateCommand(String table, String[] colonnes, String[] valeurs, String condition) {
        this.table = table;
        this.colonnes = colonnes;
        this.valeurs = valeurs;
        this.condition = condition;
    }
}
