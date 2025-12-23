package ui;

import java.awt.*;
import javax.swing.*;
import utils.UtilsUI;

public class MainView extends JFrame {
    
    private JTextArea sqlTextArea;
    private JButton executeButton;
    private JLabel statusLabel;
    private JPanel resultsPanel;
    
    public MainView() {
        configureFrame();
        
        JPanel mainPanel = createMainPanel();
        mainPanel.add(createQueryPanel(), BorderLayout.NORTH);
        mainPanel.add(createResultsPanel(), BorderLayout.CENTER);
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
    
    private JPanel createQueryPanel() {
        JPanel queryPanel = new JPanel(new BorderLayout(10, 5));
        queryPanel.setBackground(new Color(250, 250, 252));
        
        JLabel queryLabel = new JLabel("SQL Query:");
        queryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        queryPanel.add(queryLabel, BorderLayout.NORTH);
        
        sqlTextArea = UtilsUI.textAreaWithPlaceholder("ALAIVO * @ table\n RAHA condition");
        JScrollPane scrollPane = UtilsUI.scrollPane(sqlTextArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        scrollPane.setPreferredSize(new Dimension(850, 120));
        queryPanel.add(scrollPane, BorderLayout.CENTER);
        
        return queryPanel;
    }
    
    private JPanel createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBackground(new Color(245, 247, 250));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)), "Results"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JLabel resultsPlaceholder = new JLabel("Mipoitra eto ny valiny. Andramo anie e");
        resultsPlaceholder.setForeground(new Color(150, 150, 150));
        resultsPlaceholder.setHorizontalAlignment(JLabel.CENTER);
        resultsPlaceholder.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        resultsPanel.add(resultsPlaceholder, BorderLayout.CENTER);
        
        return resultsPanel;
    }
    
    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(15, 0));
        southPanel.setBackground(new Color(250, 250, 252));
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));
        southPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        
        executeButton = UtilsUI.executeButton();
        statusLabel = UtilsUI.statusLabel("Ready");
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(new Color(250, 250, 252));
        buttonsPanel.add(executeButton);
        
        southPanel.add(buttonsPanel, BorderLayout.WEST);
        southPanel.add(statusLabel, BorderLayout.EAST);
        
        return southPanel;
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
    
    public JTextArea getSqlTextArea() {
        return sqlTextArea;
    }
    
    public void addResultsPanel(JPanel panel) {
        resultsPanel.removeAll();
        resultsPanel.add(panel, BorderLayout.CENTER);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    public JPanel getResultsPanel() {
        return resultsPanel;
    }
    
    public void clearResultsPanel() {
        resultsPanel.removeAll();
        JLabel resultsPlaceholder = new JLabel("Mipoitra eto ny valiny (raha mahay ianao). Andramo anie e");
        resultsPlaceholder.setForeground(new Color(150, 150, 150));
        resultsPlaceholder.setHorizontalAlignment(JLabel.CENTER);
        resultsPlaceholder.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        resultsPanel.add(resultsPlaceholder, BorderLayout.CENTER);
        resultsPanel.revalidate();
        resultsPanel.repaint();
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
