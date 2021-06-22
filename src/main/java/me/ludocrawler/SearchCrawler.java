package me.ludocrawler;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchCrawler extends WebCrawler {

    private final File storageFolder;  //aonde serao salvos os dados

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    public SearchCrawler(File storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String origem = referringPage.getWebURL().getURL().toLowerCase();
        //so visitar paginas a partir do advanced search pra evitar expansoes
        return !FILTERS.matcher(href).matches()
                && origem.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=")
                && (href.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=")
                || href.startsWith("https://www.ludopedia.com.br/jogo/"));
    }


    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());

            // se o url for de jogos salvar os dados
            if (url.startsWith("https://www.ludopedia.com.br/jogo/")) {
                //salvar a imagem em ordem alfabetica e com nome = nome do jogo.
                String nomeJogo = url.substring(34);
                char inicial = nomeJogo.charAt(0);
                //se o nome do jogo nao comecar com uma letra
                String finalPath;
                if (Character.isLetter(inicial)) {finalPath = "C:/Users/ricar/ludocrawler/data/" + inicial + "/" + nomeJogo + ".html";}
                else {finalPath = "C:/Users/ricar/ludocrawler/data/123" + "/" + nomeJogo + ".html";}
                //escrever
                try {
                    //create directory/file if not exists
                    File targetFile = new File(finalPath);
                    File parentDirectory = targetFile.getParentFile();
                    if (!parentDirectory.exists()) {parentDirectory.mkdirs();}
                    if (!targetFile.exists()) {targetFile.createNewFile();}

                    //write in utf-8
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(finalPath), StandardCharsets.UTF_8);
                    writer.write(html);
                    //Files.write(html.getBytes(), new File(finalPath));
                    WebCrawler.logger.info("Stored: {}", url);
                    System.out.println("Stored: " + url);
                } catch (IOException iox) {
                    WebCrawler.logger.error("Failed to write file: {}", finalPath, iox);
                }

            }
        }
    }


}
