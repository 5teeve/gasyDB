package model;
public class Relation {
    String nom;
    Attribut[] attributs;
    Object[][] tuples;
    int nbTuples;

    public Relation(String nom, Attribut[] attributs) {
        this.nom = nom;
        this.attributs = attributs;
        this.tuples = new Object[10][];
        this.nbTuples = 0;
    }
    
    public Attribut[] getAttributs() {
        return attributs;
    }
    
    public Object[][] getTuples() {
        return tuples;
    }
    
    public int getNbTuples() {
        return nbTuples;
    }

    public Relation selection(String condition) {
        Relation resultat = new Relation("Selection(" + nom + ")", attributs);

        for (int i = 0; i < nbTuples; i++) {
            Object[] tuple = tuples[i];
            if (evaluer(condition, tuple)) {
                resultat.ajouterTuple(tuple);
            }
        }

        return resultat;
    }

    private boolean evaluer(String cond, Object[] tuple) {
        cond = nettoyer(cond);
        
        if (cond.startsWith("(") && cond.endsWith(")")) {
            int niveau = 0;
            boolean parenthesesExternesCompletes = true;
            for (int i = 0; i < cond.length() - 1; i++) {
                if (cond.charAt(i) == '(') niveau++;
                else if (cond.charAt(i) == ')') niveau--;
                if (niveau == 0) {
                    parenthesesExternesCompletes = false;
                    break;
                }
            }
            if (parenthesesExternesCompletes) {
                cond = cond.substring(1, cond.length() - 1);
                cond = nettoyer(cond);
            }
        }
        
        int posOr = trouverOperateurLogique(cond, "OR");
        if (posOr != -1) {
            String gauche = nettoyer(cond.substring(0, posOr));
            String droite = nettoyer(cond.substring(posOr + 2));
            return evaluer(gauche, tuple) || evaluer(droite, tuple);
        }
        
        int posAnd = trouverOperateurLogique(cond, "AND");
        if (posAnd != -1) {
            String gauche = nettoyer(cond.substring(0, posAnd));
            String droite = nettoyer(cond.substring(posAnd + 3));
            return evaluer(gauche, tuple) && evaluer(droite, tuple);
        }
        
        return evaluerConditionSimple(cond, tuple);
    }
    
    private int trouverOperateurLogique(String cond, String op) {
        int niveau = 0;
        for (int i = 0; i <= cond.length() - op.length(); i++) {
            char c = cond.charAt(i);
            if (c == '(') {
                niveau++;
            } else if (c == ')') {
                niveau--;
            } else if (niveau == 0) {
                boolean match = true;
                for (int j = 0; j < op.length(); j++) {
                    if (Character.toUpperCase(cond.charAt(i + j)) != op.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    boolean avantOk = (i == 0) || !Character.isLetterOrDigit(cond.charAt(i - 1));
                    boolean apresOk = (i + op.length() >= cond.length()) || 
                                     !Character.isLetterOrDigit(cond.charAt(i + op.length()));
                    if (avantOk && apresOk) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    private boolean evaluerConditionSimple(String cond, Object[] tuple) {
        String[] ops = { "!=", ">=", "<=", "=", ">", "<" };

        for (int i = 0; i < ops.length; i++) {
            int pos = cond.indexOf(ops[i]);
            if (pos != -1) {
                String attr = nettoyer(cond.substring(0, pos));
                String val = nettoyer(cond.substring(pos + ops[i].length()));
                val = retirerQuotes(val);

                Object valeurTuple = getValeurAttribut(attr, tuple);
                if (valeurTuple == null)
                    return false;

                return comparer(valeurTuple, val, ops[i]);
            }
        }
        return false;
    }

    private boolean comparer(Object v1, String v2, String op) {
        boolean numerique = true;
        double a = 0, b = 0;

        try {
            a = Double.parseDouble(v1.toString());
            b = Double.parseDouble(v2);
        } catch (Exception e) {
            numerique = false;
        }

        if (numerique) {
            if (op.equals("="))
                return a == b;
            if (op.equals("!="))
                return a != b;
            if (op.equals(">"))
                return a > b;
            if (op.equals("<"))
                return a < b;
            if (op.equals(">="))
                return a >= b;
            if (op.equals("<="))
                return a <= b;
            return false;
        } else {
            String s1 = v1.toString();
            return switch (op) {
                case "=" -> s1.equalsIgnoreCase(v2);
                case "!=" -> !s1.equalsIgnoreCase(v2);
                default -> false;
            };
        }
    }

    private Object getValeurAttribut(String nomAttr, Object[] tuple) {
        for (int i = 0; i < attributs.length; i++) {
            if (attributs[i].nom.equalsIgnoreCase(nomAttr)) {
                return tuple[i];
            }
        }
        return null;
    }

    private String nettoyer(String s) {
        int i = 0, j = s.length() - 1;
        while (i <= j && (s.charAt(i) == ' ' || s.charAt(i) == '\t'))
            i++;
        while (j >= i && (s.charAt(j) == ' ' || s.charAt(j) == '\t'))
            j--;
        return s.substring(i, j + 1);
    }

    private String retirerQuotes(String s) {
        if (s.startsWith("'") && s.endsWith("'") && s.length() > 1)
            return s.substring(1, s.length() - 1);
        return s;
    }

    public Relation projection(String[] nomsAttributs) {
        Attribut[] nouveauxAttributs = new Attribut[nomsAttributs.length];
        int[] indices = new int[nomsAttributs.length];

        for (int i = 0; i < nomsAttributs.length; i++) {
            boolean trouve = false;
            String nomNettoye = nettoyer(nomsAttributs[i]);
            
            for (int j = 0; j < attributs.length; j++) {
                if (attributs[j].nom.equalsIgnoreCase(nomNettoye)) {
                    nouveauxAttributs[i] = attributs[j];
                    indices[i] = j;
                    trouve = true;
                    break;
                }
            }
            if (!trouve) {
                throw new IllegalArgumentException("Attribut " + nomNettoye + " introuvable dans " + nom);
            }
        }

        Relation projection = new Relation("Projection(" + nom + ")", nouveauxAttributs);

        for (int i = 0; i < nbTuples; i++) {
            Object[] nouveauTuple = new Object[nomsAttributs.length];
            for (int j = 0; j < nomsAttributs.length; j++) {
                nouveauTuple[j] = tuples[i][indices[j]];
            }

            if (!projection.contientTuple(nouveauTuple)) {
                projection.ajouterTuple(nouveauTuple);
            }
        }

        return projection;
    }

    public Relation union(Relation r) {
        Relation union = new Relation(this.nom + " union " + r.nom, this.attributs);

        for (int i = 0; i < this.nbTuples; i++) {
            union.ajouterTuple(tuples[i]);
        }

        for (int i = 0; i < r.nbTuples; i++) {
            if (!union.contientTuple(r.tuples[i])) {
                union.ajouterTuple(r.tuples[i]);
            }
        }

        return union;
    }

    public Relation difference(Relation r) {
        Relation diff = new Relation(this.nom + " - " + r.nom, attributs);
        for (int i = 0; i < nbTuples; i++) {
            Object[] tuple = tuples[i];
            if (!r.contientTuple(tuple)) {
                diff.ajouterTuple(tuple);
            }
        }

        return diff;
    }

    public Relation intersection(Relation r) {
        Relation intersection = new Relation(this.nom + " inter " + r.nom, attributs);
        for (int i = 0; i < nbTuples; i++) {
            Object[] tuple = tuples[i];
            if (r.contientTuple(tuple)) {
                intersection.ajouterTuple(tuple);
            }
        }

        return intersection;
    }

    public boolean contientTuple(Object[] tuple) {
        if(tuple == null) return false;
        
        for (int i = 0; i < this.nbTuples; i++) {
            boolean bool = true;
            for (int j = 0; j < this.attributs.length; j++) {
                if (!this.tuples[i][j].equals(tuple[j])) {
                    bool = false;
                    break;
                }
            }
            if (bool) {
                return true;
            }
        }
        return false;
    }

    public Relation produitCartesien(Relation r) {
        Attribut[] nouveauxAttributs = new Attribut[this.attributs.length + r.attributs.length];

        for (int i = 0; i < this.attributs.length; i++) {
            nouveauxAttributs[i] = this.attributs[i];
        }
        for (int i = 0; i < r.attributs.length; i++) {
            nouveauxAttributs[this.attributs.length + i] = r.attributs[i];
        }

        Relation resultat = new Relation(this.nom + " × " + r.nom, nouveauxAttributs);

        for (int i = 0; i < this.nbTuples; i++) {
            for (int j = 0; j < r.nbTuples; j++) {
                Object[] nouveauTuple = new Object[this.tuples[i].length + r.tuples[j].length];

                for (int k = 0; k < this.tuples[i].length; k++) {
                    nouveauTuple[k] = this.tuples[i][k];
                }
                for (int k = 0; k < r.tuples[j].length; k++) {
                    nouveauTuple[this.tuples[i].length + k] = r.tuples[j][k];
                }

                resultat.ajouterTuple(nouveauTuple);
            }
        }

        return resultat;
    }

    public void ajouterTuple(Object[] tuple) {
        if (tuple == null) {
            System.err.println("Le tuple dans " + this.nom + " doit pas etre null");
            return;
        }

        if (tuple.length != attributs.length) {
            System.out.println("Le tuple doit avoir " + attributs.length + " valeurs, mais " +
                    tuple.length + " ont été fournies");
            return;
        }

        for (int i = 0; i < tuple.length; i++) {
            try {
                attributs[i].getDomaine().valider(tuple[i]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Erreur à la position " + i + " (attribut '" + attributs[i].nom + "'): " +
                                e.getMessage());
            }
        }

        if (nbTuples == tuples.length) {
            agrandirTableau();
        }
        tuples[nbTuples++] = tuple;
        System.out.println("Tuple ajouté avec succès: " + afficherTuple(tuple));
    }

    private void agrandirTableau() {
        Object[][] nouveauTableau = new Object[tuples.length * 2][];
        for (int i = 0; i < nbTuples; i++) {
            nouveauTableau[i] = tuples[i];
        }
        tuples = nouveauTableau;
    }

    public void afficherRelation() {
        System.out.println("\n=== Relation: " + nom + " ===");

        System.out.print("| ");
        for (Attribut attr : attributs) {
            System.out.printf("%-15s | ", attr.nom);
        }
        System.out.println();

        for (int i = 0; i < attributs.length; i++) {
            System.out.print("------------------");
        }
        System.out.println();

        for (int i = 0; i < nbTuples; i++) {
            System.out.print("| ");
            for (Object val : tuples[i]) {
                System.out.printf("%-15s | ", val);
            }
            System.out.println();
        }
        System.out.println();
    }

    private String afficherTuple(Object[] tuple) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tuple.length; i++) {
            sb.append(tuple[i]);
            if (i < tuple.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}