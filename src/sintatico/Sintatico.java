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
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(programa) Erro sintático: o programa deve terminar logo após o ponto final (EOF esperado).");
                    }
                } else{
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(programa) Erro sintático: faltou ponto final no programa (.)");
                }
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(programa) Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
            }
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(programa) Erro sintático: faltou o nome do programa");
        }
    } else{
        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(programa) Erro sintático: faltou começar o programa com PROGRAM");
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
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(corpo) Erro sintático: faltou terminar o corpo do programa com END");
            }
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(corpo) Erro sintático: faltou começar o corpo do programa com BEGIN");
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
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(mais_dc) Erro sintático: faltou ponto e virgula (;) no final de uma declaração de variáveis");
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
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(dvar) Erro sintático: faltou dois pontos (:) após as variáveis");
        }
    }

    //<tipo_var> ::= integer
    public void tipo_var(){
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();  // Avança para o próximo token
            // {A02}
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(tipo_var) Erro sintático: esperado 'integer' como tipo de variável");
        }
    }

    //<variaveis> ::= id {A03} <mais_var>
    public void variaveis(){
        if (token.getClasse() == Classe.indentificador) {
            token = lexico.nextToken();
            // {A03}
            mais_var();
        }else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(variaveis) Erro sintático: esperado identificador de variável");
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
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("procedure")){
            procedimento();
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("function")){
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
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(procedimento) Erro sintático: faltou ponto e vírgula (;) após o corpo do procedimento");
                    }
                } else{
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(procedimento) Erro sintático: faltou ponto e vírgula (;) após os parâmetros do procedimento");
                }
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(procedimento) Erro sintático: faltou identificador do procedimento");
            }
        } else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(procedimento) Erro sintático: faltou começar o procedimento com a palavra reservada PROCEDURE");
        }  
    }

    // <funcao> ::= function id {A05} <parametros> {A48} : <tipo_funcao> {A47} ; <corpo> {A56} ; <rotina>
    public void funcao(){
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("function")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.indentificador) {
                token = lexico.nextToken();
                // {A05}
                parametros();
                if (token.getClasse() == Classe.doisPontos) {
                    token = lexico.nextToken();
                    tipo_funcao(); // Definimos o tipo da função
                    // {A47}
                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();
                        corpo();  // Processa o corpo da função
                        // {A56}
                        if (token.getClasse() == Classe.pontoEVirgula){
                            token = lexico.nextToken();
                            rotina();  // Chama rotina para funções encadeadas
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(funcao) Erro sintático: faltou ponto e vírgula ( ; ) após a função");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(funcao) Erro sintático: faltou ponto e vírgula ( ; ) após o cabeçalho da função");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(funcao) Erro sintático: faltou dois pontos ( : ) para o tipo de retorno da função");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(funcao) Erro sintático: faltou o nome da função");
            }
        }
    }

    //<parametros> ::= ( <lista_parametros> ) | ε
    public void parametros(){
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken();
            lista_parametros();
            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken();
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (parametros) Erro sintático: esperado fechamento do parênteses ')'");
            }
        }
    }
    

    // <lista_parametros> ::= <lista_id> : <tipo_var> {A06} <cont_lista_par>
    public void lista_parametros() {
        lista_id();
        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();
            tipo_var();  // Usa o tipo_var já definido
            // {A06} - Ação semântica pode ser inserida aqui
            cont_lista_par();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois pontos ( : ) após os identificadores de parâmetros");
        }
    }
 
    public void cont_lista_par(){}
    public void lista_id(){}
    
    // <tipo_funcao> ::= integer
    public void tipo_funcao() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();
            // {A47} - Ação semântica pode ser adicionada aqui
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: tipo de função inválido, esperado 'integer'");
        }
    }

    public void sentencas(){}
}