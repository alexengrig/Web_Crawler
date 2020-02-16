package crawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WebCrawler extends JFrame {
    private final static String LINE_SEPARATOR = System.lineSeparator();
    private JTextField urlTextField;
    private JTextArea htmlTextArea;
    private JLabel titleLabel;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setTitle("Web Crawler");
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel urlLabel = new JLabel("URL: ");
        topPanel.add(urlLabel, BorderLayout.WEST);

        urlTextField = new JTextField();
        urlTextField.setName("UrlTextField");
        topPanel.add(urlTextField, BorderLayout.CENTER);

        JButton runButton = new JButton("Get text!");
        runButton.setName("RunButton");
        runButton.addActionListener(getCrawlerListener());
        topPanel.add(runButton, BorderLayout.EAST);

        titleLabel = new JLabel("Title: ");
        titleLabel.setName("TitleLabel");
        topPanel.add(titleLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        htmlTextArea = new JTextArea();
        htmlTextArea.setName("HtmlTextArea");
        htmlTextArea.setEnabled(false);
        add(new JScrollPane(htmlTextArea), BorderLayout.CENTER);
    }

    private ActionListener getCrawlerListener() {
        return event -> {
            try {
                final String html = getHtml(urlTextField.getText());
                htmlTextArea.setText(html);
                final String beginTitle = "<title>";
                final int indexOfBeginTitle = html.indexOf(beginTitle);
                if (indexOfBeginTitle != -1) {
                    final int indexOfEndTitle = html.indexOf("</title>");
                    if (indexOfEndTitle != -1) {
                        final String title = html.substring(indexOfBeginTitle + beginTitle.length(), indexOfEndTitle);
                        titleLabel.setText(title);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private String getHtml(String url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), UTF_8))) {
            final StringBuilder stringBuilder = new StringBuilder();
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                stringBuilder.append(nextLine);
                stringBuilder.append(LINE_SEPARATOR);
            }
            return stringBuilder.toString();
        }
    }
}