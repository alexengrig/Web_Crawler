package crawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebCrawler extends JFrame {
    private final static String LINE_SEPARATOR = System.lineSeparator();

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

        JTextArea htmlTextArea = new JTextArea();
        htmlTextArea.setName("HtmlTextArea");
        htmlTextArea.setEnabled(false);
        add(new JScrollPane(htmlTextArea), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());

        JTextField urlTextField = new JTextField();
        urlTextField.setName("UrlTextField");
        topPanel.add(urlTextField, BorderLayout.CENTER);

        JButton runButton = new JButton("Get text!");
        runButton.setName("RunButton");
        runButton.addActionListener(getCrawlerListener(urlTextField::getText, htmlTextArea::setText));
        topPanel.add(runButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private ActionListener getCrawlerListener(Supplier<String> urlProducer, Consumer<String> htmlConsumer) {
        return event -> {
            final String url = urlProducer.get();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8))) {
                final StringBuilder stringBuilder = new StringBuilder();
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                    stringBuilder.append(LINE_SEPARATOR);
                }
                final String html = stringBuilder.toString();
                htmlConsumer.accept(html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}