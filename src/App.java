import lexico.Classe;
import lexico.Lexico;
import lexico.TabelaSimbolos;
import lexico.Token;
import sintatico.Sintatico;

public class App {
    public static void main(String[] args) throws Exception {

        Sintatico sintatico = new Sintatico("programaPascal.pas");

        sintatico.analisar();

        /*Lexico lexico = new Lexico("programaPascal.pas");
        Token token;
        do{
            token = lexico.nextToken();
            System.out.println(token);
        }while(token.getClasse() != Classe.EOF);

        System.out.println("-----------------------------------");
        TabelaSimbolos tabelaSimbolos = lexico.getTabelaSimbolos();
        System.out.println(tabelaSimbolos);*/
    }
}
