import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Conta.java
 * 
 * Proposito: representar dados relativos á uma conta bancária
 * 
 * @author Alan Bonfim Santos
 */
public class Conta implements Serializable {
  private final long NUMERO_CONTA;
  private long saldo;
  private String senha;
  private List<Transacao> historicoTransacaos;
  // Semaphore para cuidar de problemas por concorrencia
  private Semaphore mutex;
  // contador para tornar cada numero de conta unico
  private static long contador = 1000; 

  /**
   * Único construtor, usado para criar uma nova conta no método {@link Banco#criarConta(String)}.
   * O número da conta é obtido através do contador que é incrementado automáticamente
   * 
   * @param senha A senha para a nova conta
   */
  public Conta(String senha) {
    this.NUMERO_CONTA = contador;
    contador++;
    this.senha = senha;
    this.historicoTransacaos = new ArrayList<>();
    this.mutex = new Semaphore(1);
  }

  /**
   * Realiza um deposito na conta e solicita ao Banco que salve as alterações
   * 
   * @param quantia Valor a ser depositado
   * @throws InterruptedException
   */
  public void deposito(long quantia) throws InterruptedException {
    mutex.acquire();
    saldo += quantia;
    adicionarTransacao("Deposito", quantia);
    mutex.release();
    Banco.salvarContas();
  }

  /**
   * Retira dinheiro da conta se o saldo for o suficiente
   * 
   * @param quantia Quantidade a ser retirada
   * @return {@code true} se o saldo disponível tiver sido o suficiente e
   *    {@code false} se o saldo tiver sido insuficiente
   * @throws InterruptedException
   */
  public boolean retirada(long quantia) throws InterruptedException {
    mutex.acquire();
    boolean success = quantia < saldo;
    if (success) {
      saldo-=quantia;
      adicionarTransacao("Retirada", quantia);
    }

    mutex.release();
    Banco.salvarContas();
    return success;
  }

  /**
   * Realiza uma transferência da conta atual para uma conta de destino
   * 
   * @param numeroContaDestino O número da conta que receberá a transferência
   * @param quantia O valor que será transfericdo
   * @return {@code true} se o saldo disponível tiver sido o suficiente e
   *    {@code false} se o saldo tiver sido insuficiente
   * @throws InterruptedException
   * @throws ContaInexistenteException O número da conta destino passado não foi encontrada, 
   *    logo ela não existe
   */
  public boolean transferencia(long numeroContaDestino, long quantia) throws InterruptedException, ContaInexistenteException {
    Conta contaDestino = Banco.buscarConta(numeroContaDestino);
    if (contaDestino == null)
      throw new ContaInexistenteException();
    mutex.acquire();
    boolean success = quantia < saldo;
    if (success) {
      saldo-=quantia;
      contaDestino.deposito(quantia);
      adicionarTransacao("Transferencia", quantia);
    }
    mutex.release();
    Banco.salvarContas();
    return success;
  }

  /**
   * Acessa o saldo da conta e o retorna em formato monetario
   * 
   * @return O valor em reais presente na conta
   */
  public String visualizarSaldo() {
    long reais = saldo / 100;
    int centavos = (int) saldo % 100;
    
    if(centavos <=9)
      return "R$" + reais + ",0" + centavos;
    else
      return "R$" + reais + "," + centavos;
  }

  /**
   * Gera um extrato da conta com o número, saldo e a quantidade de transações realizadas
   * 
   * @return O extrato da conta
   */
  public String emitirExtrato() {
    return 
          "Numero da conta: " + this.NUMERO_CONTA + "\n" +
          "Saldo: " + this.visualizarSaldo() + "\n" +
          "Numero de transacoes: " + historicoTransacaos.size();
  }

  /**
   * Adiciona uma transação ao histórico
   * 
   * @param tipo O tipo da transação realizada
   * @param quantia a quantida que foi movimentada
   */
  private void adicionarTransacao(String tipo, long quantia) {
    historicoTransacaos.add(
      new Transacao(tipo, quantia, this.saldo)
    );
  }

  public long getNumeroConta() {
    return this.NUMERO_CONTA;
  }
  public String getSenha() {
    return this.senha;
  }

  public static void setContador(long cont) {
    contador = cont;
  }

  /**
   * Exceção para caso uma conta não tenha sido encontrada, usada no
   * método de transferência
   */
  public class ContaInexistenteException extends Exception {
    public ContaInexistenteException(){
      super();
    }
  }
}