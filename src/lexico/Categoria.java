package lexico;

public enum Categoria {
    
    FUNCAO("Funcao"),
    PROCEDIMENTO("Procedimento"),
    PROGRAMA_PRINCIPAL("Programa Principal"),
    VARIAVEL("Variavel"),
    PARAMENTRO("Parametro"),
    TIPO("Tipo"),
    INDEFINIDA("Indefinida");
    
    private String descricao;
    
    private Categoria(String descricao){
        this.descricao = descricao;
    }
    
    public String getDescricao(){
        return descricao;
    }
}
