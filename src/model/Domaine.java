package model;
public class Domaine {
    String nom;
    Class<?> type;
    Object min;
    Object max;
    Object[] valeursPossibles;

    public Domaine(String nom, Class<?> type, Object min, Object max, Object[] valeurs) {
        this.nom = nom;
        this.type = type;
        this.min = min;
        this.max = max;
        this.valeursPossibles = valeurs;
    }

    // Validation complète d'une valeur
    public void valider(Object valeur) {
        if (valeur == null) {
            throw new IllegalArgumentException("La valeur ne peut pas être null pour le domaine '" + nom + "'");
        }
        
        validerType(valeur);
        validerBornes(valeur);
        validerValeursPossibles(valeur);
    }

    private void validerType(Object valeur) {
        if (type != null && !type.isInstance(valeur)) {
            throw new IllegalArgumentException(
                "Type incorrect pour '" + nom + "'. Attendu: " + type.getSimpleName() + 
                ", Reçu: " + valeur.getClass().getSimpleName()
            );
        }
    }

    // Comparaison des bornes pour les types numériques
    private void validerBornes(Object valeur) {
        if (min == null && max == null) return;
        
        if (valeur instanceof Integer) {
            validerBornesInteger((Integer) valeur);
        } else if (valeur instanceof Double) {
            validerBornesDouble((Double) valeur);
        } else if (valeur instanceof Long) {
            validerBornesLong((Long) valeur);
        } else if (valeur instanceof Float) {
            validerBornesFloat((Float) valeur);
        }
    }

    private void validerBornesInteger(Integer valeur) {
        if (min != null && valeur < ((Number) min).intValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " inférieure au minimum " + min + " pour '" + nom + "'"
            );
        }
        if (max != null && valeur > ((Number) max).intValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " supérieure au maximum " + max + " pour '" + nom + "'"
            );
        }
    }

    private void validerBornesDouble(Double valeur) {
        if (min != null && valeur < ((Number) min).doubleValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " inférieure au minimum " + min + " pour '" + nom + "'"
            );
        }
        if (max != null && valeur > ((Number) max).doubleValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " supérieure au maximum " + max + " pour '" + nom + "'"
            );
        }
    }

    private void validerBornesLong(Long valeur) {
        if (min != null && valeur < ((Number) min).longValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " inférieure au minimum " + min + " pour '" + nom + "'"
            );
        }
        if (max != null && valeur > ((Number) max).longValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " supérieure au maximum " + max + " pour '" + nom + "'"
            );
        }
    }

    private void validerBornesFloat(Float valeur) {
        if (min != null && valeur < ((Number) min).floatValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " inférieure au minimum " + min + " pour '" + nom + "'"
            );
        }
        if (max != null && valeur > ((Number) max).floatValue()) {
            throw new IllegalArgumentException(
                "Valeur " + valeur + " supérieure au maximum " + max + " pour '" + nom + "'"
            );
        }
    }

    private void validerValeursPossibles(Object valeur) {
        if (valeursPossibles != null && !contient(valeursPossibles, valeur)) {
            throw new IllegalArgumentException(
                "Valeur '" + valeur + "' n'appartient pas aux valeurs possibles pour '" + nom + "'"
            );
        }
    }

    // Comparaison de deux domaines
    public boolean comparer(Domaine d) {
        if (!equals(type, d.type)) return false;
        if (!equals(min, d.min)) return false;
        if (!equals(max, d.max)) return false;
        return comparerTableaux(valeursPossibles, d.valeursPossibles);
    }

    private boolean comparerTableaux(Object[] tab1, Object[] tab2) {
        if (tab1 == null && tab2 == null) return true;
        if (tab1 == null || tab2 == null) return false;
        if (tab1.length != tab2.length) return false;
        
        for (Object v1 : tab1) {
            if (!contient(tab2, v1)) return false;
        }
        for (Object v2 : tab2) {
            if (!contient(tab1, v2)) return false;
        }
        return true;
    }

    private boolean contient(Object[] tab, Object valeur) {
        for (Object o : tab) {
            if (equals(o, valeur)) return true;
        }
        return false;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null) return o2 == null;
        return o1.equals(o2);
    }

    @Override
    public String toString() {
        String typeStr = type != null ? type.getSimpleName() : "any";
        String valeursStr = valeursPossibles != null ? valeursPossibles.length + " valeurs" : "illimité";
        return "Domaine[" + nom + ": type=" + typeStr + ", min=" + min + ", max=" + max + ", valeurs=" + valeursStr + "]";
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public Object getMin() {
        return min;
    }
    
    public Object getMax() {
        return max;
    }
    
    public Object[] getValeursPossibles() {
        return valeursPossibles;
    }
    
    /**
     * Sérialise le domaine au format: typeName|range|enumValues
     * Exemples: Integer|0-100|
     *           String||Manager,Developer,Designer
     *           Double|0.0-100000.0|
     */
    public String serialize() {
        String typeName = getTypeNameFromClass(type);
        String range = serializeRange();
        String enumVals = serializeEnum();
        return typeName + "|" + range + "|" + enumVals;
    }
    
    private String getTypeNameFromClass(Class<?> cls) {
        if (cls == Integer.class) return "Integer";
        if (cls == Double.class) return "Double";
        if (cls == Boolean.class) return "Boolean";
        return "String";
    }
    
    private String serializeRange() {
        if (min == null && max == null) return "";
        String minStr = min != null ? min.toString() : "";
        String maxStr = max != null ? max.toString() : "";
        if (minStr.isEmpty() && maxStr.isEmpty()) return "";
        return minStr + "-" + maxStr;
    }
    
    private String serializeEnum() {
        if (valeursPossibles == null || valeursPossibles.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valeursPossibles.length; i++) {
            sb.append(valeursPossibles[i].toString());
            if (i < valeursPossibles.length - 1) sb.append(",");
        }
        return sb.toString();
    }
    
    public static Domaine deserialize(String nomAttribut, String spec) {
        String[] parts = spec.split("\\|");
        
        String typeName = parts.length > 0 ? parts[0].trim() : "String";
        String range = parts.length > 1 ? parts[1].trim() : "";
        String enumStr = parts.length > 2 ? parts[2].trim() : "";
        
        Class<?> typeClass = parseType(typeName);
        Object minVal = null, maxVal = null;
        Object[] enumVals = null;

        // Parser la plage min-max
        if (!range.isEmpty() && range.contains("-")) {
            String[] rangeParts = range.split("-");
            if (rangeParts.length == 2) {
                minVal = parseValue(rangeParts[0].trim(), typeClass);
                maxVal = parseValue(rangeParts[1].trim(), typeClass);
            }
        }

        // Parser les énumérés
        if (!enumStr.isEmpty()) {
            String[] vals = enumStr.split(",");
            enumVals = new Object[vals.length];
            for (int i = 0; i < vals.length; i++) {
                enumVals[i] = vals[i].trim();
            }
        }

        return new Domaine(nomAttribut, typeClass, minVal, maxVal, enumVals);
    }
    
    private static Class<?> parseType(String type) {
        switch (type.toLowerCase()) {
            case "integer": return Integer.class;
            case "double": return Double.class;
            case "boolean": return Boolean.class;
            default: return String.class;
        }
    }

    private static Object parseValue(String value, Class<?> type) {
        if (value == null || value.isEmpty()) return null;

        try {
            if (type == Integer.class) {
                return Integer.parseInt(value);
            } else if (type == Double.class) {
                return Double.parseDouble(value);
            } else if (type == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur parsing valeur: " + value + " pour type " + type.getSimpleName());
        }
        return value;
    }
}