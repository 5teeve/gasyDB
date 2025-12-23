package controlleur;

import model.Attribut;
import model.Domaine;

public class MalgachQueryParser {
    public static QueryCommand parseSelect(String malgachQuery) throws IllegalArgumentException {
        if (malgachQuery == null || malgachQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Requête vide");
        }
        
        String query = malgachQuery.trim();
        String queryUpper = query.toUpperCase();
        
        //ALAIVO
        if (!queryUpper.startsWith("ALAIVO")) {
            throw new IllegalArgumentException(
                "Syntaxe SELECT invalide. Format:\n" +
                "ALAIVO colonnes @ table\n" +
                "ALAIVO colonnes @ table RAHA condition"
            );
        }
        
        //@
        int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant dans la requête SELECT");
        }
        
        //colonnes(entre alaivo et @)
        String colonnesStr = query.substring(6, atPos).trim();
        if (colonnesStr.isEmpty()) {
            throw new IllegalArgumentException("Paramètre 'colonnes' manquant");
        }
        
        //apres @
        String afterAt = query.substring(atPos + 1).trim();
        
        //RAHA
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
        
        // Traiter les colonnes
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
        
        // AMPIDIRO
        if (!queryUpper.startsWith("AMPIDIRO")) {
            throw new IllegalArgumentException(
                "Syntaxe INSERT invalide. Format:\n" +
                "AMPIDIRO @ table IRETO (valeurs)\n" +
                "AMPIDIRO @ table(colonnes) IRETO (valeurs)\n" +
                "Exemple: AMPIDIRO @ Employes IRETO ('Alice', 'Bob')\n" +
                "Ou: AMPIDIRO @ Employes(nom, prenom) IRETO ('Alice', 'Bob')"
            );
        }
        
        //@
        int atPos = queryUpper.indexOf("@");
        if (atPos == -1) {
            throw new IllegalArgumentException("Symbole '@' manquant");
        }
        
        // apres le @
        String afterAt = query.substring(atPos + 1).trim();
        
        // Chercher IRETO pour séparer la partie table/colonnes de la partie valeurs
        String afterAtUpper = afterAt.toUpperCase();
        int iretoPos = afterAtUpper.indexOf("IRETO");
        if (iretoPos == -1) {
            throw new IllegalArgumentException("Mot-clé 'IRETO' manquant");
        }
        
        String tableAndCols = afterAt.substring(0, iretoPos).trim();
        String afterIreto = afterAt.substring(iretoPos + 5).trim();
        
        // Parser la table et les colonnes optionnelles
        String tableStr;
        String[] colonnes = null;
        
        int parenPos = tableAndCols.indexOf("(");
        
        if (parenPos == -1) {
            // Pas de colonnes spécifiées
            tableStr = tableAndCols;
        } else {
            // Colonnes spécifiées entre parenthèses
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
        
        int atPos = tableSpec.indexOf("@");
        
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
        
        // Parser les attributs et leurs domaines
        java.util.List<Attribut> attributsList = parseAttributes(attributesSpec);
        Attribut[] attributs = attributsList.toArray(new Attribut[0]);
        
        return new CreateCommand(CreateCommand.TYPE_TABLE, tableName, attributs);
    }
    
    private static java.util.List<Attribut> parseAttributes(String attributesSpec) throws IllegalArgumentException {
        java.util.List<Attribut> attributs = new java.util.ArrayList<>();
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenCount = 0;
        
        // Découper par espaces mais en respectant les parenthèses
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
        
        // Parser chaque attribut(domaine)
        for (String part : parts) {
            int parenOpenPos = part.indexOf("(");
            int parenClosePos = part.lastIndexOf(")");
            
            if (parenOpenPos == -1 || parenClosePos == -1 || parenClosePos <= parenOpenPos) {
                throw new IllegalArgumentException("Format attribut invalide: " + part + " (doit être: nom(domaine))");
            }
            
            String attrName = part.substring(0, parenOpenPos).trim();
            String domainSpec = part.substring(parenOpenPos + 1, parenClosePos).trim();
            
            if (attrName.isEmpty()) {
                throw new IllegalArgumentException("Nom d'attribut vide");
            }
            
            if (domainSpec.isEmpty()) {
                throw new IllegalArgumentException("Domaine vide pour l'attribut: " + attrName);
            }
            
            // Parser la spécification de domaine et créer l'Attribut
            Domaine domaine = Domaine.deserialize(attrName, domainSpec);
            Attribut attr = new Attribut(attrName, domaine);
            attributs.add(attr);
        }
        
        return attributs;
    }
    
    // pour parser une liste séparée par des virgules
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
    
    //pour parser les valeurs (gère les quoted strings)
    private static String[] parseValues(String values) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';
        
        for (int i = 0; i < values.length(); i++) {
            char c = values.charAt(i);
            
            // guillemets
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
            // virgules en dehors des quotes
            else if (c == ',' && !inQuotes) {
                String val = current.toString().trim();
                if (!val.isEmpty()) {
                    result.add(val);
                }
                current = new StringBuilder();
            } 
            // ajouter les autres caractères
            else {
                current.append(c);
            }
        }

        // ajouter la dernière valeur
        String val = current.toString().trim();
        if (!val.isEmpty()) {
            result.add(val);
        }
        
        return result.toArray(new String[0]);
    }
}
