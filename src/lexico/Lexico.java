package lexico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Lexico {
    private String nomeArquivo;
    private Token token;
    private BufferedReader br;
    private char caractere;
    private StringBuilder lexema = new StringBuilder();
    private TabelaSimbolos tabelaSimbolos;


    private int linha;
    private int coluna;

    private List<String> palavrasReservadas = Arrays.asList(
        "and", "array", "begin", "case", "const",
        "div", "do", "downto", "else", "end",
        "file", "for", "function", "goto", "if",
        "in", "label", "mod", "nil", "not",
        "of", "or", "packed", "procedure", "program",
        "record", "repeat", "set", "then", "to",
        "type", "until", "var", "while", "with",
        "integer", "real", "boolean", "char", "string",
        "write", "writeln", "read");

    public Lexico(String nomeArquivo) {
        linha = 1;
        coluna = 0;
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        tabelaSimbolos = new TabelaSimbolos();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            this.br = br;
            caractere = proximoChar();
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo: " + nomeArquivo);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private char proximoChar() {
        try {
            coluna++;
            return (char) br.read();
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
            e.printStackTrace();
            System.exit(-1);
        }
        return 0;
    }

    public Token nextToken(){
        lexema.setLength(0);

        do{
            if(caractere==' ' || caractere == '\t'){
                while(caractere==' ' || caractere == '\t'){
                    caractere = proximoChar();
                }  
            }

            else if(caractere=='\n'){
                while(caractere=='\n'){
                    linha++;
                    coluna = 0;
                    caractere = proximoChar();
                }
            }

            else if(Character.isDigit(caractere)){
                token = new Token(linha, coluna);

                while(Character.isDigit(caractere)){
                    lexema.append(caractere);
                    caractere = proximoChar();
                }

                token.setClasse(Classe.numeroInteiro);
                token.setValor(new Valor(Integer.parseInt(lexema.toString())));
                return token;
            }
            
            else if(Character.isAlphabetic(caractere)){
                token = new Token(linha, coluna);

                while (Character.isAlphabetic(caractere)||Character.isDigit(caractere)) {
                    lexema.append(caractere);
                    caractere = proximoChar();   
                }

                token.setClasse(Classe.identificador);
                token.setValor(new Valor(lexema.toString()));

                if(palavrasReservadas.contains(lexema.toString().toLowerCase())){
                    token.setClasse(Classe.palavraReservada);
                }
                else{
                    tabelaSimbolos.add(lexema.toString());
                }

                return token;
            }
            
            else if(caractere==65535){
                token = new Token(linha, coluna);
                token.setClasse(Classe.EOF);
                return token;
            }

            else if(caractere=='+'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorSoma);
                return token;
            }

            else if(caractere=='-'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorSubtracao);
                return token;
            }

            else if(caractere=='*'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorMultiplicacao);
                return token;
            }

            else if(caractere=='/'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorDivisao);
                return token;
            }

            else if(caractere==':'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.doisPontos);

                if(caractere=='='){
                    token = new Token(linha, coluna);
                    caractere = proximoChar();
                    token.setClasse(Classe.atribuicao);
                }
                return token;
            }

            else if(caractere==';'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.pontoEVirgula);
                return token;
            }

            else if(caractere==','){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.virgula);
                return token;
            }

            else if(caractere=='.'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.ponto);
                return token;
            }

            else if(caractere=='>'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorMaior);

                if(caractere=='='){
                    token = new Token(linha, coluna);
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorMaiorIgual);
                }
                return token;
            }

            else if(caractere=='<'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorMenor);

                if(caractere=='='){
                    token = new Token(linha, coluna);
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorMenorIgual);
                }
                else if(caractere=='>'){
                    token = new Token(linha, coluna);
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorDiferente);
                }
                return token;
            }

            else if(caractere=='='){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.operadorIgual);
                return token;
            }

            else if(caractere=='('){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.parentesesEsquerdo);
                return token;
            }

            else if(caractere==')'){
                token = new Token(linha, coluna);
                caractere = proximoChar();
                token.setClasse(Classe.parentesesDireito);
                return token;
            }

            else if(caractere=='{'){
                caractere = proximoChar();

                while(caractere!='}'){        
                    if(caractere=='\n'){
                        linha++;
                        coluna = 0;
                        caractere = proximoChar();
                    } else {
                        caractere = proximoChar();
                    }
                }
                
                if (caractere == '}') {
                    caractere = proximoChar();

                } else {
                    token.setClasse(Classe.EOF);
                    return token;
                }
            }

            else if(caractere=='\''){
                token = new Token(linha, coluna);
                caractere = proximoChar();

                while(caractere!='\''){        
                    if(caractere=='\n'){
                        token.setClasse(Classe.EOF);
                        return token;
                    } else {
                        lexema.append(caractere);
                        caractere = proximoChar();
                    }
                }
                
                caractere = proximoChar();
                token.setClasse(Classe.string);
                token.setValor(new Valor(lexema.toString()));
                return token;
            }
            
            else {
                System.out.println("Erro no sistema");
            }
            
        }while(caractere!=65535);

        token = new Token(linha, coluna);
        token.setClasse(Classe.EOF);

        return token;  
    }

    public TabelaSimbolos getTabelaSimbolos(){
        return tabelaSimbolos;
    }

}

