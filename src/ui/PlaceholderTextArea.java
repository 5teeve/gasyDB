package ui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;

public class PlaceholderTextArea extends JTextArea {
    private String placeholder;
    private boolean showingPlaceholder = false;
    private Color placeholderColor = new Color(150, 150, 150);
    private Color textColor = Color.BLACK;

    public PlaceholderTextArea(String placeholder) {
        this.placeholder = placeholder;
        setPlaceholder();
        
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    setText("");
                    setForeground(textColor);
                    showingPlaceholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setPlaceholder();
                }
            }
        });
    }

    private void setPlaceholder() {
        setText(placeholder);
        setForeground(placeholderColor);
        showingPlaceholder = true;
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }
}
