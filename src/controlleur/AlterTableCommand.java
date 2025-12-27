package controlleur;

import model.Attribut;

public class AlterTableCommand {
    public static final int TYPE_ADD_COLUMN = 1;
    public static final int TYPE_DROP_COLUMN = 2;
    
    public int type;
    public String table;
    public String columnName;
    public Attribut attribut; // Pour ADD COLUMN
    
    public AlterTableCommand(int type, String table, String columnName) {
        this.type = type;
        this.table = table;
        this.columnName = columnName;
    }
    
    public AlterTableCommand(int type, String table, String columnName, Attribut attribut) {
        this.type = type;
        this.table = table;
        this.columnName = columnName;
        this.attribut = attribut;
    }
}
