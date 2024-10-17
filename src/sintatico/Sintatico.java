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
            if (token.getClasse() == Classe.identificador) {
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
        if (token.getClasse() == Classe.identificador){
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
        if (token.getClasse() == Classe.identificador) {
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
            if (token.getClasse() == Classe.identificador) {
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
    public void funcao() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("function")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.identificador) {
                token = lexico.nextToken();
                // {A05}
                parametros(); // Pode ser ε
                if (token.getClasse() == Classe.doisPontos) {
                    token = lexico.nextToken();
                    tipo_funcao(); // Definimos o tipo da função
                    // {A47}
                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();
                        corpo(); // Processa o corpo da função
                        // {A56}
                        if (token.getClasse() == Classe.pontoEVirgula) {
                            token = lexico.nextToken();
                            rotina(); // Chama rotina para funções encadeadas
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

    // <parametros> ::= ( <lista_parametros> ) | ε
    public void parametros() {
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken();
            if (token.getClasse() != Classe.parentesesDireito) { // Verifica se não é ε
                lista_parametros();
            }
            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken();
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (parametros) Erro sintático: esperado fechamento do parênteses ')'");
            }
        }
    }

    // <lista_parametros> ::= <lista_id> : <tipo_var> {A06} <cont_lista_par>
    public void lista_parametros() {
        lista_id(); // Chama para processar os identificadores
        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();
            tipo_var(); // Usa o tipo_var já definido
            // {A06} - Ação semântica pode ser inserida aqui
            cont_lista_par(); // Chama para processar parâmetros adicionais
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois pontos ( : ) após os identificadores de parâmetros");
        }
    }

 
    //<cont_lista_par> ::= ; <lista_parametros> | ε
    public void cont_lista_par() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            lista_parametros();  // Processa uma nova lista de parâmetros
        }
        // ε (vazio) é tratado implicitamente, já que não há ação no caso do vazio.
    }
    
    //<lista_id> ::= id {A07} <cont_lista_id>
    public void lista_id() {
        if (token.getClasse() == Classe.identificador) {
            token = lexico.nextToken();
            // {A07} - Ação associada ao identificador (pode ser semântica)
            cont_lista_id();  // Processa o resto da lista
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "(lista_id) Erro sintático: esperado identificador na lista de parâmetros");
        }
    }
    
    // <cont_lista_id> ::= , <lista_id> | ε
    public void cont_lista_id() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            lista_id();
        }
    }

    // <tipo_funcao> ::= integer
    public void tipo_funcao() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();
            // {A47} - Ação semântica pode ser adicionada aqui
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: tipo de função inválido, esperado 'integer'");
        }
    }
    
    // <sentencas> ::= <comando> <mais_sentencas>
    public void sentencas() {
        comando();  // Processa o comando atual
        mais_sentencas();  // Processa mais comandos (se houver)
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    public void mais_sentencas() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();  // Avança para o próximo token
            cont_sentencas();  // Processa mais sentenças
        }
        // Se não houver ponto e vírgula, não há erro, pois a produção pode ser ε.
    }

    // <cont_sentencas> ::= <sentencas> | ε
    public void cont_sentencas() {
        // Permite continuar processando sentenças ou terminar
        if (token.getClasse() != Classe.pontoEVirgula) {
            sentencas(); // Continua processando sentenças, se houver
        }
        // Se o token for ponto e vírgula, apenas finaliza aqui (ε é implícito)
    }

    
 // <var_read> ::= id {A08} <mais_var_read>
public void var_read() {
    if (token.getClasse() == Classe.identificador) {
        token = lexico.nextToken();
        // {A08} - Ação associada à leitura da variável
        mais_var_read();
    } else {
        System.err.println(token.getLinha() + "," + token.getColuna() + " - (var_read) Erro sintático: esperado identificador de variável para leitura");
    }
}

    // <mais_var_read> ::= , <var_read> | ε
    public void mais_var_read() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            var_read();  // Chama var_read para processar a próxima variável
        }
    }

    // <exp_write> ::= id {A09} <mais_exp_write> | string {A59} <mais_exp_write> | intnum {A43} <mais_exp_write>
    public void exp_write() {
        if ((token.getClasse() == Classe.identificador)
                || (token.getClasse() == Classe.string)
                || (token.getClasse() == Classe.numeroInteiro)) {

            // <id> {A09} <mais_exp_write>
            if (token.getClasse() == Classe.identificador) {
                token = lexico.nextToken();
                // {A09}
                mais_exp_write();
            } else if (token.getClasse() == Classe.string) {
                token = lexico.nextToken();
                // {A59}
                mais_exp_write();
            } else if (token.getClasse() == Classe.numeroInteiro) {
                token = lexico.nextToken();
                // {A43}
                mais_exp_write();
            }
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Identificador, ou string, ou numeroInteiro esperado ao ler a função exp_write()");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    public void mais_exp_write() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            exp_write();  // Chama exp_write para processar a próxima expressão
        }
    }

// <comando> ::=
//    read ( <var_read> ) |
//    write ( <exp_write> ) |
//    writeln ( <exp_write> ) {A61} |
//    for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} |
//    repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
//    while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
//    if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21} |
//    id {A49} := <expressao> {A22} |
//    <chamada_procedimento>
    public void comando() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("read")) {
            token = lexico.nextToken();
            // Verifica o símbolo de abertura da função read
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                var_read(); // Chama a função para processar <var_read>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("write")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                exp_write(); // Chama a função para processar <exp_write>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("writeln")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                exp_write(); // Chama a função para processar <exp_write>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    // {A61} - Ação associada a writeln
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("for")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.identificador) {
                token = lexico.nextToken();
                if (token.getClasse() == Classe.atribuicao) {
                    token = lexico.nextToken();
                    expressao(); // Chama a função para processar <expressao>
                    if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("to")) {
                        token = lexico.nextToken();
                        expressao(); // Chama a função para processar <expressao>
                        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("do")) {
                            token = lexico.nextToken();
                            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                                token = lexico.nextToken();
                                sentencas(); // Chama a função para processar <sentencas>
                                if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                    token = lexico.nextToken();
                                    // {A13} - Ação associada ao comando for
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'end'");
                                }
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'begin'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'do'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'to'");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado identificador");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'for'");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("repeat")) {
            token = lexico.nextToken();
            // {A14} - Ação associada ao comando repeat
            sentencas(); // Chama a função para processar <sentencas>
            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("until")) {
                token = lexico.nextToken();
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    expressao_logica(); // Chama a função para processar <expressao_logica>
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();
                        // {A15} - Ação associada ao comando repeat
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'until'");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("while")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                expressao_logica(); // Chama a função para processar <expressao_logica>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("do")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                            token = lexico.nextToken();
                            sentencas(); // Chama a função para processar <sentencas>
                            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                token = lexico.nextToken();
                                // {A18} - Ação associada ao comando while
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'end'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'begin'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'do'");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("if")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                expressao_logica(); // Chama a função para processar <expressao_logica>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("then")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                            token = lexico.nextToken();
                            sentencas(); // Chama a função para processar <sentencas>
                            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                token = lexico.nextToken();
                                // {A20} - Ação associada ao comando if
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'end'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'begin'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'then'");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
            pfalsa(); // Chama a função para processar <pfalsa>
        } else if (token.getClasse() == Classe.identificador) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.atribuicao) {
                token = lexico.nextToken();
                expressao(); // Chama a função para processar <expressao>
                // {A22} - Ação associada ao comando de atribuição
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ':='");
            }
        } else {
            //chamada_procedimento(); // Chama a função para processar <chamada_procedimento>
        }
    }

    // <pfalsa> ::= {A25} else begin <sentencas> end | ε
    public void pfalsa() {
        if (token.getClasse() == Classe.palavraReservada && 
            token.getValor().getTexto().equalsIgnoreCase("else")) {
            token = lexico.nextToken(); // Lê o próximo token
            if (token.getClasse() == Classe.palavraReservada && 
                token.getValor().getTexto().equalsIgnoreCase("begin")) {
                token = lexico.nextToken(); // Lê o próximo token
                sentencas(); // Processa as sentenças no bloco else
                if (token.getClasse() == Classe.palavraReservada && 
                    token.getValor().getTexto().equalsIgnoreCase("end")) {
                    token = lexico.nextToken(); // Lê o próximo token
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - (pfalsa) Erro sintático: esperado 'end' após 'else begin'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (pfalsa) Erro sintático: esperado 'begin' após 'else'");
            }
        }
        // Se não houver 'else', simplesmente retorna (ε)
    }

    // <chamada_procedimento> ::= <id_proc> {A50} <argumentos> {A23}
    public void chamada_procedimento() {
        id_proc(); // Processa o identificador do procedimento
        argumentos(); // Processa os argumentos
    }


    // <argumentos> ::= ( <lista_arg> ) | ε
    public void argumentos() {
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken(); // Lê o próximo token
            lista_arg(); // Processa a lista de argumentos
            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken(); // Lê o próximo token
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (argumentos) Erro sintático: esperado fechamento do parênteses ')'");
            }
        }
        // Se não houver parênteses, apenas retorna (ε)
    }

    // <lista_arg> ::= <expressao> <cont_lista_arg>
    public void lista_arg() {
        expressao(); // Processa a expressão
        cont_lista_arg(); // Processa os argumentos adicionais
    }

    // <cont_lista_arg> ::= , <lista_arg> | ε
    public void cont_lista_arg() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken(); // Lê o próximo token
            lista_arg(); // Processa a próxima lista de argumentos
        }
        // Se não houver vírgula, apenas retorna (ε)
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    public void expressao_logica() {
        termo_logico(); // Processa o termo lógico
        mais_expr_logica(); // Processa mais expressões lógicas
    }

    // <mais_expr_logica> ::= or <termo_logico> <mais_expr_logica> {A26} | ε
    public void mais_expr_logica() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("or")) {
            token = lexico.nextToken(); // Lê o próximo token
            termo_logico(); // Processa o próximo termo lógico
            mais_expr_logica(); // Chama recursivamente para mais expressões lógicas
        }
        // Se não houver 'or', epsilon é aceito, não faz nada
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    public void termo_logico() {
        fator_logico(); // Processa o fator lógico
        mais_termo_logico(); // Processa mais termos lógicos
    }

    // <mais_termo_logico> ::= and <fator_logico> <mais_termo_logico> {A27} | ε
    public void mais_termo_logico() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("and")) {
            token = lexico.nextToken(); // Lê o próximo token
            fator_logico(); // Processa o próximo fator lógico
            mais_termo_logico(); // Chama recursivamente para mais termos lógicos
        }
        // Se não houver 'and', epsilon é aceito, não faz nada
    }

    // <fator_logico> ::= <relacional> | ( <expressao_logica> ) | not <fator_logico> {A28} | true {A29} | false {A30}
    public void fator_logico() {
        // Verifica se é a palavra reservada 'not'
        if (token.getClasse() == Classe.palavraReservada && 
            token.getValor().getTexto().equalsIgnoreCase("not")) {
            token = lexico.nextToken(); // Lê o próximo token
            fator_logico(); // Chama recursivamente para processar o fator lógico
        } else if (token.getClasse() == Classe.palavraReservada && 
                token.getValor().getTexto().equalsIgnoreCase("true")) {
            token = lexico.nextToken(); // Lê o próximo token para 'true'
        } else if (token.getClasse() == Classe.palavraReservada && 
                token.getValor().getTexto().equalsIgnoreCase("false")) {
            token = lexico.nextToken(); // Lê o próximo token para 'false'
        } else if (token.getClasse() == Classe.parentesesEsquerdo) { // Verifica o parêntese esquerdo
            token = lexico.nextToken(); // Lê o próximo token
            expressao_logica(); // Processa a expressão lógica dentro dos parênteses
            if (token.getClasse() == Classe.parentesesDireito) { // Verifica o parêntese direito
                token = lexico.nextToken(); // Lê o próximo token
            } else {
                // Erro: Esperado parêntese direito
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (fator_logico) Erro sintático: esperado ')' após expressão lógica.");
            }
        } else {
            relacional(); // Processa uma expressão relacional
        }
    }

    // <relacional> ::= <expressao> = <expressao> {A31} | <expressao> > <expressao> {A32} | <expressao> >= <expressao> {A33} | <expressao> < <expressao> {A34} | <expressao> <= <expressao> {A35} | <expressao> <> <expressao> {A36}
    public void relacional() {
        expressao(); // Processa a primeira expressão

        // Verifica o operador relacional
        if (token.getClasse() == Classe.operadorIgual || 
            token.getClasse() == Classe.operadorMaior || 
            token.getClasse() == Classe.operadorMaiorIgual || 
            token.getClasse() == Classe.operadorMenor || 
            token.getClasse() == Classe.operadorMenorIgual || 
            token.getClasse() == Classe.operadorDiferente) {
            
            Token operador = token; // Armazena o operador atual
            token = lexico.nextToken(); // Lê o próximo token

            expressao(); // Processa a segunda expressão

            // Aqui você pode adicionar lógica para lidar com o operador, se necessário
        } else {
            // Erro: Operador relacional não reconhecido
            System.err.println(token.getLinha() + "," + token.getColuna() + " - (relacional) Erro sintático: operador relacional esperado.");
        }
    }

    // <expressao> ::= <termo> <mais_expressao>
    public void expressao() {
        termo(); // Processa o primeiro termo
        mais_expressao(); // Processa a continuação da expressão
    }

    // <mais_expressao> ::= + <termo> {A37} <mais_expressao>  | - <termo> {A38} <mais_expressao>  | ε
    public void mais_expressao() {
        if (token.getClasse() == Classe.operadorSoma) { // Verifica se é o operador "+"
            token = lexico.nextToken(); // Lê o próximo token
            termo(); // Processa o termo
            // Ação semântica A37 pode ser inserida aqui
            mais_expressao(); // Processa a continuação da expressão
        } else if (token.getClasse() == Classe.operadorSubtracao) { // Verifica se é o operador "-"
            token = lexico.nextToken(); // Lê o próximo token
            termo(); // Processa o termo
            // Ação semântica A38 pode ser inserida aqui
            mais_expressao(); // Processa a continuação da expressão
        }
        // ε (nenhuma ação, retorna vazia)
    }


    // <termo> ::= <fator> <mais_termo>
    public void termo() {
        fator(); // Processa o fator
        mais_termo(); // Processa a continuação do termo
    }

    // <mais_termo> ::= * <fator> {A39} <mais_termo>  | / <fator> {A40} <mais_termo>  | ε
    public void mais_termo() {
        if (token.getClasse() == Classe.operadorMultiplicacao) { // Verifica se é o operador "*"
            token = lexico.nextToken(); // Lê o próximo token
            fator(); // Processa o fator
            // Ação semântica A39 pode ser inserida aqui
            mais_termo(); // Processa a continuação do termo
        } else if (token.getClasse() == Classe.operadorDivisao) { // Verifica se é o operador "/"
            token = lexico.nextToken(); // Lê o próximo token
            fator(); // Processa o fator
            // Ação semântica A40 pode ser inserida aqui
            mais_termo(); // Processa a continuação do termo
        }
        // ε (nenhuma ação, retorna vazia)
    }

    // <fator> ::= <id> | <numero> | ( <expressao> )
    public void fator() {
        if (token.getClasse() == Classe.identificador) { // Verifica se o token é um identificador
            token = lexico.nextToken(); // Lê o próximo token
            // Aqui pode ser inserida a ação semântica, se necessária
        } else if (token.getClasse() == Classe.numeroInteiro) { // Verifica se o token é um número inteiro
            token = lexico.nextToken(); // Lê o próximo token
            // Aqui pode ser inserida a ação semântica, se necessária
        } else if (token.getClasse() == Classe.parentesesEsquerdo) { // Verifica o parêntese esquerdo
            token = lexico.nextToken(); // Lê o próximo token
            expressao(); // Processa a expressão dentro dos parênteses
            if (token.getClasse() == Classe.parentesesDireito) { // Verifica o parêntese direito
                token = lexico.nextToken(); // Lê o próximo token
            } else {
                // Erro: Esperado parêntese direito
                System.err.println(token.getLinha() + "," + token.getColuna() + " - (fator) Erro sintático: esperado ')'.");
            }
        } else {
            // Erro: Esperado identificador, número ou expressão entre parênteses
            System.err.println(token.getLinha() + "," + token.getColuna() + " - (fator) Erro sintático: esperado identificador, número ou '('.");
        }
    }


    public void id_proc(){}

}



