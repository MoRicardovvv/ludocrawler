#ludocrawler 
algumas inspirações:
- [esse guia](https://www.devmedia.com.br/desenvolvendo-um-crawler-com-crawler4j/32893)
- [os guias do crawler4j](https://github.com/yasserg/crawler4j)
- [o Ludopédia](https://www.ludopedia.com.br/)

## sobre esse programa:
um crawler que navega que percorre o site/comunidade/forúm Ludopédia e extrai dados dos jogos

no momento, ele salva a página dos jogos integralmente como html, separados por ordem alfabética

No futuro, salvara apenas as informações relevantes em json, pois seu objetivo é ser usado como base de dados para um sistema de jogos tabletop para grupos


### Porque usar a pesquisa avançada como seed para o crawler?
A seed escolhide permite caminhar apenas pelos jogos base e filtrar expansões/acessórios 

### Tenha certeza que o diretório "data" está no *root* do ambiente. Ao contrário, resultará em erro.
