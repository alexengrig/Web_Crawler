package crawler.util;

import crawler.domain.CrawledPage;
import crawler.domain.Page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WebCrawlerRunner implements Runnable {
    private final Page page;
    private final Consumer<CrawledPage> consumer;

    public WebCrawlerRunner(Page page, Consumer<CrawledPage> consumer) {
        this.page = page;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try {
            final String html = getHtml(page.url);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            final String title = getTitle(html);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            final List<URL> urls = getUrls(page.url, html);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            final CrawledPage crawledPage = new CrawledPage(page, title, urls);
            consumer.accept(crawledPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                stringBuilder.append(System.lineSeparator());
            }
            return stringBuilder.toString();
        }
    }

    private String getTitle(String html) {
        try {
            final String beginTitle = "<title>";
            final int indexOfBeginTitle = html.indexOf(beginTitle);
            if (indexOfBeginTitle != -1) {
                final int indexOfEndTitle = html.indexOf("</title>");
                if (indexOfEndTitle != -1) {
                    return html.substring(indexOfBeginTitle + beginTitle.length(), indexOfEndTitle);
                }
            }
        } catch (Exception e) {

        }
        return "";
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
}
