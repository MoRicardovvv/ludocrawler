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

public class ProfileCrawler extends WebCrawler {

    private final File storageFolder;  //aonde serao salvos os dados
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    public ProfileCrawler(File storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String origem = referringPage.getWebURL().getURL().toLowerCase();
        //so visitar paginas a partir do advanced search pra evitar expansoes
        return !FILTERS.matcher(href).matches()

                && (origem.startsWith("https://www.ludopedia.com.br/search_jogo?pagina=")
                && href.startsWith("https://https://www.ludopedia.com.br/search_jogo?pagina="))

                || (origem.startsWith("https://www.ludopedia.com.br/search_jogo?pagina=")
                && href.startsWith("https://www.ludopedia.com.br/jogo/"))

                || (origem.startsWith("https://www.ludopedia.com.br/jogo/")
                && href.contains("?v=avaliacoes"))

                || (origem.contains("?v=avaliacoes")
                && href.contains("?v=avaliacoes"))

                || (origem.contains("?=avaliacoes")
                && href.startsWith("https://www.ludopedia.com.br/usuario/"))

                || (origem.startsWith("https://www.ludopedia.com.br/usuario/")
                && href.contains("&lista=notas"))

                || (origem.endsWith("&listas=notas")
                && href.equals(origem));
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        if (url.contains("&lista=notas")) {
            if (page.getParseData().getOutgoingUrls().toString().contains("&lista=notas")) {
                if (page.getParseData() instanceof HtmlParseData) {
                    //identificar o nome do usuario
                    // exemplo de url: https://www.ludopedia.com.br/colecao?usuario=Ricardo%20Gama&lista=notas
                    String unfinishedName = url.substring(45);
                    int nameEndIndex = unfinishedName.length();
                    for (int i = 0; i < unfinishedName.length(); i++) {
                        char c = unfinishedName.charAt(i);
                        if (c == '&') {
                            nameEndIndex = i;
                            break;
                        }
                    }
                    String nomePerfil = unfinishedName.substring(0, nameEndIndex);
                    nomePerfil = nomePerfil.replaceAll("%20", "-");
                    String finalPath = storageFolder.getPath() + '/' + nomePerfil + ".json";

                    //escrever
                    try {
                        JSONObject profileData = new JSONObject();
                        JSONArray scoreArray = new JSONArray();

                        File targetFile = new File(finalPath);
                        File parentDirectory = targetFile.getParentFile();
                        if (!parentDirectory.exists()) {
                            parentDirectory.mkdirs();
                        }
                        if (!targetFile.exists()) {
                            targetFile.createNewFile();
                        }

                        //usar jsoup pra parsar o html
                        Document listaNotas = Jsoup.connect(url).get();

                        Elements avalicao = listaNotas.getElementsByClass("col-xs-6 col-md-3");
                        String game, score;
                        for (Element a : avalicao) {
                            game = a.getElementsByClass("jogo-title").text();
                            score = a.select("label[title]").text();


                            JSONObject temp = new JSONObject();
                            temp.put("game", game);
                            temp.put("score", score);

                            scoreArray.add(temp);
                        }
                        profileData.put("scores", scoreArray);
                        profileData.put("name", nomePerfil);
                        System.out.println(profileData.toString());
                /*
                FileWriter writer = new FileWriter("finalPath");
                writer.write(game_data.toString());
                */

                        //write in utf-8
                        OutputStreamWriter writer =
                                new OutputStreamWriter(new FileOutputStream(finalPath), StandardCharsets.UTF_8);
                        writer.write(profileData.toString());
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
}