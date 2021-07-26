package me.ludocrawler;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GameDataStructures {
    public class ReviewData {
        private String user;
        private int score;
        private String comment;

        public ReviewData(String user, int score, String Comment) {
            this.user = user;
            this.score = score;
            this.comment = comment;
        }
    }

    public class GameData {
        private String name;
        private String descricao;
        private String dependenciaIdioma;
        private Set<Integer> editoras;
        private Set<Integer> dominios;
        private Set<Integer> mecanicas;
        private Set<Integer> categorias;
        private Set<Integer> temas;
        private ReviewData[] reviewData;

        public GameData() {}

    }
}