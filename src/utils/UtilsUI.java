package utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import ui.PlaceholderTextArea;

public class UtilsUI {
    
    public static final Color COLOR_BG_LIGHT = new Color(245, 245, 245);
    public static final Color COLOR_BORDER = new Color(200, 200, 200);
    public static final Color COLOR_BUTTON_BG = new Color(70, 130, 180);
    public static final Color COLOR_TEXT_WHITE = Color.WHITE;
    public static final Color COLOR_STATUS_READY = new Color(34, 139, 34);
    public static final Color COLOR_STATUS_ERROR = new Color(220, 20, 60);
    public static final Color COLOR_STATUS_LOADING = new Color(70, 130, 180);
    public static final Color COLOR_STATUS_BG = new Color(240, 240, 240);
    
    public static JTextArea textArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(COLOR_BG_LIGHT);
        textArea.setForeground(Color.BLACK);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);
        return textArea;
    }
    
    public static JScrollPane scrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollPane.setBackground(COLOR_BG_LIGHT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
    
    public static JButton executeButton() {
        JButton button = new JButton("▶  Execute");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(COLOR_TEXT_WHITE);
        button.setBackground(COLOR_BUTTON_BG);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(8, 15, 8, 15));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_BUTTON_BG);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 100, 180));
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
        });
        return button;
    }
    
    public static JLabel statusLabel(String initialText) {
        JLabel label = new JLabel("● " + initialText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_STATUS_READY);
        label.setBackground(new Color(245, 248, 250));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return label;
    }
    
    // state: ready/error/loading
    public static void setStatusColor(JLabel label, String state) {
        switch(state.toLowerCase()) {
            case "ready":
                label.setText("● Ready");
                label.setForeground(COLOR_STATUS_READY);
                break;
            case "error":
                label.setText("● Error");
                label.setForeground(COLOR_STATUS_ERROR);
                break;
            case "loading":
                label.setText("⟳ Executing...");
                label.setForeground(COLOR_STATUS_LOADING);
                break;
            default:
                label.setForeground(Color.BLACK);
        }
    }
    
    public static JTextArea textAreaWithPlaceholder(String placeholder) {
        JTextArea textArea = new PlaceholderTextArea(placeholder);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(COLOR_BG_LIGHT);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);
        return textArea;
    }
}
