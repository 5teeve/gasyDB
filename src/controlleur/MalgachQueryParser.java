package controlleur;

import model.Attribut;
import model.Domaine;

public class MalgachQueryParser {
    private static service.DomaineService domaineService;
    
    public static void setDomaineService(service.DomaineService service) {
        domaineService = service;
    }
    
    public static QueryCommand parseSelect(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("ALAIVO")) {
            throw new IllegalArgumentException(
                "Syntaxe SELECT invalide. Format:\n" +
                "ALAIVO colonnes @ table\n" +
                "ALAIVO colonnes @ table RAHA condition"
            );
        }
        
                int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête SELECT");
        }
        
        String colonnesStr = query.substring(6, atPos).trim();
        if (colonnesStr.isEmpty()) {
            throw new IllegalArgumentException("Paramètre 'colonnes' manquant");
        }
        
        String afterAt = query.substring(atPos + 1).trim();
        
        int rahaPos = afterAt.toUpperCase().indexOf("RAHA");
        String tableStr, condition = null;
        
        if (rahaPos != -1) {
            tableStr = afterAt.substring(0, rahaPos).trim();
            condition = afterAt.substring(rahaPos + 4).trim();
            if (condition.isEmpty()) {
                condition = null;
            }
        } else {
            tableStr = afterAt;
        }
        
        if (tableStr.isEmpty()) {
            throw new IllegalArgumentException("Nom de la table manquant");
        }
        
        String[] colonnes;
        if (colonnesStr.equals("*")) {
            colonnes = new String[]{"*"};
        } else {
            colonnes = parseList(colonnesStr);
        }
        
        return new QueryCommand(colonnes, tableStr, condition);
    }
    
    public static InsertCommand parseInsert(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("AMPIDIRO")) {
            throw new IllegalArgumentException(
                "Syntaxe INSERT invalide. Format:\n" +
                "AMPIDIRO @ table IRETO (valeurs)\n" +
                "AMPIDIRO @ table(colonnes) IRETO (valeurs)\n" +
                "Exemple: AMPIDIRO @ Employes IRETO ('Alice', 'Bob')\n" +
                "Ou: AMPIDIRO @ Employes(nom, prenom) IRETO ('Alice', 'Bob')"
            );
        }
        
                int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant");
        }
        
        String afterAt = query.substring(atPos + 1).trim();
        
        String afterAtUpper = afterAt.toUpperCase();
        int iretoPos = afterAtUpper.indexOf("IRETO");
        if (iretoPos == -1) {
            throw new IllegalArgumentException("Mot-clé 'IRETO' manquant");
        }
        
        String tableAndCols = afterAt.substring(0, iretoPos).trim();
        String afterIreto = afterAt.substring(iretoPos + 5).trim();
        
        String tableStr;
        String[] colonnes = null;
        
        int parenPos = tableAndCols.indexOf("(");
        
        if (parenPos == -1) {
            tableStr = tableAndCols;
        } else {
            int closeParenPos = tableAndCols.indexOf(")");
            if (closeParenPos == -1) {
                throw new IllegalArgumentException("Parenthèse fermante ')' manquante après colonnes");
            }
            
            tableStr = tableAndCols.substring(0, parenPos).trim();
            String colonnesStr = tableAndCols.substring(parenPos + 1, closeParenPos).trim();
            
            if (!colonnesStr.isEmpty()) {
                colonnes = parseList(colonnesStr);
            }
        }
        
        if (tableStr.isEmpty()) {
            throw new IllegalArgumentException("Nom de la table manquant");
        }
        
        // Parser les valeurs
        int valeursOpenPos = afterIreto.indexOf("(");
        int valeursClosePos = afterIreto.lastIndexOf(")");
        
        if (valeursOpenPos == -1 || valeursClosePos == -1) {
            throw new IllegalArgumentException("Parenthèses manquantes autour des valeurs");
        }
        
        // Extraire les valeurs
        String valeursStr = afterIreto.substring(valeursOpenPos + 1, valeursClosePos).trim();
        if (valeursStr.isEmpty()) {
            throw new IllegalArgumentException("Liste des valeurs vide");
        }
        
        // Parser les valeurs
        String[] valeurs = parseValues(valeursStr);
        
        // Vérifier le nombre de colonnes/valeurs
        if (colonnes != null && colonnes.length != valeurs.length) {
            throw new IllegalArgumentException(
                "Nombre de colonnes (" + colonnes.length + ") différent du nombre de valeurs (" + valeurs.length + ")"
            );
        }
        
        return new InsertCommand(tableStr, colonnes, valeurs);
    }
    
    public static boolean isSelectQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("ALAIVO");
    }
    
    public static boolean isInsertQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("AMPIDIRO");
    }
    
    public static boolean isCreateQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("MAMORONA");
    }
    
    public static boolean isDeleteQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("FAFAO");
    }
    
    public static boolean isShowQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("ASEHOY");
    }
    
    public static boolean isUseQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("AMPIASAO");
    }
    
    public static boolean isUpdateQuery(String query) {
        return query != null && query.trim().toUpperCase().startsWith("OVAY");
    }
    
    public static boolean isAlterTableQuery(String query) {
        if (query == null) return false;
        String upper = query.trim().toUpperCase();
        return upper.startsWith("AMPIO COLUMN") || upper.startsWith("FAFAO COLUMN");
    }
    
    public static boolean isCreateDomaineQuery(String query) {
        if (query == null) return false;
        String upper = query.trim().toUpperCase();
        return upper.startsWith("MAMORONA DOMAINE");
    }
    
    public static CreateCommand parseCreate(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        //MAMORONA
        if (!queryUpper.startsWith("MAMORONA")) {
            throw new IllegalArgumentException(
                "Syntaxe CREATE invalide. Format:\n" +
                "MAMORONA TABLE nom @ attribut1(domaine1) attribut2(domaine2) ...\n" +
                "MAMORONA DATABASE nom\n" +
                "Domaines: Type|Range|Enum\n" +
                "Exemples: Integer|0-120|  String||  Double|0-100000|  String||Manager,Developer"
            );
        }
        
        //apres MAMORONA
        String afterCmd = query.substring(8).trim();
        
        //premier espace pour séparer le type du nom
        int spacePos = afterCmd.indexOf(" ");
        if (spacePos == -1) {
            throw new IllegalArgumentException("Format invalide: type (TABLE/DATABASE) manquant");
        }
        
        String typeStr = afterCmd.substring(0, spacePos).trim().toUpperCase();
        String nameAndAttrs = afterCmd.substring(spacePos).trim();
        
        if (nameAndAttrs.isEmpty()) {
            throw new IllegalArgumentException("Nom manquant après le type");
        }
        
        if (!typeStr.equals("TABLE") && !typeStr.equals("DATABASE")) {
            throw new IllegalArgumentException("Type invalide: '" + typeStr + "' (doit être TABLE ou DATABASE)");
        }
        
        if (typeStr.equals("DATABASE")) {
            // Pour DATABASE, c'est simple: juste le nom
            return new CreateCommand(CreateCommand.TYPE_DATABASE, nameAndAttrs);
        } else {
            // Pour TABLE, on doit parser le nom et les attributs avec leurs domaines
            return parseCreateTable(nameAndAttrs);
        }
    }
    
    private static CreateCommand parseCreateTable(String tableSpec) throws IllegalArgumentException {
        // Format: nom @ attribut1(domaine1) attribut2(domaine2) ...
        // Exemple: Employes @ Age(Integer|0-120|) Name(String||) Salaire(Double|0-100000|)
        
        String tableSpecUpper = tableSpec.toUpperCase();
        int atPos = tableSpecUpper.indexOf("@");
        
        String tableName;
        String attributesSpec;
        
        if (atPos == -1) {
            // Si pas de @, on crée juste la table vide
            tableName = tableSpec.trim();
            return new CreateCommand(CreateCommand.TYPE_TABLE, tableName, new Attribut[0]);
        }
        
        tableName = tableSpec.substring(0, atPos).trim();
        attributesSpec = tableSpec.substring(atPos + 1).trim();
        
        if (tableName.isEmpty()) {
            throw new IllegalArgumentException("Nom de table manquant");
        }
        
        if (attributesSpec.isEmpty()) {
            throw new IllegalArgumentException("Attributs manquants après @");
        }
        
        java.util.List<Attribut> attributsList = parseAttributes(attributesSpec);
        Attribut[] attributs = attributsList.toArray(new Attribut[0]);
        
        return new CreateCommand(CreateCommand.TYPE_TABLE, tableName, attributs);
    }
    
    private static java.util.List<Attribut> parseAttributes(String attributesSpec) throws IllegalArgumentException {
        java.util.List<Attribut> attributs = new java.util.ArrayList<>();
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenCount = 0;
        
        for (int i = 0; i < attributesSpec.length(); i++) {
            char c = attributesSpec.charAt(i);
            
            if (c == '(') {
                parenCount++;
                current.append(c);
            } else if (c == ')') {
                parenCount--;
                current.append(c);
            } else if (c == ' ' && parenCount == 0) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        
        for (String part : parts) {
            int parenOpenPos = part.indexOf("(");
            int parenClosePos = part.lastIndexOf(")");
            
            if (parenOpenPos == -1 || parenClosePos == -1 || parenClosePos <= parenOpenPos) {
                throw new IllegalArgumentException("Format attribut invalide: " + part + " (doit être: nom(domaine) ou nom(nomDomaine))");
            }
            
            String attrName = part.substring(0, parenOpenPos).trim();
            String domainSpec = part.substring(parenOpenPos + 1, parenClosePos).trim();
            
            if (attrName.isEmpty()) {
                throw new IllegalArgumentException("Nom d'attribut vide");
            }
            
            if (domainSpec.isEmpty()) {
                throw new IllegalArgumentException("Domaine vide pour l'attribut: " + attrName);
            }
            
            Domaine domaine;
            
            if (!domainSpec.contains("|")) {
                if (domaineService != null && domaineService.domaineExists(domainSpec)) {
                    domaine = domaineService.getDomaine(domainSpec);
                    domaine = new Domaine(attrName, domaine.getType(), domaine.getMin(), domaine.getMax(), domaine.getValeursPossibles());
                } else {
                    throw new IllegalArgumentException("Domaine nommé '" + domainSpec + "' non trouvé. Créez-le d'abord avec MAMORONA DOMAINE.");
                }
            } else {
                domaine = Domaine.deserialize(attrName, domainSpec);
            }
            
            Attribut attr = new Attribut(attrName, domaine);
            attributs.add(attr);
        }
        
        return attributs;
    }
    
        private static String[] parseList(String list) {
        String[] items = list.split(",");
        for (int i = 0; i < items.length; i++) {
            items[i] = items[i].trim();
            if (items[i].isEmpty()) {
                throw new IllegalArgumentException("Élément vide détecté dans la liste");
            }
        }
        return items;
    }
    
        private static String[] parseValues(String values) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';
        
        for (int i = 0; i < values.length(); i++) {
            char c = values.charAt(i);
            
                        if ((c == '"' || c == '\'') && (i == 0 || values.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                } else {
                    current.append(c);
                }
            } 
                        else if (c == ',' && !inQuotes) {
                String val = current.toString().trim();
                if (!val.isEmpty()) {
                    result.add(val);
                }
                current = new StringBuilder();
            } 
                        else {
                current.append(c);
            }
        }

                String val = current.toString().trim();
        if (!val.isEmpty()) {
            result.add(val);
        }
        
        return result.toArray(new String[0]);
    }
    
    public static DeleteCommand parseDelete(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("FAFAO")) {
            throw new IllegalArgumentException(
                "Syntaxe DELETE invalide. Format:\n" +
                "FAFAO @ nom_table\n" +
                "FAFAO DATABASE nom_base"
            );
        }
        
                int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête DELETE");
        }
        
        String target = query.substring(atPos + 1).trim();
        
        if (target.isEmpty()) {
            throw new IllegalArgumentException("Cible manquante après '@'");
        }
        
        return new DeleteCommand(target);
    }
    
    public static ShowCommand parseShowTables(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("ASEHOY")) {
            throw new IllegalArgumentException(
                "Syntaxe SHOW invalide. Format:\n" +
                "ASEHOY TABLE * (pour afficher toutes les tables)\n" +
                "ASEHOY TABLE nom_table (pour afficher une table spécifique)\n" +
                "ASEHOY DATABASE * (pour afficher toutes les bases de données)\n" +
                "ASEHOY DATABASE nom_base (pour afficher une base de données spécifique)\n" +
                "ASEHOY DOMAINE * (pour afficher tous les domaines)\n" +
                "ASEHOY * (ancienne syntaxe pour afficher toutes les tables)"
            );
        }
        
        String[] parts = query.substring(6).trim().split("\\s+", 2);
        
        if (parts.length == 1) {
            String target = parts[0].trim();
            if (target.equals("*")) {
                return new ShowCommand("TABLE", "*");
            } else {
                return new ShowCommand("TABLE", target);
            }
        } else if (parts.length == 2) {
            String type = parts[0].toUpperCase();
            String target = parts[1].trim();
            
            if (target.isEmpty()) {
                throw new IllegalArgumentException("Cible manquante après 'ASEHOY " + type + "'");
            }
            
            return new ShowCommand(type, target);
        } else {
            throw new IllegalArgumentException("Syntaxe ASEHOY invalide");
        }
    }
    
    public static String parseUse(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("AMPIASAO")) {
            throw new IllegalArgumentException(
                "Syntaxe USE invalide. Format:\n" +
                "AMPIASAO nom_base_de_données"
            );
        }
        
        String dbName = query.substring(8).trim();
        
        if (dbName.isEmpty()) {
            throw new IllegalArgumentException("Nom de la base de données manquant après 'AMPIASAO'");
        }
        
        return dbName;
    }
    
    public static UpdateCommand parseUpdate(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (!queryUpper.startsWith("OVAY")) {
            throw new IllegalArgumentException(
                "Syntaxe UPDATE invalide. Format:\n" +
                "OVAY @ table AMBOARY colonne1 = valeur1, colonne2 = valeur2 RAHA condition\n" +
                "Exemple: OVAY @ Employes AMBOARY salaire = 50000 RAHA age > 30"
            );
        }
        
        int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête UPDATE");
        }
        
        String afterAt = query.substring(atPos + 1).trim();
        
        int amboaryPos = afterAt.toUpperCase().indexOf("AMBOARY");
        if (amboaryPos == -1) {
            throw new IllegalArgumentException("Mot-clé 'AMBOARY' manquant");
        }
        
        String tableStr = afterAt.substring(0, amboaryPos).trim();
        String afterAmboary = afterAt.substring(amboaryPos + 8).trim();
        
        if (tableStr.isEmpty()) {
            throw new IllegalArgumentException("Nom de la table manquant");
        }
        
        int rahaPos = afterAmboary.toUpperCase().indexOf("RAHA");
        String setStr, condition = null;
        
        if (rahaPos != -1) {
            setStr = afterAmboary.substring(0, rahaPos).trim();
            condition = afterAmboary.substring(rahaPos + 4).trim();
            if (condition.isEmpty()) {
                condition = null;
            }
        } else {
            setStr = afterAmboary;
        }
        
        if (setStr.isEmpty()) {
            throw new IllegalArgumentException("Clause AMBOARY vide");
        }
        
        // Parser les affectations colonne=valeur
        String[] assignments = parseAssignments(setStr);
        String[] colonnes = new String[assignments.length / 2];
        String[] valeurs = new String[assignments.length / 2];
        
        for (int i = 0; i < assignments.length; i += 2) {
            colonnes[i / 2] = assignments[i];
            valeurs[i / 2] = assignments[i + 1];
        }
        
        return new UpdateCommand(tableStr, colonnes, valeurs, condition);
    }
    
    public static AlterTableCommand parseAlterTable(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        if (queryUpper.startsWith("AMPIO COLUMN")) {
            return parseAddColumn(query);
        } else if (queryUpper.startsWith("FAFAO COLUMN")) {
            return parseDropColumn(query);
        } else {
            throw new IllegalArgumentException(
                "Syntaxe ALTER TABLE invalide. Format:\n" +
                "AMPIO COLUMN @ table nom_colonne(domaine)\n" +
                "FAFAO COLUMN @ table nom_colonne"
            );
        }
    }
    
    private static AlterTableCommand parseAddColumn(String query) throws IllegalArgumentException {
        String queryUpper = query.toUpperCase();
        
        int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête ADD COLUMN");
        }
        
        String afterAt = query.substring(atPos + 1).trim();
        
        int spacePos = afterAt.indexOf(" ");
        if (spacePos == -1) {
            throw new IllegalArgumentException("Format invalide: nom de colonne manquant");
        }
        
        String tableStr = afterAt.substring(0, spacePos).trim();
        String columnSpec = afterAt.substring(spacePos).trim();
        
        if (tableStr.isEmpty()) {
            throw new IllegalArgumentException("Nom de la table manquant");
        }
        
        if (columnSpec.isEmpty()) {
            throw new IllegalArgumentException("Spécification de colonne manquante");
        }
        
        // Parser la colonne avec son domaine
        int parenOpenPos = columnSpec.indexOf("(");
        int parenClosePos = columnSpec.lastIndexOf(")");
        
        if (parenOpenPos == -1 || parenClosePos == -1 || parenClosePos <= parenOpenPos) {
            throw new IllegalArgumentException("Format de colonne invalide: " + columnSpec + " (doit être: nom(domaine))");
        }
        
        String columnName = columnSpec.substring(0, parenOpenPos).trim();
        String domainSpec = columnSpec.substring(parenOpenPos + 1, parenClosePos).trim();
        
        if (columnName.isEmpty()) {
            throw new IllegalArgumentException("Nom de colonne vide");
        }
        
        if (domainSpec.isEmpty()) {
            throw new IllegalArgumentException("Domaine vide pour la colonne: " + columnName);
        }
        
    Domaine domaine = Domaine.deserialize(columnName, domainSpec);
        Attribut attribut = new Attribut(columnName, domaine);
        
        return new AlterTableCommand(AlterTableCommand.TYPE_ADD_COLUMN, tableStr, columnName, attribut);
    }   
    
    private static AlterTableCommand parseDropColumn(String query) throws IllegalArgumentException {
        String queryUpper = query.toUpperCase();
        
                        int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête DROP COLUMN");
        }
        
        String afterAt = query.substring(atPos + 1).trim();
        
        int spacePos = afterAt.indexOf(" ");
        if (spacePos == -1) {
            throw new IllegalArgumentException("Format invalide: nom de colonne manquant");
        }
        
        String tableStr = afterAt.substring(0, spacePos).trim();
        String columnName = afterAt.substring(spacePos).trim();
        
        if (tableStr.isEmpty()) {
            throw new IllegalArgumentException("Nom de la table manquant");
        }
        
        if (columnName.isEmpty()) {
            throw new IllegalArgumentException("Nom de la colonne manquant");
        }
        
        return new AlterTableCommand(AlterTableCommand.TYPE_DROP_COLUMN, tableStr, columnName);
    }
    
    private static String[] parseAssignments(String assignmentsStr) throws IllegalArgumentException {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';
        
        for (int i = 0; i < assignmentsStr.length(); i++) {
            char c = assignmentsStr.charAt(i);
            
                        if ((c == '"' || c == '\'') && (i == 0 || assignmentsStr.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                } else {
                    current.append(c);
                }
            }
                        else if (c == ',' && !inQuotes) {
                String assignment = current.toString().trim();
                if (!assignment.isEmpty()) {
                    String[] parts = assignment.split("=", 2);
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Format d'affectation invalide: " + assignment + " (doit être: colonne=valeur)");
                    }
                    result.add(parts[0].trim());
                    result.add(parts[1].trim());
                }
                current = new StringBuilder();
            }
                        else {
                current.append(c);
            }
        }
        
                String assignment = current.toString().trim();
        if (!assignment.isEmpty()) {
            String[] parts = assignment.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format d'affectation invalide: " + assignment + " (doit être: colonne=valeur)");
            }
            result.add(parts[0].trim());
            result.add(parts[1].trim());
        }
        
        return result.toArray(new String[0]);
    }
    
    public static CreateDomaineCommand parseCreateDomaine(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        // MAMORONA DOMAINE
        if (!queryUpper.startsWith("MAMORONA DOMAINE")) {
            throw new IllegalArgumentException(
                "Syntaxe CREATE DOMAINE invalide. Format:\n" +
                "MAMORONA DOMAINE nom @ Type|Range|Enum\n" +
                "Exemples:\n" +
                "MAMORONA DOMAINE AgeDomain @ Integer|0-120|\n" +
                "MAMORONA DOMAINE StatusDomain @ String||ACTIF,INACTIF,SUSPENDU\n" +
                "MAMORONA DOMAINE SalaireDomain @ Double|0-1000000|"
            );
        }
        
        String afterCmd = query.substring(16).trim(); 
        
        int atPos = afterCmd.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête CREATE DOMAINE");
        }
        
        String domaineName = afterCmd.substring(0, atPos).trim();
        String domainSpec = afterCmd.substring(atPos + 1).trim();
        
        if (domaineName.isEmpty()) {
            throw new IllegalArgumentException("Nom du domaine manquant");
        }
        
        if (domainSpec.isEmpty()) {
            throw new IllegalArgumentException("Spécification du domaine manquante après '@'");
        }
        
        Domaine domaine = Domaine.deserialize(domaineName, domainSpec);
        
        return new CreateDomaineCommand(domaineName, domaine);
    }
}
