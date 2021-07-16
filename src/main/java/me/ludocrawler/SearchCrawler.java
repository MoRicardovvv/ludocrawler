package me.ludocrawler;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
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


            // se o url for de jogos salvar os dados
            if (url.startsWith("https://www.ludopedia.com.br/jogo/")) {
                //salvar a imagem em ordem alfabetica e com nome = nome do jogo.
                String nomeJogo = url.substring(34);
                char inicial = nomeJogo.charAt(0);
                //arquivo aonde será salvo o jogo
                String finalPath = storageFolder.getPath() + "/" + nomeJogo + ".json";

                //escrever
                try {
                    //create directory/file if not exists
                    File targetFile = new File(finalPath);
                    File parentDirectory = targetFile.getParentFile();
                    if (!parentDirectory.exists()) {parentDirectory.mkdirs();}
                    if (!targetFile.exists()) {targetFile.createNewFile();}


                    JSONObject gameData = new JSONObject();
                    JSONArray reviewData = new JSONArray();
                    JSONArray mechanicsData = new JSONArray();
                    JSONArray domainsData = new JSONArray();
                    JSONArray categoriesData = new JSONArray();
                    JSONArray themesData = new JSONArray();


                    //usar jsoup pra parsar o html
                    Document jogo = Jsoup.connect(url).get();
                    Document avaliacoes = Jsoup.connect(url +"?v=avaliacoes").get();

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
                    gameData.put("reviews", reviewData);

                    /*Elements attributes = jogo.getElementsByClass("mar-btm bg-gray-light pad-all");
                    for (Element e : attributes) {

                    }*/


                    String description = jogo.getElementById("bloco-descricao-sm").text();
                    gameData.put("description", description);


                    System.out.println(gameData.toString());
                    /*
                    FileWriter writer = new FileWriter("finalPath");
                    writer.write(game_data.toString());
                    */


                    //write in utf-8
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(finalPath), StandardCharsets.UTF_8);
                    writer.write(gameData.toString());
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
