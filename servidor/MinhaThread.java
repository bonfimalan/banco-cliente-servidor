import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * MinhaThread.java
 * 
 * Proposito: uma thread do servidor que é ativada após o servidor estabelecer uma conexão com um cliente, 
 *    ela recebe as requisições, as trata e responde ao cliente
 * 
 * @author Alan Bonfim Santos
 */
public class MinhaThread extends Thread {
  private Scanner entrada;
  private int id;
  private static int contador;
  private Socket socket;
  private static final String CODIGO_OK = "OK\n";
  private static final String CODIGO_ERRO = "ER\n";

  /**
   * Construtor que define o id da thread de forma automatica
   * 
   * @param socket Socket que é usada para comunicar com o cliente
   */
  public MinhaThread(Socket socket) {
    this.socket = socket;
    this.id = contador;
    contador++;
  }

  /**
   * Construtor que recebe um id para a thread, usado para criar uma thread a partir
   * de outra que foi destuida
   * 
   * @param socket Socket que é usada para comunicar com o cliente
   * @param id Usado para identificar a thread, importante para debugar
   */
  public MinhaThread(Socket socket, int id) {
    this.socket = socket;
    this.id = id;
  }

  public int getIdd() {
    return this.id;
  }
  
  /**
   * 
   */
  @Override
  public void run() {
    try {
      Conta conta = null;
      String[] entrada;
      String metodo;

      // login
      do {
        // recebe a string de entrada e separa as palavras em um vetor
        // 0 => codigo, 1 => numero da conta, 2 => senha
        entrada = receberMsg().split("\\s+");
        metodo = entrada[0];
        if(metodo.equals("ENC")) {
          encerrar();
          return;
        }
        conta = executarEntrada(metodo, entrada);

      } while (conta == null);

      // processando transacoes
      do {
        entrada = receberMsg().split("\\s+");        
        metodo = entrada[0];

        executarTransacao(metodo, conta, entrada);
        
      } while (!metodo.equals("ENC"));

      encerrar();
    } catch (IOException | InterruptedException e) { }
  }

  /**
   * Envia uma mensagem ao cliente avisando que foi encerrada a aplicação e
   * coloca a esta thread na pilha, para que possa ser reutilizada
   * 
   * @throws IOException
   */
  private void encerrar() throws IOException {
    enviarMsg(CODIGO_OK + "Aplicacao encerrada");
    Servidor.disponiveis.push(this);
  }

  /**
   * Acessa o banco e realiza o login através do método {@link Banco#login(long, String)},
   * se o login for bem sucedido ele retorna a conta, caso contrário, ele retorna {@code null}
   * 
   * @param numeroConta Numero da conta
   * @param senha Senha da conta
   * @return {@code null} ou uma {@link Conta} relativa aos dados passados
   */
  private Conta login(long numeroConta, String senha) {
    return Banco.login(numeroConta, senha);
  }

  /**
   * Acessa o banco e cria uma conta através do método {@link Banco#criarConta(String)}
   * @param senha Senha para a nova conta
   * @return uma {@link Conta} recem criada
   */
  private Conta criarConta(String senha) {
    return Banco.criarConta(senha);
  }

  /**
   * Executa o login ou a criação de uma conta, dependendo do método recebido na mensagem
   * vinda do cliente
   * 
   * @param metodo O nome do método que foi passado pelo cliente
   * @param args Argumentos passado na mensagem, as palavras que vêm após o método
   * @return uma {@link Conta}, que pode ser uma conta nova ou já existente
   * @throws IOException
   */
  private Conta executarEntrada(String metodo, String... args) throws IOException {
    Conta conta = null;
    switch (metodo) {
      case "LOG":
        {
          conta = login(Long.parseLong(args[1]), args[2]);
          if(conta == null) 
            enviarMsg(CODIGO_ERRO + "Numero da conta ou senha errados");
          else 
            enviarMsg(CODIGO_OK + "Login efetuado com sucesso");
        }
        break;
      case "NEW":
        {
          conta = criarConta(args[1]);
          enviarMsg(CODIGO_OK + "Conta criada com o numero " + conta.getNumeroConta() + ", anote esse numero");
        }
        break;
    }// fim switch
    return conta;
  }

  /**
   * Executa uma transação que será de acordo com o método recebido do cliente
   * 
   * @param metodo O nome do método que foi passado pelo cliente
   * @param conta A conta do cliente
   * @param args Argumentos passado na mensagem, as palavras que vêm após o método
   * 
   * @throws InterruptedException
   * @throws IOException
   */
  private void executarTransacao(String metodo, Conta conta, String... args) throws InterruptedException, IOException {
    switch(metodo) {
      case "DEP": // deposito
        {
          long montante = Long.parseLong(args[1]);
          conta.deposito(montante);
          enviarMsg(CODIGO_OK + "Deposito executado");
        }
        break;
      case "RET": // retirada
        {
          long montante = Long.parseLong(args[1]);
          if(conta.retirada(montante))
            enviarMsg(CODIGO_OK + "Retirada de R$" + toReais(montante) + " executado com sucesso");
          else
            enviarMsg(CODIGO_ERRO + "Saldo insuficiente");
        }
        break;
      case "TRA": // transferencia
        {
          long montante = Long.parseLong(args[1]);
          long numeroContaDestino = Long.parseLong(args[2]);
          try {
            if(conta.transferencia(numeroContaDestino, montante))
              enviarMsg(CODIGO_OK + "R$" + toReais(montante) + " transferidos com sucesso para a conta " + numeroContaDestino);
            else
              enviarMsg(CODIGO_ERRO + "Saldo insuficiente");
          } catch (Conta.ContaInexistenteException e) {
            enviarMsg(CODIGO_ERRO + "Conta destino inexistente");
          }
        }
        break;
      case "SAL": // visualizar saldo
        enviarMsg(CODIGO_OK + conta.visualizarSaldo());
        break;
      case "EXT": // extrato
        enviarMsg(CODIGO_OK + conta.emitirExtrato());
        break;
    } //fim switch
  }

  /**
   * Recebe uma mensagem do cliente e, caso a conexão seja encerrada por parte do cliente, ele chama o
   * método {@link #encerrar()}
   * 
   * @return A mensagem recebida através do socket
   * @throws IOException
   */
  private String receberMsg() throws IOException {
    // todas as msg serao de somente uma linha
    entrada = new Scanner(socket.getInputStream());
    String stringEntrada = "";
    try{ 
      stringEntrada = entrada.nextLine();
    } catch (NoSuchElementException e) {encerrar();}

    if(stringEntrada.equals("0")) encerrar();
    
    return stringEntrada;
  }

  /**
   * Envia uma mensagem ao cliente
   * 
   * @param msg A mensangem que deverá ser enviada
   * @throws IOException
   */
  private void enviarMsg(String msg) throws IOException {
    PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
    saida.println(msg);
  }

  /**
   * Converte da notação de long (em que os últimos dois digitos representam centavos
   * e o restante representa reais) para uma String com um formato monetario 
   * 
   * @param montante O valor que será convertido
   * @return uma Sting com uma vírgula nos antes dos últimos dois digitos
   */
  private static String toReais(long montante) {
    long reais = montante / 100;
    int centavos = (int) montante % 100;

    if(centavos <=9)
      return reais + ",0" + centavos;
    else
      return reais + "," + centavos;
  }
}