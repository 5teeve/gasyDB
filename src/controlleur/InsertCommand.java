package controlleur;

public class InsertCommand {
    public String table;
    public String[] colonnes;
    public String[] valeurs;
    
    public InsertCommand(String table, String[] colonnes, String[] valeurs) {
        this.table = table;
        this.colonnes = colonnes;
        this.valeurs = valeurs;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Insertion dans '").append(table).append("'");
        sb.append(" (").append(String.join(", ", colonnes)).append(")");
        sb.append(" VALEURS: (").append(String.join(", ", valeurs)).append(")");
        return sb.toString();
    }
}
