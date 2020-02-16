package crawler;

import javax.swing.*;

public class WebCrawler extends JFrame {
    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setTitle("Simple Window");
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JTextArea textArea = new JTextArea("HTML code?");
        textArea.setName("TextArea");
        textArea.setEnabled(false);
        add(textArea);
    }
}