package crawler;

import crawler.domain.UrlAndTitle;
import crawler.service.WebCrawlerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class WebCrawler extends JFrame {
    private final static String LINE_SEPARATOR = System.lineSeparator();
    private JTextField urlTextField;
    private JTextField exportTextField;
    private JTextField workersTextField;
    private JTextField depthTextField;
    private JCheckBox depthCheckBox;
    private JLabel timeLabel;
    private JLabel parseLabel;
    private JToggleButton runButton;

    private WebCrawlerService crawlerService;
    private ConcurrentLinkedQueue<UrlAndTitle> urlAndTitles;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setTitle("Web Crawler");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        urlTextField = new JTextField();
        urlTextField.setName("UrlTextField");

        runButton = new JToggleButton("Run");
        runButton.setName("RunButton");
        runButton.addItemListener(getItemListener());

        add(createPanel(new JLabel("Start URL: "), urlTextField, runButton));

        workersTextField = new JTextField();
        workersTextField.setName("WorkersTextField");

        add(createPanel(new JLabel("Workers: "), workersTextField, null));

        depthTextField = new JTextField();
        depthTextField.setName("DepthTextField");

        depthCheckBox = new JCheckBox("Enabled");
        depthCheckBox.setName("DepthCheckBox");

        add(createPanel(new JLabel("Maximum depth: "), depthTextField, depthCheckBox));

        timeLabel = new JLabel("0:00");

        add(createPanel(new JLabel("Elapsed time: "), timeLabel, null));

        parseLabel = new JLabel("0");
        parseLabel.setName("ParsedLabel");

        add(createPanel(new JLabel("Parsed pages: "), parseLabel, null));

        exportTextField = new JTextField();
        exportTextField.setName("ExportUrlTextField");

        JButton exportButton = new JButton("Save");
        exportButton.setName("ExportButton");
        exportButton.addActionListener(getExportListener());

        add(createPanel(new JLabel("Export: "), exportTextField, exportButton));
    }

    private JPanel createPanel(JComponent west, JComponent center, JComponent east) {
        final JPanel panel = new JPanel(new BorderLayout());
        if (west != null) {
            panel.add(west, BorderLayout.WEST);
        }
        if (center != null) {
            panel.add(center, BorderLayout.CENTER);
        }
        if (east != null) {
            panel.add(east, BorderLayout.EAST);
        }
        return panel;
    }

    private ItemListener getItemListener() {
        return event -> {
            final int state = event.getStateChange();
            if (ItemEvent.SELECTED == state) {
                runButton.setText("Stop");
                urlAndTitles = new ConcurrentLinkedQueue<>();
                parseLabel.setText("0");
                timeLabel.setText("0:00");
                crawlerService = new WebCrawlerService(urlAndTitle -> {
                    urlAndTitles.add(urlAndTitle);
                    parseLabel.setText(String.valueOf(getNumber(parseLabel.getText()) + 1));
                });
                crawlerService.setNumberOfWorkers(getNumber(workersTextField.getText()));
                if (depthCheckBox.isSelected()) {
                    crawlerService.setDepth(getNumber(depthTextField.getText()));
                }
                final Timer timer = new Timer(1000, e -> {
                    System.out.println(LocalTime.now());
                });
                timer.start();
                SwingUtilities.invokeLater(() -> {
                    crawlerService.start(getUrlFromLink(urlTextField.getText()));
                    timer.stop();
                });
            } else if (ItemEvent.DESELECTED == state) {
                runButton.setText("Run");
                if (crawlerService != null) {
                    crawlerService.stop();
                    crawlerService = null;
                }
            }
        };
    }

    private int getNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ignore) {
            return -1;
        }
    }

    private URL getUrlFromLink(String link) {
        try {
            return new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ActionListener getExportListener() {
        return event -> {
            if (this.urlAndTitles == null) {
                return;
            }
            final List<UrlAndTitle> urlAndTitles = new ArrayList<>(this.urlAndTitles);
            final String path = exportTextField.getText();
            if (path.isEmpty()) {
                return;
            }
            final File file = new File(path);
            if (!file.exists()) {
                final File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        new IOException("Failed to create directories: " + parent).printStackTrace();
                        return;
                    }
                }
                try {
                    if (!file.createNewFile()) {
                        new IOException("Failed to create file: " + file).printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            try (FileWriter writer = new FileWriter(file)) {
                for (UrlAndTitle urlAndTitle : urlAndTitles) {
                    writer.write(urlAndTitle.url);
                    writer.write(LINE_SEPARATOR);
                    writer.write(urlAndTitle.title);
                    writer.write(LINE_SEPARATOR);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}