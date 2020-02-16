package crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("URL");
        model.addColumn("Title");
        table = new JTable(model);
        table.setName("TitlesTable");
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private ActionListener getCrawlerListener() {
        return event -> {
            try {
                String link = urlTextField.getText();
                URL homeUrl = getUrlFromLink(link);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
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
            String contentType = url.openConnection().getContentType();
            if ("text/html".equals(contentType)) {
                return url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}