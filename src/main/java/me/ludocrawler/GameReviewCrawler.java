package me.ludocrawler;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
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

                && (origem.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=")
                && href.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina="))

                || (origem.startsWith("https://www.ludopedia.com.br/search_jogo?advsearch=true&fl_tp_jogo=1&s=1&pagina=")
                && href.startsWith("https://www.ludopedia.com.br/jogo/"))

                || (origem.startsWith("https://www.ludopedia.com.br/jogo/")
                && href.contains("?v=avaliacoes"))

                || (origem.contains("?v=avaliacoes")
                && href.contains("?v=avaliacoes"));
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (url.contains("?v=avaliacoes")) {
            if (page.getParseData() instanceof HtmlParseData) {

                //identificar o nome do jogo
                String unfinishedName = url.substring(34);
                int nameEndIndex = unfinishedName.length();
                for (int i = 0; i < unfinishedName.length(); i++) {
                    char c = unfinishedName.charAt(i);
                    if (c == '?') {
                        nameEndIndex = i;
                        break;
                    }
                }

                String nomeJogo = unfinishedName.substring(0, nameEndIndex);
                String finalPath = storageFolder.getPath() + '/' + nomeJogo + ".json";

                //escrever
                try {
                    JSONObject reviewdata = new JSONObject();
                    JSONArray reviews = new JSONArray();
                    //the program must check if the reviewdata alredy exists in the file
                    File targetFile = new File(finalPath);
                    File parentDirectory = targetFile.getParentFile();
                    if (!parentDirectory.exists()) {parentDirectory.mkdirs();}
                    if (targetFile.exists()) {
                        JSONParser jsonParser = new JSONParser();
                        InputStreamReader fileReader =
                                new InputStreamReader(new FileInputStream(targetFile), "utf-8");
                        JSONObject oldData = (JSONObject) jsonParser.parse(fileReader);
                        JSONArray oldDataReviewsArray = (JSONArray) oldData.get("reviews");
                        for (Object o : oldDataReviewsArray) {
                            reviews.add(o);
                        }
                        //reviews.add(oldData.get("reviews"));
                    }
                    else {
                        targetFile.createNewFile();
                    }

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

                        reviews.add(temp);
                    }
                    reviewdata.put("reviews", reviews);
                    reviewdata.put("name", nomeJogo);
                    System.out.println(reviewdata.toString());
                /*
                FileWriter writer = new FileWriter("finalPath");
                writer.write(game_data.toString());
                */

                    //write in utf-8
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(finalPath), StandardCharsets.UTF_8);
                    writer.write(reviewdata.toString());
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