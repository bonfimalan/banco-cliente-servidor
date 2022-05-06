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
  private double valorTransacao;
  private double saldo;
  
  /**
   * Único construtor, recebe todos os dados presentes em uma transação, com execeção
   * da data que será instanciada com a data atual
   * 
   * @param tipo Tipo da transação
   * @param valorTransacao O valor movimentado pela transação
   * @param saldo O saldo após a execução da transação
   */
  public Transacao(String tipo, double valorTransacao, double saldo) {
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

  public double getValorTransacao() {
    return valorTransacao;
  }

  public void setValorTransacao(double valorTransacao) {
    this.valorTransacao = valorTransacao;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }  
}
