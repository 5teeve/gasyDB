package ui;

import java.awt.*;
import javax.swing.*;
import utils.UtilsUI;

public class MainView extends JFrame {
    
    private JTextArea sqlTextArea;
    private JButton executeButton;
    private JLabel statusLabel;
    private JLabel databaseLabel;
    private JPanel resultsPanel;
    
    public MainView() {
        configureFrame();
        
        JPanel mainPanel = createMainPanel();
        
        JPanel queryPanel = UtilsUI.createQueryPanel();
        sqlTextArea = UtilsUI.textAreaWithPlaceholder("ALAIVO * @ table\n RAHA condition");
        JScrollPane scrollPane = UtilsUI.createQueryScrollPane(sqlTextArea);
        queryPanel.add(scrollPane, BorderLayout.CENTER);
        
        resultsPanel = UtilsUI.createResultsPanel();
        
        mainPanel.add(queryPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        mainPanel.add(createSouthPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private void configureFrame() {
        setTitle("Gasy-SGBD v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(250, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }
    
    private JPanel createSouthPanel() {
        executeButton = UtilsUI.executeButton();
        statusLabel = UtilsUI.statusLabel("Ready");
        databaseLabel = new JLabel("Base: default");
        databaseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        databaseLabel.setForeground(new Color(30, 60, 120));
        databaseLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 200), 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        databaseLabel.setBackground(new Color(240, 248, 255));
        databaseLabel.setOpaque(true);
        
        return UtilsUI.createSouthPanel(executeButton, statusLabel, databaseLabel);
    }
    
    public String getSqlText() {
        return sqlTextArea.getText();
    }
    
    public void setSqlText(String text) {
        sqlTextArea.setText(text);
    }
    
    public JButton getExecuteButton() {
        return executeButton;
    }
    
    public void setStatus(String text) {
        statusLabel.setText(text);
    }
    
    // state: ready/error/loading
    public void setStatusColor(String state) {
        UtilsUI.setStatusColor(statusLabel, state);
    }
    
    public void setDatabaseName(String dbName) {
        databaseLabel.setText("Base: " + dbName);
    }
    
    public String getDatabaseName() {
        return databaseLabel.getText().replace("Base: ", "");
    }
    
    public JTextArea getSqlTextArea() {
        return sqlTextArea;
    }
    
    public void addResultsPanel(JPanel panel) {
        resultsPanel.removeAll();
        resultsPanel.add(panel, BorderLayout.CENTER);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    public void clearResultsPanel() {
        resultsPanel.removeAll();
        JLabel resultsPlaceholder = new JLabel("Mipoitra eto ny valiny. Andramo anie e");
        resultsPlaceholder.setForeground(new Color(150, 150, 150));
        resultsPlaceholder.setHorizontalAlignment(JLabel.CENTER);
        resultsPlaceholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        resultsPanel.add(resultsPlaceholder, BorderLayout.CENTER);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    public JPanel getResultsPanel() {
        return resultsPanel;
    }
    
    // Futur : Ajouter JTree pour bases/tables
    public void addNavigationPanel(JPanel navPanel) {
        // TODO: Ajouter JTree avec structures BDD
    }
    
    // Futur : Ajouter JMenuBar
    public void addMenuBar(JMenuBar menuBar) {
        setJMenuBar(menuBar);
    }
}
