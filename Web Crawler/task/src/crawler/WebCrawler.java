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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebCrawler extends JFrame {
    private final static String LINE_SEPARATOR = System.lineSeparator();

    private final ExecutorService backgroundExecutor;
    private final Timer timer;
    private TimerTask timerTask;
    private WebCrawlerService crawlerService;
    private ConcurrentLinkedQueue<UrlAndTitle> urlAndTitles;
    private JTextField urlTextField;
    private JTextField exportTextField;
    private JTextField workersTextField;
    private JTextField depthTextField;
    private JCheckBox depthCheckBox;
    private JTextField timeTextField;
    private JCheckBox timeCheckBox;
    private JLabel timeLabel;
    private JLabel parseLabel;
    private JToggleButton runButton;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setTitle("Web Crawler");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);
        backgroundExecutor = Executors.newCachedThreadPool();
        timer = new Timer();
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

        timeTextField = new JTextField();
        timeTextField.setName("TimeTextField");

        timeCheckBox = new JCheckBox("Enabled");
        timeCheckBox.setName("TimeCheckBox");

        add(createPanel(new JLabel("Time limit: "), timeTextField, timeCheckBox));

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
                doCrawl();
            } else if (ItemEvent.DESELECTED == state) {
                doStop();
            }
        };
    }

    private void doCrawl() {
        runButton.setText("Stop");
        parseLabel.setText("0");
        timeLabel.setText("0:00");
        urlAndTitles = new ConcurrentLinkedQueue<>();
        crawlerService = new WebCrawlerService(this::addResult);
        crawlerService.setNumberOfWorkers(getNumber(workersTextField.getText()));
//        if (depthCheckBox.isSelected()) { // Wrong answer in test #17  File your app saves has a wrong lines number
        crawlerService.setDepth(getNumber(depthTextField.getText()));
//        }
        final boolean hasTimeout = timeCheckBox.isSelected();
        final long timeout = getNumber(timeTextField.getText());
        backgroundExecutor.execute((() -> {
            if (hasTimeout && timeout > 0) {
                crawlerService.start(getUrlFromLink(urlTextField.getText()), timeout);
            } else {
                crawlerService.start(getUrlFromLink(urlTextField.getText()));
            }
            timerTask.cancel();
            SwingUtilities.invokeLater(() -> {
                runButton.setText("Run");
                runButton.setSelected(false);
            });
        }));
        timerTask = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    final String colon = ":";
                    final String time = timeLabel.getText();
                    final int minutes = Integer.parseInt(time.substring(0, time.indexOf(colon)));
                    final int seconds = Integer.parseInt(time.substring(time.indexOf(colon) + 1));
                    final Duration duration = Duration.ofMinutes(minutes).plusSeconds(seconds + 1);
                    final String nextMinutes = Long.toString(duration.toMinutes());
                    final int nextSecondsPart = duration.toSecondsPart();
                    final String nextSeconds = (nextSecondsPart > 9 ? "" : "0") + nextSecondsPart;
                    timeLabel.setText(nextMinutes + colon + nextSeconds);
                });
            }
        };
        backgroundExecutor.execute(() -> timer.schedule(timerTask, 0, 1000));
    }

    private void addResult(UrlAndTitle urlAndTitle) {
        urlAndTitles.add(urlAndTitle);
        SwingUtilities.invokeLater(() -> parseLabel.setText(String.valueOf(getNumber(parseLabel.getText()) + 1)));
    }

    private void doStop() {
        runButton.setText("Run");
        if (crawlerService != null) {
            crawlerService.stop();
            crawlerService = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
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
                if (!urlAndTitles.isEmpty()) {
                    UrlAndTitle urlAndTitle = urlAndTitles.get(0);
                    writer.write(urlAndTitle.url);
                    writer.write(LINE_SEPARATOR);
                    writer.write(urlAndTitle.title);
                    for (int i = 1; i < urlAndTitles.size(); i++) {
                        urlAndTitle = urlAndTitles.get(i);
                        writer.write(LINE_SEPARATOR);
                        writer.write(urlAndTitle.url);
                        writer.write(LINE_SEPARATOR);
                        writer.write(urlAndTitle.title);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}