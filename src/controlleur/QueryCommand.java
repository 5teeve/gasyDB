package controlleur;

public class QueryCommand {
    public String[] colonnes;
    public String table;
    public String condition;
    
    public QueryCommand(String[] colonnes, String table, String condition) {
        this.colonnes = colonnes;
        this.table = table;
        this.condition = condition;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Requête: projection sur [");
        for (int i = 0; i < colonnes.length; i++) {
            sb.append(colonnes[i]);
            if (i < colonnes.length - 1) sb.append(", ");
        }
        sb.append("] de la table '").append(table).append("'");
        if (condition != null && !condition.isEmpty()) {
            sb.append(" avec condition: ").append(condition);
        }
        return sb.toString();
    }
}
