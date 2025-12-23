package controlleur;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Attribut;
import model.Relation;
import ui.MainView;
import utils.DataPersistence;

public class QueryController {
    private MainView view;
    private Map<String, Relation> relations;
    private DataPersistence persistence;
    
    public QueryController(MainView view) {
        this.view = view;
        this.relations = new HashMap<>();
        this.persistence = new DataPersistence();
        initListeners();
    }
    
    private void initListeners() {
        view.getExecuteButton().addActionListener(e -> handleExecute());
    }
    
    private void handleExecute() {
        String query = view.getSqlText().trim();
        
        if (query.isEmpty()) {
            view.setStatus("Requête vide");
            view.setStatusColor("error");
            return;
        }
        
        view.setStatus("Exécution...");
        view.setStatusColor("loading");
        
        new Thread(() -> {
            try {
                if (MalgachQueryParser.isSelectQuery(query)) {
                    try {
                        QueryCommand cmd = MalgachQueryParser.parseSelect(query);
                        executeCommand(cmd);
                        
                        SwingUtilities.invokeLater(() -> {
                            view.setStatus("Requête exécutée avec succès");
                            view.setStatusColor("ready");
                        });
                    } catch (IllegalArgumentException e) {
                        SwingUtilities.invokeLater(() -> {
                            displayErrorMessage(
                                "ERREUR DE SYNTAXE - SÉLECTION (ALAIVO)",
                                "Votre syntaxe de sélection est incorrecte.\n\n" + e.getMessage(),
                                "Syntaxe correcte:\nALAIVO colonne1, colonne2 @ table\nALAIVO * @ table RAHA condition"
                            );
                            view.setStatusColor("error");
                        });
                    }
                } else if (MalgachQueryParser.isInsertQuery(query)) {
                    try {
                        InsertCommand cmd = MalgachQueryParser.parseInsert(query);
                        executeInsert(cmd);
                        
                        SwingUtilities.invokeLater(() -> {
                            view.clearResultsPanel();
                            view.setStatus("Insertion effectuée avec succès");
                            view.setStatusColor("ready");
                        });
                    } catch (IllegalArgumentException e) {
                        SwingUtilities.invokeLater(() -> {
                            displayErrorMessage(
                                "ERREUR DE SYNTAXE - INSERTION (AMPIDIRO)",
                                "Votre syntaxe d'insertion est incorrecte.\n\n" + e.getMessage(),
                                "Syntaxe correcte:\nAMPIDIRO @ table(col1, col2) IRETO (val1, val2)"
                            );
                            view.setStatusColor("error");
                        });
                    }
                } else if (MalgachQueryParser.isCreateQuery(query)) {
                    try {
                        CreateCommand cmd = MalgachQueryParser.parseCreate(query);
                        executeCreate(cmd);
                        
                        SwingUtilities.invokeLater(() -> {
                            view.clearResultsPanel();
                            view.setStatus("Création effectuée avec succès");
                            view.setStatusColor("ready");
                        });
                    } catch (IllegalArgumentException e) {
                        SwingUtilities.invokeLater(() -> {
                            displayErrorMessage(
                                "ERREUR DE SYNTAXE - CRÉATION (MAMORONA)",
                                "Votre syntaxe de création est incorrecte.\n\n" + e.getMessage(),
                                "Syntaxe correcte:\nMAMORONA TABLE nom\nMAMORONA DATABASE nom"
                            );
                            view.setStatusColor("error");
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        displayErrorMessage(
                            "REQUÊTE NON RECONNUE",
                            "Cette requête n'a pas pu être reconnue.",
                            "Commandes supportées:\n- ALAIVO (SELECT)\n- AMPIDIRO (INSERT)\n- MAMORONA (CREATE)"
                        );
                        view.setStatusColor("error");
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    displayErrorMessage(
                        "ERREUR D'EXÉCUTION",
                        "Une erreur s'est produite lors de l'exécution.\n\n" + ex.getMessage(),
                        "Vérifiez que:\n- La table existe\n- Les colonnes sont correctes\n- La syntaxe est valide"
                    );
                    view.setStatusColor("error");
                });
                ex.printStackTrace();
            }
        }).start();
    }
    
    private void executeCommand(QueryCommand cmd) throws Exception {
        Relation r = relations.get(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        Relation resultat = r;
        
        if (cmd.condition != null && !cmd.condition.isEmpty()) {
            resultat = resultat.selection(cmd.condition);
        }
        
        if (!isSelectAll(cmd.colonnes)) {
            resultat = resultat.projection(cmd.colonnes);
        }
        
        resultat.afficherRelation();
        displayResultsInUI(resultat);
    }
    
    private boolean isSelectAll(String[] colonnes) {
        return colonnes.length == 1 && colonnes[0].equals("*");
    }
    
    private void displayResultsInUI(Relation resultat) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = new DefaultTableModel();
            
            Attribut[] attributs = resultat.getAttributs();
            for (Attribut attr : attributs) {
                model.addColumn(attr.getNom());
            }
            
            Object[][] tuples = resultat.getTuples();
            int nbTuples = resultat.getNbTuples();
            for (int i = 0; i < nbTuples; i++) {
                model.addRow(tuples[i]);
            }
            
            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);
            JPanel panel = new JPanel(new java.awt.BorderLayout());
            panel.add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
            
            view.addResultsPanel(panel);
        });
    }
    
    private void executeInsert(InsertCommand cmd) throws Exception {
        Relation r = relations.get(cmd.table);
        if (r == null) {
            throw new IllegalArgumentException("Table '" + cmd.table + "' introuvable");
        }
        
        String[] colonnesAUtiliser = cmd.colonnes;
        if (colonnesAUtiliser == null) {
            Attribut[] attributs = r.getAttributs();
            colonnesAUtiliser = new String[attributs.length];
            for (int i = 0; i < attributs.length; i++) {
                colonnesAUtiliser[i] = attributs[i].getNom();
            }
        }
        
        if (colonnesAUtiliser.length != cmd.valeurs.length) {
            throw new IllegalArgumentException(
                "Nombre de colonnes (" + colonnesAUtiliser.length + ") différent du nombre de valeurs (" + cmd.valeurs.length + ")"
            );
        }
        
        Object[] tuple = convertValuesToObjects(cmd.valeurs, colonnesAUtiliser, r);
        r.ajouterTuple(tuple);
        persistence.saveInsert(cmd.table, colonnesAUtiliser, cmd.valeurs);
    }
    
    private Object[] convertValuesToObjects(String[] valeurs, String[] colonnes, Relation relation) {
        Object[] result = new Object[valeurs.length];
        Attribut[] attributs = relation.getAttributs();
        
        Map<String, Attribut> attrMap = new HashMap<>();
        for (Attribut attr : attributs) {
            attrMap.put(attr.getNom(), attr);
        }
        
        for (int i = 0; i < valeurs.length; i++) {
            String val = valeurs[i];
            String colonneNom = colonnes[i];
            Attribut attribut = attrMap.get(colonneNom);
            
            Class<?> expectedType = (attribut != null) ? attribut.getDomaine().getType() : String.class;
            
            result[i] = convertValue(val, expectedType);
        }
        return result;
    }
    
    private Object convertValue(String val, Class<?> expectedType) {
        if (val == null || val.isEmpty()) {
            return null;
        }
        
        try {
            if (expectedType == Boolean.class) {
                return Boolean.parseBoolean(val);
            } else if (expectedType == Integer.class) {
                return Integer.parseInt(val);
            } else if (expectedType == Double.class) {
                return Double.parseDouble(val);
            } else {
                return val;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Cannot convert '" + val + "' to " + expectedType.getSimpleName()
            );
        }
    }
    
    private void executeCreate(CreateCommand cmd) throws Exception {
        if (cmd.type == CreateCommand.TYPE_DATABASE) {
            persistence.createDatabase(cmd.name);
            view.setStatus("Base de données " + cmd.name + " créée");
        } else if (cmd.type == CreateCommand.TYPE_TABLE) {
            persistence.createTable(cmd.name);
            if (cmd.attributs != null && cmd.attributs.length > 0) {
                Relation relation = new Relation(cmd.name, cmd.attributs);
                addRelation(cmd.name, relation);
                view.setStatus("Table " + cmd.name + " créée avec " + cmd.attributs.length + " attributs");
            } else {
                view.setStatus("Table " + cmd.name + " créée");
            }
        }
    }
    
    public void addRelation(String nom, Relation relation) {
        relations.put(nom, relation);
        persistence.createTableSchemaWithDomains(nom, relation.getAttributs());
    }
    
    public Map<String, Relation> getRelations() {
        return relations;
    }
    
    private void displayErrorMessage(String title, String message, String suggestion) {
        JPanel errorPanel = new JPanel(new BorderLayout(10, 10));
        errorPanel.setBackground(new Color(255, 245, 245));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(200, 50, 50));
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 235, 235));
        titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        titlePanel.add(titleLabel);
        errorPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Error details
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(255, 250, 250));
        messageArea.setForeground(new Color(50, 50, 50));
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        messageArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        // Suggestion area
        JTextArea suggestionArea = new JTextArea("Ireto andramana :\n" + suggestion);
        suggestionArea.setEditable(false);
        suggestionArea.setLineWrap(true);
        suggestionArea.setWrapStyleWord(true);
        suggestionArea.setBackground(new Color(240, 255, 240));
        suggestionArea.setForeground(new Color(40, 100, 40));
        suggestionArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        suggestionArea.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(100, 200, 100), 1),
            javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Center panel with both text areas
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(new Color(255, 245, 245));
        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        centerPanel.add(suggestionArea, BorderLayout.SOUTH);
        
        errorPanel.add(centerPanel, BorderLayout.CENTER);
        
        view.addResultsPanel(errorPanel);
        view.setStatus("Erreur: " + title);
    }
}
