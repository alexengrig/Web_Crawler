package crawler.util;

import crawler.domain.Page;

import java.util.concurrent.Callable;

public class PageCallable implements Callable<Page> {
    private final Page page;

    public PageCallable(Page page) {
        this.page = page;
    }

    @Override
    public Page call() {
        return page;
    }
}
