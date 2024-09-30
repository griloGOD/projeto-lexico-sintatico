package sintatico;

import lexico.Classe;
import lexico.Lexico;
import lexico.Token;

public class Sintatico{

    private Lexico lexico;
    private String aquivoLeitura;
    private Token token;

    public Sintatico(String arquivo){
        this.aquivoLeitura = arquivo;
        lexico = new Lexico(arquivo);
    }

    public void analisar(){
        token = lexico.nextToken();
        programa();
    }

    //<programa> ::= program id {A01} ; <corpo> • {A45}
public void programa(){
    if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("program")) {
        token = lexico.nextToken();
        if (token.getClasse() == Classe.indentificador) {
            token = lexico.nextToken();
            // {A01} -> Ação associada à declaração do programa, caso seja necessário
            if (token.getClasse() == Classe.pontoEVirgula) {
                token = lexico.nextToken();
                corpo();
                if (token.getClasse() == Classe.ponto) {
                    token = lexico.nextToken();
                    // {A45} -> Ação associada ao final do programa, caso seja necessário
                    if (token.getClasse() != Classe.EOF) {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: o programa deve terminar logo após o ponto final (EOF esperado).");
                    }
                } else{
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto final no programa (.)");
                }
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
            }
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou o nome do programa");
        }
    } else{
        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou começar o programa com PROGRAM");
    }
}

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    public void corpo(){
        declara();  // Declarações de variáveis
        rotina();   // Declarações de rotinas (procedimentos ou funções)
        // {A44} -> Ação associada ao corpo do programa
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
            token = lexico.nextToken();
            sentencas();  // Processa sentenças entre begin e end
            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")) {
                token = lexico.nextToken();
                // {A46} -> Ação associada ao final do bloco "begin...end"
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou terminar o corpo do programa com END");
            }
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou começar o corpo do programa com BEGIN");
        }
    }
    

    //<declara> ::= var <dvar> <mais_dc> | ε
    public void declara(){
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("var")){
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }
    
    //<mais_dc> ::=  ; <cont_dc>
    public void mais_dc(){
        if (token.getClasse() == Classe.pontoEVirgula){
            token = lexico.nextToken();
            cont_dc();
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e virgula (;) no final de uma declaração de variáveis");
        }
    }

    //<cont_dc> ::= <dvar> <mais_dc> | ε
    public void cont_dc(){
        if (token.getClasse() == Classe.indentificador){
            dvar();
            mais_dc();
        }
    }
    
    //<dvar> ::= <variaveis> : <tipo_var> {A02}
    public void dvar(){
        variaveis();
        if (token.getClasse() == Classe.doisPontos){
            token = lexico.nextToken();
            tipo_var();
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois pontos (:) no final do nome do programa");
        }
    }

    //<tipo_var> ::= integer
    public void tipo_var(){
        if (token.getClasse() == Classe.numeroInteiro) {
            token = lexico.nextToken();
            //{A02}
        }else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou um numero inteiro no programa");
        }
    }

    //<variaveis> ::= id {A03} <mais_var>
    public void variaveis(){
        if (token.getClasse() == Classe.indentificador) {
            token = lexico.nextToken();
            // {A03}
            mais_var();
        }else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou o nome do programa");
        } 
    }

    //<mais_var> ::=  ,  <variaveis> | ε
    public void mais_var(){
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            variaveis();
        } 
    }

    //<rotina> ::= <procedimento> | <funcao> | ε
    public void rotina(){
        if(token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("procedure")){
            procedimento();
        }
        if(token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("function")){
            funcao();
        }
    }
    //<procedimento> ::= procedure id {A04} <parametros> {A48}; <corpo> {A56} ; <rotina>
    public void procedimento(){
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("procedure")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.indentificador) {
                token = lexico.nextToken();
                // {A04}
                    parametros();
                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();
                        // {A48}
                        corpo();
                        //{A56}
                        if (token.getClasse() == Classe.pontoEVirgula){
                            token = lexico.nextToken();
                            rotina();
                        }else{
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
                        }
                    } else{
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto final no programa (.)");
                    }
                } else{
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
                }
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou po nome do programa");
            }  
    }

    public void funcao(){}
    public void parametros(){}
    public void sentencas(){}
}