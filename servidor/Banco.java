import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Banco.java
 * 
 * Proposito: armazenar as contas, disponibilizar acesso à elas e seus métodos. Além disso,
 * é resposável por armazenar as contas em disco. Basicamente, a interface de comunicação
 * entre as contas e o servidor.
 * 
 * @author Alan Bonfim Santos.
 */ 
public class Banco {
  private static List<Conta> contas;
  private static Semaphore mutex = new Semaphore(1);

  /**
   * Cria uma nova {@link Conta}.
   * 
   * @param senha Senha para a nova conta.
   * @return A {@link Conta} recém criada.
   */
  public static Conta criarConta(String senha) {
    Conta conta = new Conta(senha);
    contas.add(conta);
    salvarContas();
    return conta;
  }

  /**
   * Provura uma conta na lista de contas presentes nessa classe.
   * 
   * @param numeroConta Número da conta para realizar a busca.
   * @return Uma {@link Conta} caso tenha sido encontrada ou {@code null} caso
   *   a conta não esteja na lista.
   */
  public static Conta buscarConta(long numeroConta) {
    for(Conta conta : contas)
      if(conta.getNumeroConta() == numeroConta) return conta;
    
    return null;
  }

  /**
   * Realiza o login de acordo com as credenciais passadas.
   * 
   * @param numeroConta Número da conta que deseja-se acessar.
   * @param senha Senha da conta relativa ao número passado.
   * @return A {@link Conta} caso ela exista e as credencias estejam certas ou {@code null} caso
   *   a conta não esteja na lista ou as credencias estejam erradas.
   */
  public static Conta login(long numeroConta, String senha) {
    Conta conta = buscarConta(numeroConta);
    if (conta != null && conta.getSenha().equals(senha)) 
      return conta;
    
    return null;
  }

  /**
   * Salva a lista de contas em um arquivo ou cria um novo arquivo caso o mesmo
   * não exista.
   */
  public static void salvarContas() {
    try {
      mutex.acquire();
      // somente para verificar se o arquivo existe
      // provavelmente nao eh uma abordagem eficiente
      File file = new File("contas.ser");
      if(!file.exists()) file.createNewFile();
      // ----------------------------------------------

      FileOutputStream fileOutput = new FileOutputStream("contas.ser");
      ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
      objectOutput.writeObject(contas);
      objectOutput.close();
      mutex.release();
    } catch (IOException | InterruptedException e) { }
  }

  /**
   * Recupera a lista de contas salvas em um arquivo. Caso o arquivo não exista, uma lista nova
   * é criada. Se o arquivo existir, a lista salva nele será passada para a lista presente no 
   * {@link Banco} e o número da última conta será incrementado e passado para o contador presente 
   * em {@link Conta} através do método {@link Conta#setContador(long)}
   */
  public static void recuperarContas() {
    try {
      FileInputStream fileInputStream = new FileInputStream("contas.ser");
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      contas = (ArrayList<Conta>) objectInputStream.readObject();

      Conta ultimaConta = contas.get(contas.size()-1); // pega a ultima conta
      Conta.setContador(ultimaConta.getNumeroConta() + 1); // passa o numero da ultima conta + 1, o numero da proxima conta

      objectInputStream.close();
    } catch (FileNotFoundException e) {
      contas = new ArrayList<>();
    } catch (IOException | ClassNotFoundException e) { 
      e.printStackTrace();
    }
  }

  private static List<Conta> listarContas() {
    return contas;
  }
}