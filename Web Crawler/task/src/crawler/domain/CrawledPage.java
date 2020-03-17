package crawler.domain;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class CrawledPage extends Page {
    public final String title;
    public final List<URL> urls;

    public CrawledPage(Page page, String title, List<URL> urls) {
        super(page.depth, page.url);
        this.title = title;
        this.urls = urls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawledPage that = (CrawledPage) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(urls, that.urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, urls);
    }

    @Override
    public String toString() {
        return "CrawledPage{" +
                "depth='" + depth + '\'' +
                ", title='" + title + '\'' +
                ", url=" + url +
                ", urls=" + urls +
                '}';
    }
}
