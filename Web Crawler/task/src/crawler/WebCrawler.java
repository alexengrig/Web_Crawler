package crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WebCrawler extends JFrame {
    private final static String LINE_SEPARATOR = System.lineSeparator();
    private JTextField urlTextField;
    private JTable table;
    private JLabel titleLabel;
    private JTextField exportTextField;

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

        JButton runButton = new JButton("Parse");
        runButton.setName("RunButton");
        runButton.addActionListener(getCrawlerListener());
        topPanel.add(runButton, BorderLayout.EAST);

        titleLabel = new JLabel("Title: ");
        titleLabel.setName("TitleLabel");
        topPanel.add(titleLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("URL");
        model.addColumn("Title");
        table = new JTable(model);
        table.setName("TitlesTable");
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JLabel exportLabel = new JLabel("Export: ");
        bottomPanel.add(exportLabel, BorderLayout.WEST);

        exportTextField = new JTextField();
        exportTextField.setName("ExportUrlTextField");
        bottomPanel.add(exportTextField, BorderLayout.CENTER);

        JButton exportButton = new JButton("Save");
        exportButton.setName("ExportButton");
        exportButton.addActionListener(getExportListener());
        bottomPanel.add(exportButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private ActionListener getCrawlerListener() {
        return event -> {
            try {
                String link = urlTextField.getText();
                URL homeUrl = getUrlFromLink(link);
                if (homeUrl == null) {
                    return;
                }
                final String homeHtml = getHtml(homeUrl);
                final String homeTitle = getTitle(homeHtml);
                titleLabel.setText(homeTitle);
                final List<URL> urls = getUrls(homeUrl, homeHtml);
                final DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                for (URL url : urls) {
                    final String html = getHtml(url);
                    final String title = getTitle(html);
                    model.addRow(new Object[]{url, title});
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private URL getUrlFromLink(String link) {
        try {
            return new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getHtml(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), UTF_8))) {
            final StringBuilder stringBuilder = new StringBuilder();
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                stringBuilder.append(nextLine);
                stringBuilder.append(LINE_SEPARATOR);
            }
            return stringBuilder.toString();
        }
    }

    private String getTitle(String html) {
        final String beginTitle = "<title>";
        final int indexOfBeginTitle = html.indexOf(beginTitle);
        if (indexOfBeginTitle != -1) {
            final int indexOfEndTitle = html.indexOf("</title>");
            if (indexOfEndTitle != -1) {
                return html.substring(indexOfBeginTitle + beginTitle.length(), indexOfEndTitle);
            }
        }
        return null;
    }

    private List<URL> getUrls(URL context, String html) {
        final List<URL> urls = new ArrayList<>();
        final Pattern pattern = Pattern.compile("<a.*>");
        final Matcher matcher = pattern.matcher(html);
        final String href = "href=";
        while (matcher.find()) {
            final String linkSelector = matcher.group();
            int beginIndex = linkSelector.indexOf(href);
            if (beginIndex != -1) {
                beginIndex += href.length() + 1;
                int endIndex = linkSelector.indexOf("\"", beginIndex);
                if (endIndex == -1) {
                    endIndex = linkSelector.indexOf("'", beginIndex);
                    if (endIndex == -1) {
                        continue;
                    }
                }
                final String link = linkSelector.substring(beginIndex, endIndex);
                final URL url = getUrl(context, link);
                if (url != null) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private URL getUrl(URL context, String link) {
        try {
            URL url = new URL(context, link);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            String contentType = urlConnection.getContentType();
            if ("text/html".equals(contentType)) {
                return url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ActionListener getExportListener() {
        return event -> {
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
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    final String url = model.getValueAt(i, 0).toString();
                    writer.write(url);
                    writer.write(LINE_SEPARATOR);
                    final String title = model.getValueAt(i, 1).toString();
                    writer.write(title);
                    writer.write(LINE_SEPARATOR);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}