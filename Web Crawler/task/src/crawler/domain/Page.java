package crawler.domain;

import java.net.URL;
import java.util.Objects;

public class Page {
    public final int depth;
    public final URL url;

    public Page(int depth, URL url) {
        this.depth = depth;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return depth == page.depth &&
                Objects.equals(url, page.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, url);
    }

    @Override
    public String toString() {
        return "Page{" +
                "url=" + url +
                ", depth=" + depth +
                '}';
    }
}
