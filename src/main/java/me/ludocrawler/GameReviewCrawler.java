package me.ludocrawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class GameReviewCrawler extends WebCrawler {

    private final File storageFolder;  //aonde serao salvos os dados
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    public GameReviewCrawler(File storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String origem = referringPage.getWebURL().getURL().toLowerCase();
        //so visitar paginas a partir do advanced search pra evitar expansoes
        return !FILTERS.matcher(href).matches()
                && origem.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=")
                && origem.startsWith("https://www.ludopedia.com.br/jogo/")
                && (href.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=") ||
                    href.contains("?v=avaliacoes&pagina=") ||
                    href.startsWith("https://www.ludopedia.com.br/jogo/")
                    );
    }


    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (url.startsWith(jogoPgInicial +"?v=avaliacoes")) {
            if (page.getParseData() instanceof HtmlParseData) {

                //identificar o nome do jogo
                String nomeJogo = jogoPgInicial.substring(34);
                char inicial = nomeJogo.charAt(0);

                String finalPath;
                if (Character.isLetter(inicial)) {
                    finalPath = "C:/Users/ricar/ludocrawler/data/" + inicial + "/" + nomeJogo + ".json";
                } else {
                    finalPath = "C:/Users/ricar/ludocrawler/data/123" + "/" + nomeJogo + ".json";
                }
                //escrever
                try {
                    //create directory/file if not exists
                    File targetFile = new File(finalPath);
                    File parentDirectory = targetFile.getParentFile();
                    if (!parentDirectory.exists()) {
                        parentDirectory.mkdirs();
                    }
                    if (!targetFile.exists()) {
                        targetFile.createNewFile();
                    }

                    JSONObject data = new JSONObject();
                    JSONArray reviewData = new JSONArray();


                    //usar jsoup pra parsar o html
                    Document avaliacoes = Jsoup.connect(url).get();

                    Elements avalicao = avaliacoes.getElementsByClass("speech");
                    String score, user, comment;
                    for (Element a : avalicao) {
                        user = a.getElementsByClass("media-heading").text();
                        comment = a.getElementsByClass("media-body").text();
                        score = a.getElementsByClass("pull-right nota-comentario-header hidden-sm hidden-md hidden-lg").text();


                        JSONObject temp = new JSONObject();
                        temp.put("user", user);
                        temp.put("comment", comment);
                        temp.put("score", score);

                        reviewData.put(temp);
                    }
                    data.put("name", nomeJogo);
                    data.put("reviews", reviewData);


                    System.out.println(data.toString());
                    /*
                    FileWriter writer = new FileWriter("finalPath");
                    writer.write(game_data.toString());
                    */


                    //write in utf-8
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(finalPath), StandardCharsets.UTF_8);
                    writer.write(data.toString());
                    writer.close();
                    //Files.write(html.getBytes(), new File(finalPath));
                    WebCrawler.logger.info("Stored: {}", url);
                    System.out.println("Stored: " + url);

                } catch (Exception iox) {
                    WebCrawler.logger.error("Failed to write file: {}", finalPath, iox);
                }

            }
        }

    }
}