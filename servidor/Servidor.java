import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

/**
 * Servidor.java
 * 
 * Proposito: classe do serviodor, usa um ServerSocket para receber requisições e gerencia as threads em um
 *    servidor multithread com 10 thread no total
 * 
 * @author Alan Bonfim Santos
 */
public class Servidor {
  private static ServerSocket servidor;
  private static Thread[] threads;
  public static Stack<Thread> disponiveis = new Stack<>();

  /**
   * Metodo principal, chamado no inicio do programa java. Nesse caso, ele é reponsavel por gerenciar o servidor
   * e o loop do mesmo. Ela cria o servidor, instacia as threads e por fim fica aguardando requisições
   * 
   * @param args Argumentos passados na linha de comando ao executar o programa
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Iniciando servidor");
    servidor = new ServerSocket(60000);
    threads = new MinhaThread[10];
    for(Thread t : threads) {
      disponiveis.push(t);
    }

    System.out.println("Recuperando dados");
    Banco.recuperarContas();
    
    System.out.println("Servidor funcionando");
    while(true) {
      Socket socket = servidor.accept();
      socket.setKeepAlive(true);
      while(disponiveis.isEmpty());
      MinhaThread thread = (MinhaThread) disponiveis.pop();
      thread = thread == null ? new MinhaThread(socket) : new MinhaThread(socket, thread.getIdd());
      thread.start();
    }
  }
}