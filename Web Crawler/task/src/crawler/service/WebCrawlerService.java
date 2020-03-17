package crawler.service;

import crawler.domain.CrawledPage;
import crawler.domain.Page;
import crawler.domain.UrlAndTitle;
import crawler.util.WebCrawlerRunner;

import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WebCrawlerService {
    private final Consumer<UrlAndTitle> consumer;

    private int numberOfWorkers;
    private int depth;
    private boolean launched;
    private ThreadPoolExecutor executor;

    public WebCrawlerService(Consumer<UrlAndTitle> consumer) {
        this(consumer, -1, -1);
    }

    protected WebCrawlerService(Consumer<UrlAndTitle> consumer, int numberOfWorkers, int depth) {
        this.consumer = consumer;
        this.numberOfWorkers = numberOfWorkers;
        this.depth = depth;
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        if (launched) {
            throw new IllegalStateException("Launched.");
        }
        this.numberOfWorkers = numberOfWorkers;
    }

    public void setDepth(int depth) {
        if (launched) {
            throw new IllegalStateException("Launched.");
        }
        this.depth = depth;
    }

    public void start(URL url) {
        start(url, 10 * 60 * 1000);
    }

    public void start(URL url, long timeout) {
        if (launched) {
            throw new IllegalStateException("Launched.");
        }
        launched = true;
        initExecutor();
        executeNewTask(new Page(0, url));
    }

    private void initExecutor() {
        if (numberOfWorkers < 1) {
            executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        } else if (numberOfWorkers == 1) {
            executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        } else {
            executor = new ThreadPoolExecutor(numberOfWorkers, numberOfWorkers,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }
    }

    private void executeNewTask(Page page) {
        executor.execute(new WebCrawlerRunner(page, this::processTaskResult));
    }

    private void processTaskResult(CrawledPage page) {
        final int nextDepth = page.depth + 1;
        if (depth < 1 || depth >= nextDepth) {
            for (URL url : page.urls) {
                executeNewTask(new Page(nextDepth, url));
            }
        }
        consumer.accept(new UrlAndTitle(page.url.toString(), page.title));
        System.out.println("Page: " + page);
    }

    public void stop() {
        if (!launched) {
            throw new IllegalStateException("Not running.");
        }
        executor.shutdownNow();
    }
}
