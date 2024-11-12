package sintatico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lexico.Categoria;
import lexico.Classe;
import lexico.Lexico;
import lexico.Registro;
import lexico.TabelaSimbolos;
import lexico.Tipo;
import lexico.Token;

public class Sintatico {

    private Lexico lexico;
    private String nomeArquivo;
    private Token token;

    private TabelaSimbolos tabela = new TabelaSimbolos();
    private String rotulo = "";
    private int contRotulo = 1;
    private int offsetVariavel = 0;
    private String nomeArquivoSaida;
    private String caminhoArquivoSaida;
    private BufferedWriter bw;
    private FileWriter fw;
    private static final int TAMANHO_INTEIRO = 4;
    private List<String> variaveis = new ArrayList<>();
    private List<String> sectionData = new ArrayList<>();
    private Registro registro;
    private String rotuloElse;

    public Sintatico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        lexico = new Lexico(nomeArquivo);

        nomeArquivoSaida = "queronemver.asm";
        caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }
    }

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }

    public void analisar() {
        token = lexico.nextToken();
        programa();
        // try{
        // bw.flush();
        // }catch (IOException e){
        // e.printStackTrace();
        // }
    }

    // <programa> ::= program id {A01} ; <corpo> • {A45}
    public void programa() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("program")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.identificador) {
                // {A01} -> Ação associada à declaração do programa, caso seja necessário
                Registro registro = tabela.add(token.getValor().getTexto());
                offsetVariavel = 0;
                registro.setCategoria(Categoria.PROGRAMA_PRINCIPAL);
                escreverCodigo("global main");
                escreverCodigo("extern printf");
                escreverCodigo("extern scanf\n");
                escreverCodigo("section .text");
                rotulo = "main";
                escreverCodigo("\t; Entrada do programa");
                escreverCodigo("\tpush ebp");
                escreverCodigo("\tmov ebp, esp");
                token = lexico.nextToken();
                if (token.getClasse() == Classe.pontoEVirgula) {
                    token = lexico.nextToken();
                    corpo();
                    if (token.getClasse() == Classe.ponto) {
                        token = lexico.nextToken();
                        // {A45} -> Ação associada ao final do programa, caso seja necessário
                        escreverCodigo("\tleave");
                        escreverCodigo("\tret");
                        if (!sectionData.isEmpty()) {
                            escreverCodigo("\nsection .data\n");
                            for (String mensagem : sectionData) {
                                escreverCodigo(mensagem);
                            }
                        }
                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar arquivo de saída");
                        }
                        if (token.getClasse() != Classe.EOF) {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                                    + "(programa) Erro sintático: o programa deve terminar logo após o ponto final (EOF esperado).");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                                + "(programa) Erro sintático: faltou ponto final no programa (.)");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                            + "(programa) Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                        + "(programa) Erro sintático: faltou o nome do programa");
            }
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(programa) Erro sintático: faltou começar o programa com PROGRAM");
        }
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    public void corpo() {
        declara(); // Declarações de variáveis
        rotina(); // Declarações de rotinas (procedimentos ou funções)
        // {A44} -> Ação associada ao corpo do programa
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
            token = lexico.nextToken();
            sentencas(); // Processa sentenças entre begin e end
            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")) {
                token = lexico.nextToken();
                // {A46} -> Ação associada ao final do bloco "begin...end"
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                        + "(corpo) Erro sintático: faltou terminar o corpo do programa com END");
            }
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(corpo) Erro sintático: faltou começar o corpo do programa com BEGIN");
        }
    }

    // <declara> ::= var <dvar> <mais_dc> | ε
    public void declara() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("var")) {
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }

    // <mais_dc> ::= ; <cont_dc>
    public void mais_dc() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            cont_dc();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(mais_dc) Erro sintático: faltou ponto e virgula (;) no final de uma declaração de variáveis");
        }
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    public void cont_dc() {
        if (token.getClasse() == Classe.identificador) {
            dvar();
            mais_dc();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    public void dvar() {
        variaveis();
        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();
            tipo_var();
            int tamanho = 0;
            for (String var : variaveis) {
                tabela.getRegistro(var).setTipo(Tipo.INTEGER);
                tamanho += TAMANHO_INTEIRO;
            }
            escreverCodigo("\tsub esp, " + tamanho);
            variaveis.clear();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(dvar) Erro sintático: faltou dois pontos (:) após as variáveis");
        }
    }

    // <tipo_var> ::= integer
    public void tipo_var() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken(); // Avança para o próximo token
            // {A02}
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(tipo_var) Erro sintático: esperado 'integer' como tipo de variável");
        }
    }

    // <variaveis> ::= id {A03} <mais_var>
    public void variaveis() {
        if (token.getClasse() == Classe.identificador) {
            String variavel = token.getValor().getTexto();
            if (tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " já foi declarada anteriormente");
                System.exit(-1);
            } else {
                tabela.add(variavel);
                tabela.getRegistro(variavel).setCategoria(Categoria.VARIAVEL);
                tabela.getRegistro(variavel).setOffset(offsetVariavel);
                offsetVariavel += TAMANHO_INTEIRO;
                variaveis.add(variavel);
            }
            token = lexico.nextToken();
            // {A03}
            mais_var();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(variaveis) Erro sintático: esperado identificador de variável");
        }
    }

    // <mais_var> ::= , <variaveis> | ε
    public void mais_var() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            variaveis();
        }
    }

    // <rotina> ::= <procedimento> | <funcao> | ε
    public void rotina() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("procedure")) {
            procedimento();
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("function")) {
            funcao();
        }
    }

    // <procedimento> ::= procedure id {A04} <parametros> {A48}; <corpo> {A56} ;
    // <rotina>
    public void procedimento() {
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
                    // {A56}
                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();
                        rotina();
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                                + "(procedimento) Erro sintático: faltou ponto e vírgula (;) após o corpo do procedimento");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                            + "(procedimento) Erro sintático: faltou ponto e vírgula (;) após os parâmetros do procedimento");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                        + "(procedimento) Erro sintático: faltou identificador do procedimento");
            }
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(procedimento) Erro sintático: faltou começar o procedimento com a palavra reservada PROCEDURE");
        }
    }

    // <funcao> ::= function id {A05} <parametros> {A48} : <tipo_funcao> {A47} ;
    // <corpo> {A56} ; <rotina>
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
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                                    + "(funcao) Erro sintático: faltou ponto e vírgula ( ; ) após a função");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                                + "(funcao) Erro sintático: faltou ponto e vírgula ( ; ) após o cabeçalho da função");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                            + "(funcao) Erro sintático: faltou dois pontos ( : ) para o tipo de retorno da função");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                        + "(funcao) Erro sintático: faltou o nome da função");
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
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " - (parametros) Erro sintático: esperado fechamento do parênteses ')'");
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
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "Erro sintático: faltou dois pontos ( : ) após os identificadores de parâmetros");
        }
    }

    // <cont_lista_par> ::= ; <lista_parametros> | ε
    public void cont_lista_par() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            lista_parametros(); // Processa uma nova lista de parâmetros
        }
        // ε (vazio) é tratado implicitamente, já que não há ação no caso do vazio.
    }

    // <lista_id> ::= id {A07} <cont_lista_id>
    public void lista_id() {
        if (token.getClasse() == Classe.identificador) {
            token = lexico.nextToken();
            // {A07} - Ação associada ao identificador (pode ser semântica)
            cont_lista_id(); // Processa o resto da lista
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "(lista_id) Erro sintático: esperado identificador na lista de parâmetros");
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
            System.err.println(token.getLinha() + "," + token.getColuna() + " - "
                    + "Erro sintático: tipo de função inválido, esperado 'integer'");
        }
    }

    // <sentencas> ::= <comando> <mais_sentencas>
    public void sentencas() {
        comando(); // Processa o comando atual
        mais_sentencas(); // Processa mais comandos (se houver)
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    public void mais_sentencas() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken(); // Avança para o próximo token
            cont_sentencas(); // Processa mais sentenças
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
            // {A08} - Ação associada à leitura da variável
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Registro registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tmov edx, ebp");
                    escreverCodigo("\tlea eax, [edx - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush eax");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall scanf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.nextToken();
            mais_var_read();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna()
                    + " - (var_read) Erro sintático: esperado identificador de variável para leitura");
        }
    }

    // <mais_var_read> ::= , <var_read> | ε
    public void mais_var_read() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            var_read(); // Chama var_read para processar a próxima variável
        }
    }

    // <exp_write> ::= id {A09} <mais_exp_write> | string {A59} <mais_exp_write> |
    // intnum {A43} <mais_exp_write>
    public void exp_write() {
        if ((token.getClasse() == Classe.identificador)
                || (token.getClasse() == Classe.string)
                || (token.getClasse() == Classe.numeroInteiro)) {

            // <id> {A09} <mais_exp_write>
            if (token.getClasse() == Classe.identificador) {
                // {A09}
                String variavel = token.getValor().getTexto();
                if (!tabela.isPresent(variavel)) {
                    System.err.println("Variável " + variavel + " não foi declarada");
                    System.exit(-1);
                } else {
                    Registro registro = tabela.getRegistro(variavel);
                    if (registro.getCategoria() != Categoria.VARIAVEL) {
                        System.err.println("Identificador " + variavel + " não é uma variável");
                        System.exit(-1);
                    } else {
                        escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");
                        escreverCodigo("\tpush @Integer");
                        escreverCodigo("\tcall printf");
                        escreverCodigo("\tadd esp, 8");
                        if (!sectionData.contains("@Integer: db '%d',0")) {
                            sectionData.add("@Integer: db '%d',0");
                        }
                    }
                }
                token = lexico.nextToken();
                mais_exp_write();
            } else if (token.getClasse() == Classe.string) {
                // {A59}
                String string = token.getValor().getTexto();
                String rotulo = criarRotulo("String");
                sectionData.add(rotulo + ": db '" + string + "',0");
                escreverCodigo("\tpush " + rotulo);
                escreverCodigo("\tcall printf");
                escreverCodigo("\tadd esp, 4");
                token = lexico.nextToken();
                mais_exp_write();
            } else if (token.getClasse() == Classe.numeroInteiro) {
                // {A43}
                escreverCodigo("\tpush " + token.getValor().getInteiro());
                escreverCodigo("\tpush @Integer");
                escreverCodigo("\tcall printf");
                escreverCodigo("\tadd esp, 8");
                if (!sectionData.contains("@Integer: db '%d',0")) {
                    sectionData.add("@Integer: db '%d',0");
                }
                token = lexico.nextToken();
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
            exp_write(); // Chama exp_write para processar a próxima expressão
        }
    }

    // <comando> ::=
    // read ( <var_read> ) |
    // write ( <exp_write> ) |
    // writeln ( <exp_write> ) {A61} |
    // for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas>
    // end {A13} |
    // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
    // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
    // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>
    // {A21} |
    // id {A49} := <expressao> {A22} |
    // <chamada_procedimento>
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
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("write")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                exp_write(); // Chama a função para processar <exp_write>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                } else {
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("writeln")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                exp_write(); // Chama a função para processar <exp_write>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    // {A61} - Ação associada a writeln
                    String novaLinha = "rotuloStringLN: db '',10,0";
                    if (!sectionData.contains(novaLinha)) {
                        sectionData.add(novaLinha);
                    }
                    escreverCodigo("\tpush rotuloStringLN");
                    escreverCodigo("\tcall printf");
                    escreverCodigo("\tadd esp, 4");
                } else {
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("for")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.identificador) {
                // {A57}
                String variavel = token.getValor().getTexto();
                if (!tabela.isPresent(variavel)) {
                    System.err.println("Variável " + variavel + " não foi declarada");
                    System.exit(-1);
                } else {
                    registro = tabela.getRegistro(variavel);
                    if (registro.getCategoria() != Categoria.VARIAVEL) {
                        System.err.println("O identificador " + variavel + "não é uma variável. A57");
                        System.exit(-1);
                    }
                }
                token = lexico.nextToken();
                if (token.getClasse() == Classe.atribuicao) {
                    token = lexico.nextToken();
                    expressao(); // Chama a função para processar <expressao>
                    // {A11}
                    escreverCodigo("\tpop dword[ebp - " + registro.getOffset() + "]");
                    String rotuloEntrada = criarRotulo("FOR");
                    String rotuloSaida = criarRotulo("FIMFOR");
                    rotulo = rotuloEntrada;
                    if (token.getClasse() == Classe.palavraReservada
                            && token.getValor().getTexto().equalsIgnoreCase("to")) {
                        token = lexico.nextToken();
                        expressao(); // Chama a função para processar <expressao>
                        // {A12}
                        escreverCodigo("\tpush ecx\n"
                                + "\tmov ecx, dword[ebp - " + registro.getOffset() + "]\n"
                                + "\tcmp ecx, dword[esp+4]\n" // +4 por causa do ecx
                                + "\tjg " + rotuloSaida + "\n"
                                + "\tpop ecx");
                        if (token.getClasse() == Classe.palavraReservada
                                && token.getValor().getTexto().equalsIgnoreCase("do")) {
                            token = lexico.nextToken();
                            if (token.getClasse() == Classe.palavraReservada
                                    && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                                token = lexico.nextToken();
                                sentencas(); // Chama a função para processar <sentencas>
                                if (token.getClasse() == Classe.palavraReservada
                                        && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                    token = lexico.nextToken();
                                    // {A13} - Ação associada ao comando for
                                    escreverCodigo("\tadd dword[ebp - " + registro.getOffset() + "], 1");
                                    escreverCodigo("\tjmp " + rotuloEntrada);
                                    rotulo = rotuloSaida;
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna()
                                            + " - (comando) Erro sintático: esperado 'end'");
                                }
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna()
                                        + " - (comando) Erro sintático: esperado 'begin'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " - (comando) Erro sintático: esperado 'do'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " - (comando) Erro sintático: esperado 'to'");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna()
                            + " - (comando) Erro sintático: esperado identificador");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'for'");
            }
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("repeat")) {
            token = lexico.nextToken();
            // {A14} - Ação associada ao comando repeat
            String rotRepeat = criarRotulo("Repeat");
            rotulo = rotRepeat;
            sentencas(); // Chama a função para processar <sentencas>
            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("until")) {
                token = lexico.nextToken();
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    expressao_logica(); // Chama a função para processar <expressao_logica>
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();
                        // {A15} - Ação associada ao comando repeat
                        escreverCodigo("\tcmp dword[esp], 0");
                        escreverCodigo("\tje " + rotRepeat);
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " - (comando) Erro sintático: esperado ')'");
                    }
                } else {
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado 'until'");
            }
        } else if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("while")) {
            token = lexico.nextToken();
            // {A16}
            String rotuloWhile = criarRotulo("While");
            String rotuloFim = criarRotulo("FimWhile");
            rotulo = rotuloWhile;
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                expressao_logica(); // Chama a função para processar <expressao_logica>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    // {A17}
                    escreverCodigo("\tcmp dword[esp], 0");
                    escreverCodigo("\tje " + rotuloFim);
                    if (token.getClasse() == Classe.palavraReservada
                            && token.getValor().getTexto().equalsIgnoreCase("do")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.palavraReservada
                                && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                            token = lexico.nextToken();
                            sentencas(); // Chama a função para processar <sentencas>
                            if (token.getClasse() == Classe.palavraReservada
                                    && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                token = lexico.nextToken();
                                // {A18} - Ação associada ao comando while
                                escreverCodigo("\tjmp " + rotuloWhile);
                                rotulo = rotuloFim;
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna()
                                        + " - (comando) Erro sintático: esperado 'end'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " - (comando) Erro sintático: esperado 'begin'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " - (comando) Erro sintático: esperado 'do'");
                    }
                } else {
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
        } else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("if")) {
            token = lexico.nextToken();
            // {A19}
            rotuloElse = criarRotulo("Else");
            String rotuloFim = criarRotulo("FimIf");
            escreverCodigo("\tcmp dword[esp], 0\n");
            escreverCodigo("\tje " + rotuloElse);
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                expressao_logica(); // Chama a função para processar <expressao_logica>
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    if (token.getClasse() == Classe.palavraReservada
                            && token.getValor().getTexto().equalsIgnoreCase("then")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.palavraReservada
                                && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                            token = lexico.nextToken();
                            sentencas(); // Chama a função para processar <sentencas>
                            if (token.getClasse() == Classe.palavraReservada
                                    && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                token = lexico.nextToken();
                                // {A20} - Ação associada ao comando if
                                escreverCodigo("\tjmp " + rotuloFim);
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna()
                                        + " - (comando) Erro sintático: esperado 'end'");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " - (comando) Erro sintático: esperado 'begin'");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " - (comando) Erro sintático: esperado 'then'");
                    }
                } else {
                    System.err.println(
                            token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ')'");
                }
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado '('");
            }
            pfalsa(); // Chama a função para processar <pfalsa>
            // {A21}
            rotulo = rotuloFim;
        } else if (token.getClasse() == Classe.identificador) {
            // {A49}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A49");
                    System.exit(-1);
                }
            }
            token = lexico.nextToken();
            if (token.getClasse() == Classe.atribuicao) {
                token = lexico.nextToken();
                expressao(); // Chama a função para processar <expressao>
                // {A22} - Ação associada ao comando de atribuição
                registro = tabela.getRegistro(variavel);
                escreverCodigo("\tpop eax");
                escreverCodigo("\tmov dword[ebp - " + registro.getOffset() + "], eax");
            } else {
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (comando) Erro sintático: esperado ':='");
            }
        } else {
            // chamada_procedimento(); // Chama a função para processar
            // <chamada_procedimento>
        }
    }

    // <pfalsa> ::= {A25} else begin <sentencas> end | ε
    public void pfalsa() {
        // {A25}
        escreverCodigo(rotuloElse + ":");
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
                    System.err.println(token.getLinha() + "," + token.getColuna()
                            + " - (pfalsa) Erro sintático: esperado 'end' após 'else begin'");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " - (pfalsa) Erro sintático: esperado 'begin' após 'else'");
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
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " - (argumentos) Erro sintático: esperado fechamento do parênteses ')'");
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
            // {A26}
            String rotSaida = criarRotulo("SaidaMEL");
            String rotVerdade = criarRotulo("VerdadeMEL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tmov dword [ESP + 4], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotVerdade;
            escreverCodigo("\tmov dword [ESP + 4], 1");
            rotulo = rotSaida;
            escreverCodigo("\tadd esp, 4");
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
            // {A27}
            String rotSaida = criarRotulo("SaidaMTL");
            String rotFalso = criarRotulo("FalsoMTL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
            mais_termo_logico(); // Chama recursivamente para mais termos lógicos
        }
        // Se não houver 'and', epsilon é aceito, não faz nada
    }

    // <fator_logico> ::= <relacional> | ( <expressao_logica> ) | not <fator_logico>
    // {A28} | true {A29} | false {A30}
    public void fator_logico() {
        // Verifica se é a palavra reservada 'not'
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("not")) {
            token = lexico.nextToken(); // Lê o próximo token
            fator_logico(); // Chama recursivamente para processar o fator lógico
            // {A28}
            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 1");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("true")) {
            token = lexico.nextToken(); // Lê o próximo token para 'true'
            // {A29}
            escreverCodigo("\tpush 1");
        } else if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("false")) {
            token = lexico.nextToken(); // Lê o próximo token para 'false'
            // {A30}
            escreverCodigo("\tpush 0");
        } else if (token.getClasse() == Classe.parentesesEsquerdo) { // Verifica o parêntese esquerdo
            token = lexico.nextToken(); // Lê o próximo token
            expressao_logica(); // Processa a expressão lógica dentro dos parênteses
            if (token.getClasse() == Classe.parentesesDireito) { // Verifica o parêntese direito
                token = lexico.nextToken(); // Lê o próximo token
            } else {
                // Erro: Esperado parêntese direito
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " - (fator_logico) Erro sintático: esperado ')' após expressão lógica.");
            }
        } else {
            relacional(); // Processa uma expressão relacional
        }
    }

    // <relacional> ::= <expressao> = <expressao> {A31} |
    // <expressao> > <expressao> {A32} |
    // <expressao> >= <expressao> {A33} |
    // <expressao> < <expressao> {A34} |
    // <expressao> <= <expressao> {A35} |
    // <expressao> <> <expressao> {A36}
    private void relacional() {
        expressao();
        if (token.getClasse() == Classe.operadorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A31}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMaior) {
            token = lexico.nextToken();
            expressao();
            // {A32}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjle " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMaiorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A33}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjl " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMenor) {
            token = lexico.nextToken();
            expressao();
            // {A34}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjge " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMenorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A35}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjg " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorDiferente) {
            token = lexico.nextToken();
            expressao();
            // {A36}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tje " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Operador relacional (=, <, <=, >, >= <>) esperado (relacional).");
        }
    }

    // <expressao> ::= <termo> <mais_expressao>
    public void expressao() {
        termo(); // Processa o primeiro termo
        mais_expressao(); // Processa a continuação da expressão
    }

    // <mais_expressao> ::= + <termo> {A37} <mais_expressao> | - <termo> {A38}
    // <mais_expressao> | ε
    public void mais_expressao() {
        if (token.getClasse() == Classe.operadorSoma) { // Verifica se é o operador "+"
            token = lexico.nextToken(); // Lê o próximo token
            termo(); // Processa o termo
            // Ação semântica A37 pode ser inserida aqui
            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax");
            mais_expressao(); // Processa a continuação da expressão
        } else if (token.getClasse() == Classe.operadorSubtracao) { // Verifica se é o operador "-"
            token = lexico.nextToken(); // Lê o próximo token
            termo(); // Processa o termo
            // Ação semântica A38 pode ser inserida aqui
            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax");
            mais_expressao(); // Processa a continuação da expressão
        }
        // ε (nenhuma ação, retorna vazia)
    }

    // <termo> ::= <fator> <mais_termo>
    public void termo() {
        fator(); // Processa o fator
        mais_termo(); // Processa a continuação do termo
    }

    // <mais_termo> ::= * <fator> {A39} <mais_termo> | / <fator> {A40} <mais_termo>
    // | ε
    public void mais_termo() {
        if (token.getClasse() == Classe.operadorMultiplicacao) { // Verifica se é o operador "*"
            token = lexico.nextToken(); // Lê o próximo token
            fator(); // Processa o fator
            // Ação semântica A39 pode ser inserida aqui
            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword [ESP], eax");
            mais_termo(); // Processa a continuação do termo
        } else if (token.getClasse() == Classe.operadorDivisao) { // Verifica se é o operador "/"
            token = lexico.nextToken(); // Lê o próximo token
            fator(); // Processa o fator
            // Ação semântica A40 pode ser inserida aqui
            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tidiv ecx");
            mais_termo(); // Processa a continuação do termo
        }
        // ε (nenhuma ação, retorna vazia)
    }

    // <fator> ::= <id> | <numero> | ( <expressao> )
    public void fator() {
        if (token.getClasse() == Classe.identificador) { // Verifica se o token é um identificador
            // Aqui pode ser inserida a ação semântica, se necessária
            // {A55}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A55");
                    System.exit(-1);
                }
            }
            escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");
            token = lexico.nextToken(); // Lê o próximo token
        } else if (token.getClasse() == Classe.numeroInteiro) { // Verifica se o token é um número inteiro
            // {A41}
            escreverCodigo("\tpush " + token.getValor().getInteiro());
            token = lexico.nextToken(); // Lê o próximo token
            // Aqui pode ser inserida a ação semântica, se necessária
        } else if (token.getClasse() == Classe.parentesesEsquerdo) { // Verifica o parêntese esquerdo
            token = lexico.nextToken(); // Lê o próximo token
            expressao(); // Processa a expressão dentro dos parênteses
            if (token.getClasse() == Classe.parentesesDireito) { // Verifica o parêntese direito
                token = lexico.nextToken(); // Lê o próximo token
            } else {
                // Erro: Esperado parêntese direito
                System.err.println(
                        token.getLinha() + "," + token.getColuna() + " - (fator) Erro sintático: esperado ')'.");
            }
        } else {
            // Erro: Esperado identificador, número ou expressão entre parênteses
            System.err.println(token.getLinha() + "," + token.getColuna()
                    + " - (fator) Erro sintático: esperado identificador, número ou '('.");
        }
    }

    public void id_proc() {
    }

}
