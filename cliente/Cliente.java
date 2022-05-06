import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Cliente.java
 * 
 * Proposito: enviar requisições ao servidor e receber respostas dele
 * 
 * @author Alan Bonfim Santos
 */
public class Cliente {
  private static final String NEW = "NEW ";
  private static final String LOG = "LOG ";
  private static final String DEP = "DEP ";
  private static final String RET = "RET ";
  private static final String TRA = "TRA ";
  private static final String SAL = "SAL ";
  private static final String EXT = "EXT ";
  private static final String ENC = "ENC ";
  
  /**
   * Método principal, nesse caso ele estabelece conexão com o servidor e oferece uma
   * interface em linha de comando para o usuário acessar a sua conta salva no servidor
   * do banco
   * 
   * @param args Argumentos passados na linha de comando ao executar o programa
   * 
   * @throws UnknownHostException
   * @throws IOException
   */
  public static void main(String[] args) throws UnknownHostException, IOException {
    Socket socket = new Socket("localhost", 60000);
    
    Scanner teclado = new Scanner(System.in);
    
    int opcao = 0;
    String codigoMsg = "";
    while (!codigoMsg.equals("OK") && opcao != 3) {
      System.out.print("\n" + menuLogin());
      opcao = teclado.nextInt();

      switch(opcao) {
        case 1:
          {
            System.out.print("Digite uma senha: ");
            String senha = teclado.next();
            enviarMsg(socket, NEW + senha);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 2:
          {
            System.out.print("Digite o numero da sua conta: ");
            Long numeroConta = teclado.nextLong();
            System.out.print("Digite sua senha: ");
            String senha = teclado.next();
            enviarMsg(socket, LOG + numeroConta + " " + senha);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 3:
          {
            enviarMsg(socket, ENC);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
      }
    }

    if (opcao == 3) opcao = 6;
    
    while (opcao != 6) {
      System.out.print("\n" + menuTransacao());
      opcao = teclado.nextInt();

      switch(opcao) {
        case 1: // deposito
          {
            System.out.print("Digite o montante que deseja depositar\n(no formato Y,XX, em que Y = reais e X = centavos): ");
            String montante = teclado.next();

            enviarMsg(socket, DEP + formatarDinheiro(montante.toCharArray()));

            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 2: // retirada
          {
            System.out.print("Digite o montante que deseja retirar\n(no formato Y,XX, em que Y = reais e X = centavos): ");
            String montante = teclado.next();

            enviarMsg(socket, RET + formatarDinheiro(montante.toCharArray()));

            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 3: // transferencia
          {
            System.out.print("Digite o montante que deseja transferir\n(no formato Y,XX, em que Y = reais e X = centavos): ");
            String montante = teclado.next();
            System.out.print("Digite o numero da conta destino: ");
            long numeroContaDestino = teclado.nextLong();

            enviarMsg(socket, TRA + formatarDinheiro(montante.toCharArray()) + " " + numeroContaDestino);

            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 4: // visualizar saldo
          {
            enviarMsg(socket, SAL);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
        case 5: // extrato
          {
            enviarMsg(socket, EXT);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            
            String extrato = "";
            for(int i=0; i<3; i++)
              extrato += saida.nextLine() + "\n";
            System.out.print(extrato);
          }
          break;
        case 6:
          {
            enviarMsg(socket, ENC);
            Scanner saida = receberMsg(socket);
            codigoMsg = saida.nextLine();
            System.out.println(saida.nextLine());
          }
          break;
      }
    }

    socket.close();
    teclado.close();
  }

  /**
   * Recebe uma mensagem do servidor
   * 
   * @param socket Socket relativo a conexão com o servidor
   * 
   * @return um Scanner com o Stream de dados recebido so servidor
   * 
   * @throws IOException
   */
  private static Scanner receberMsg(Socket socket) throws IOException {
    return new Scanner(socket.getInputStream());
  }

  /**
   * Envia uma mensagem ao servidor
   * 
   * @param socket Socket relativo a conexão com o servidor
   * @param msg A mensagem que será enviada ao servidor
   * 
   * @throws IOException
   */
  private static void enviarMsg(Socket socket, String msg) throws IOException {
    PrintWriter saida = new PrintWriter(
      socket.getOutputStream(),
      true
    );
    saida.println(msg);
  }

  /**
   * O dinheiro é recebido através de uma String no formato monetatio que precisa
   * ser passado para long antes de ser enviado ao servidor
   * 
   * @param montanteCharArray O montante passado pelo usuário no formato de char array,
   *      mais fácil para manipulação
   * @return O montante passado para long
   */
  private static long formatarDinheiro(char[] montanteCharArray) {
    String montanteString = "";
    int cont = 0; // contar o numero de centavos
    boolean hasVirgula = false;
    for (char c : montanteCharArray) {
      if(c == ',') hasVirgula = true;       // Achou a virgula
      else montanteString += c;             // Achou um digito
      if(hasVirgula) cont++;                // Ja que achou a virgula estamos nos centavos
      if(cont == 3) break;                  // Se o contador for igual a 2 ele encerra o loop, caso o 
                                            // usuario tenha digitado mais do que dois digitos depois a da virgula
    }
    if(!hasVirgula) montanteString += "00"; // não foi encontrada uma vérgula

    return Long.parseLong(montanteString);
  }

  /**
   * Um menu para dar instruções ao usuário
   * 
   * @return O menu com as opções numeradas para o processo de login
   */
  private static String menuLogin() {
    return 
      "Digite um dos numeros para escolher uma opcao:\n" +
      ">1 Registrar conta\n" +
      ">2 Realizar login\n" +
      ">3 Encerrar sessao\n" +
      ">";
  }

  /**
   * Um menu para dar instruções ao usuário
   * 
   * @return O menu com as opções numeradas para o processo de transação
   */
  private static String menuTransacao() {
    return 
      "Digite um dos numeros para escolher uma opcao:\n" +
      ">1 Deposito\n" +
      ">2 Retirada\n" +
      ">3 Transferencia\n" +
      ">4 Visualizar saldo\n" +
      ">5 Emitir extrato\n" +
      ">6 Encerrar\n" +
      ">";
  }
}
