package controlleur;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import controlleur.AlterTableCommand;
import controlleur.CreateCommand;
import controlleur.DeleteCommand;
import controlleur.InsertCommand;
import controlleur.QueryCommand;
import controlleur.ShowCommand;
import controlleur.UpdateCommand;
import model.Relation;
import service.DatabaseService;
import service.QueryService;
import service.TableService;
import ui.MainView;
import utils.DataPersistence;

public class QueryController {
    private MainView view;
    private TableService tableService;
    private QueryService queryService;
    private DatabaseService databaseService;
    
    public QueryController(MainView view) {
        this.view = view;
        this.databaseService = new DatabaseService();
        DataPersistence persistence = new DataPersistence();
        this.tableService = new TableService(persistence);
        this.queryService = new QueryService(tableService, databaseService, persistence);
        initListeners();
        loadInitialData();
        updateDatabaseDisplay();
    }
    
    private void initListeners() {
        view.getExecuteButton().addActionListener(e -> handleExecute());
    }
    
    private void loadInitialData() {
        String dbName = databaseService.getCurrentDatabase();
        tableService.setCurrentDatabase(dbName);
        tableService.loadAllTables();
    }
    
    private void updateDatabaseDisplay() {
        SwingUtilities.invokeLater(() -> {
            String dbName = databaseService.getCurrentDatabase();
            view.setDatabaseName(dbName);
            tableService.setCurrentDatabase(dbName);
            tableService.loadAllTables();
        });
    }
    
    public void addRelation(String nom, Relation relation) {
        tableService.addTable(nom, relation);
    }
    
    public Map<String, Relation> getRelations() {
        return tableService.getAllTables();
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
                Object result = processQuery(query);
                
                SwingUtilities.invokeLater(() -> {
                    if (result == null) {
                        displaySuccessMessage("OPÉRATION RÉUSSIE", "Création effectuée avec succès", "L'opération a été effectuée avec succès");
                    } else if (result instanceof Relation) {
                        displayResultsInUI((Relation) result);
                    } else if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, ?> resultMap = (Map<String, ?>) result;
                        if (!resultMap.isEmpty() && resultMap.values().iterator().next() instanceof Relation) {
                            @SuppressWarnings("unchecked")
                            Map<String, Relation> tablesMap = (Map<String, Relation>) result;
                            displayTablesInUI(tablesMap);
                        } else {
                            displaySuccessMessage("OPÉRATION RÉUSSIE", result.toString(), "L'opération a été effectuée avec succès");
                        }
                    } else if (result instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> databasesList = (List<String>) result;
                        displayDatabasesInUI(databasesList);
                    } else if (result instanceof String) {
                        displaySuccessMessage("OPÉRATION RÉUSSIE", (String) result, "L'opération a été effectuée avec succès");
                    }
                    view.setStatus("Requête exécutée avec succès");
                    view.setStatusColor("ready");
                });
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
    
    private Object processQuery(String query) throws Exception {
        if (MalgachQueryParser.isSelectQuery(query)) {
            QueryCommand cmd = MalgachQueryParser.parseSelect(query);
            return queryService.executeSelect(cmd);
        } else if (MalgachQueryParser.isInsertQuery(query)) {
            InsertCommand cmd = MalgachQueryParser.parseInsert(query);
            queryService.executeInsert(cmd);
            return "Insertion effectuée avec succès";
        } else if (MalgachQueryParser.isCreateQuery(query)) {
            try {
                CreateCommand cmd = MalgachQueryParser.parseCreate(query);
                queryService.executeCreate(cmd);
                
                SwingUtilities.invokeLater(() -> {
                    view.setStatus("Création effectuée avec succès");
                    view.setStatusColor("ready");
                });
                
                if (cmd.type == CreateCommand.TYPE_DATABASE) {
                    updateDatabaseDisplay();
                } else if (cmd.type == CreateCommand.TYPE_TABLE) {
                    updateDatabaseDisplay();
                }
                return null;
            } catch (Exception e) {
                throw e;
            }
        } else if (MalgachQueryParser.isAlterTableQuery(query)) {
            AlterTableCommand cmd = MalgachQueryParser.parseAlterTable(query);
            queryService.executeAlterTable(cmd);
            return "Modification de table effectuée avec succès";
        } else if (MalgachQueryParser.isDeleteQuery(query)) {
            DeleteCommand cmd = MalgachQueryParser.parseDelete(query);
            queryService.executeDelete(cmd);
            return "Suppression effectuée avec succès";
        } else if (MalgachQueryParser.isShowQuery(query)) {
            ShowCommand cmd = MalgachQueryParser.parseShowTables(query);
            return queryService.executeShow(cmd);
        } else if (MalgachQueryParser.isUseQuery(query)) {
            String dbName = MalgachQueryParser.parseUse(query);
            queryService.executeUse(dbName);
            updateDatabaseDisplay();
            return "Base de données changée avec succès";
        } else if (MalgachQueryParser.isUpdateQuery(query)) {
            UpdateCommand cmd = MalgachQueryParser.parseUpdate(query);
            queryService.executeUpdate(cmd);
            return "Mise à jour effectuée avec succès";
        } else {
            throw new IllegalArgumentException("Requête non reconnue");
        }
    }
    
    private void displayResultsInUI(Relation relation) {
        SwingUtilities.invokeLater(() -> {
            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBackground(new Color(250, 250, 250));
            
            // Créer le tableau
            String[] columnNames = new String[relation.getAttributs().length];
            for (int i = 0; i < relation.getAttributs().length; i++) {
                columnNames[i] = relation.getAttributs()[i].getNom();
            }
            
            Object[][] data = relation.getTuples();
            
            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setRowHeight(25);
            
            JScrollPane scrollPane = new JScrollPane(table);
            resultPanel.add(scrollPane, BorderLayout.CENTER);
            
            view.addResultsPanel(resultPanel);
        });
    }
    
    private void displayTablesInUI(Map<String, Relation> tables) {
        SwingUtilities.invokeLater(() -> {
            JPanel tablesPanel = new JPanel(new BorderLayout(10, 10));
            tablesPanel.setBackground(new Color(245, 247, 250));
            
            if (tables.isEmpty()) {
                JLabel titleLabel = new JLabel("Aucune table trouvée dans cette base de données");
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(new Color(150, 50, 50));
                JPanel titlePanel = new JPanel();
                titlePanel.setBackground(new Color(245, 247, 250));
                titlePanel.add(titleLabel);
                tablesPanel.add(titlePanel, BorderLayout.CENTER);
            } else {
                JLabel titleLabel = new JLabel("Tables trouvées: " + tables.size());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(new Color(50, 100, 150));
                JPanel titlePanel = new JPanel();
                titlePanel.setBackground(new Color(245, 247, 250));
                titlePanel.add(titleLabel);
                tablesPanel.add(titlePanel, BorderLayout.NORTH);
                
                JTextArea listArea = new JTextArea();
                listArea.setEditable(false);
                listArea.setBackground(Color.WHITE);
                listArea.setForeground(Color.BLACK);
                
                for (String tableName : tables.keySet()) {
                    Relation relation = tables.get(tableName);
                    int tupleCount = relation.getTuples().length;
                    int attrCount = relation.getAttributs().length;
                    listArea.append("• " + tableName + " (" + tupleCount + " tuples, " + attrCount + " attributs)\n");
                }
                
                listArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                listArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
                
                JScrollPane scrollPane = new JScrollPane(listArea);
                scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
                tablesPanel.add(scrollPane, BorderLayout.CENTER);
            }
            
            view.addResultsPanel(tablesPanel);
        });
    }
    
    private void displayDatabasesInUI(List<String> databases) {
        SwingUtilities.invokeLater(() -> {
            JPanel databasesPanel = new JPanel(new BorderLayout(10, 10));
            databasesPanel.setBackground(new Color(245, 247, 250));
            
            if (databases.isEmpty()) {
                JLabel titleLabel = new JLabel("Aucune base de données trouvée");
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(new Color(150, 50, 50));
                JPanel titlePanel = new JPanel();
                titlePanel.setBackground(new Color(245, 247, 250));
                titlePanel.add(titleLabel);
                databasesPanel.add(titlePanel, BorderLayout.CENTER);
            } else {
                JLabel titleLabel = new JLabel("Bases de données trouvées: " + databases.size());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(new Color(50, 100, 150));
                JPanel titlePanel = new JPanel();
                titlePanel.setBackground(new Color(245, 247, 250));
                titlePanel.add(titleLabel);
                databasesPanel.add(titlePanel, BorderLayout.NORTH);
                
                JTextArea listArea = new JTextArea();
                listArea.setEditable(false);
                listArea.setBackground(Color.WHITE);
                listArea.setForeground(Color.BLACK);
                
                for (String dbName : databases) {
                    listArea.append("• " + dbName + "\n");
                }
                
                listArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                listArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
                
                JScrollPane scrollPane = new JScrollPane(listArea);
                scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
                databasesPanel.add(scrollPane, BorderLayout.CENTER);
            }
            
            view.addResultsPanel(databasesPanel);
        });
    }
    
    private void displaySuccessMessage(String title, String message, String details) {
        SwingUtilities.invokeLater(() -> {
            JPanel successPanel = new JPanel(new BorderLayout(10, 10));
            successPanel.setBackground(new Color(245, 255, 245));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(50, 150, 50));
            JPanel titlePanel = new JPanel();
            titlePanel.setBackground(new Color(235, 255, 235));
            titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            titlePanel.add(titleLabel);
            successPanel.add(titlePanel, BorderLayout.NORTH);
            
            JTextArea messageArea = new JTextArea(message);
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setBackground(new Color(250, 255, 250));
            messageArea.setForeground(new Color(40, 100, 40));
            messageArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            messageArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            
            JTextArea detailsArea = new JTextArea("Détails :\n" + details);
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setBackground(new Color(240, 255, 240));
            detailsArea.setForeground(new Color(30, 80, 30));
            detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            detailsArea.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(100, 200, 100), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            
            JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
            centerPanel.setBackground(new Color(245, 255, 245));
            centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            centerPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
            centerPanel.add(detailsArea, BorderLayout.SOUTH);
            
            successPanel.add(centerPanel, BorderLayout.CENTER);
            
            view.addResultsPanel(successPanel);
            view.setStatus("Succès: " + title);
        });
    }
    
    private void displayErrorMessage(String title, String message, String suggestion) {
        SwingUtilities.invokeLater(() -> {
            JPanel errorPanel = new JPanel(new BorderLayout(10, 10));
            errorPanel.setBackground(new Color(255, 245, 245));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(200, 50, 50));
            JPanel titlePanel = new JPanel();
            titlePanel.setBackground(new Color(255, 235, 235));
            titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            titlePanel.add(titleLabel);
            errorPanel.add(titlePanel, BorderLayout.NORTH);
            
            JTextArea messageArea = new JTextArea(message);
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setBackground(new Color(255, 250, 250));
            messageArea.setForeground(new Color(150, 30, 30));
            messageArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            messageArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            
            JTextArea suggestionArea = new JTextArea("Suggestion :\n" + suggestion);
            suggestionArea.setEditable(false);
            suggestionArea.setLineWrap(true);
            suggestionArea.setWrapStyleWord(true);
            suggestionArea.setBackground(new Color(255, 245, 245));
            suggestionArea.setForeground(new Color(100, 50, 50));
            suggestionArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            suggestionArea.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(200, 100, 100), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            
            JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
            centerPanel.setBackground(new Color(255, 245, 245));
            centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
            centerPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
            centerPanel.add(suggestionArea, BorderLayout.SOUTH);
            
            errorPanel.add(centerPanel, BorderLayout.CENTER);
            
            view.addResultsPanel(errorPanel);
            view.setStatus("Erreur: " + title);
        });
    }
}
