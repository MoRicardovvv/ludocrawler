package me.ludocrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.File;

public class ReviewsController {
    public static void main(String[] args) throws Exception {

        //aonde ficaram os dados dos jogos
        File storageFolder = new File("D:/recommender-data-master/game-reviews");
        //dados temporarios do crawler
        String crawlStorageFolder = "./reviews-storage";
        //numero de threads
        int numberOfCrawlers = 1; //threads

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        //desabilitar o robottxt se não não funciona
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=1");


        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<SearchCrawler> factory = () -> new SearchCrawler(storageFolder);

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
    }

}
