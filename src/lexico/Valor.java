package lexico;

public class Valor {
    private int inteiro;
    private String texto;

    public Valor(int inteiro){
        this.inteiro = inteiro;
    }

    public Valor(String texto){
        this.texto = texto;
    }

    public int getInteiro() {
        return inteiro;
    }

    public void setInteiro(int inteiro) {
        this.inteiro = inteiro;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        
        return texto!=null? "texto=" + texto : "inteiro=" + inteiro;
    }


}
