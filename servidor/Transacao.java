import java.io.Serializable;
import java.util.Date;

/**
 * Transacao.java
 * 
 * Proposito: representar dados relativos á uma transação bancária
 * 
 * @author Alan Bonfim Santos
 */
public class Transacao implements Serializable {
  private Date data;
  private String tipo;
  private long valorTransacao;
  private long saldo;
  
  /**
   * Único construtor, recebe todos os dados presentes em uma transação, com execeção
   * da data que será instanciada com a data atual
   * 
   * @param tipo Tipo da transação
   * @param valorTransacao O valor movimentado pela transação
   * @param saldo O saldo após a execução da transação
   */
  public Transacao(String tipo, long valorTransacao, long saldo) {
    this.data = new Date();
    this.tipo = tipo;
    this.valorTransacao = valorTransacao;
    this.saldo = saldo;
  }

  public Date getDate() {
    return data;
  }
  
  public void setDate(Date date) {
    this.data = date;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public long getValorTransacao() {
    return valorTransacao;
  }

  public void setValorTransacao(long valorTransacao) {
    this.valorTransacao = valorTransacao;
  }

  public long getSaldo() {
    return saldo;
  }

  public void setSaldo(long saldo) {
    this.saldo = saldo;
  }  
}
