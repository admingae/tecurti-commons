package com.tecurti.model.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.tecurti.model.utils.ModelUtils;
import com.tecurti.view.util.WebUtils;
import com.tecurti.view.util.WebUtils.HttpMethod;

public class PagarMeService {

    public RespostaCadastrarCartaoCredito cadastrarCartao(String cardHash, String apiKey) throws Exception {
	String url = "https://api.pagar.me/1/cards";
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("api_key", apiKey);
	map.put("card_hash", cardHash);
	
	String respostaJson = WebUtils.fazerChamadaWebservice(url, HttpMethod.POST, map);
	Map<String, Object> mapResposta = WebUtils.mapJsonDeserializer.deserialize(respostaJson);
	
	RespostaCadastrarCartaoCredito resposta = new RespostaCadastrarCartaoCredito(mapResposta.get("errors")!= null, (String)mapResposta.get("id"));
	return resposta;
    }
    
    /**
     * @param cardId
     * @param apiKey
     * @param valorEmDecimais
     * @param soft_descriptor Descri��o que aparecer� na fatura depois do nome da loja. M�ximo de 13 caracteres
     * @return
     * @throws Exception
     */
    public static class ParametrosEfetuarPagamentoNoCartaoCredito {
	public String cardId;
	public String apiKey;
	public BigDecimal valorEmDecimais;
	
	// Descri��o que aparecer� na fatura depois do nome da loja. M�ximo de 13 caracteres
	public String softDescriptor;
	
	public boolean capture = true;
	
	// Voc� pode passar dados adicionais na cria��o da transa��o para posteriormente filtrar estas na nossa dashboard.
	public Map<String, String> metadata = new HashMap<String, String>();
	
	// Se o pagamento for boleto, o padr�o � 1
	public Integer installments;
	public ParametrosEfetuarPagamentoNoCartaoCredito(String cardId, String apiKey, BigDecimal valorEmDecimais) {
	    super();
	    this.cardId = cardId;
	    this.apiKey = apiKey;
	    this.valorEmDecimais = valorEmDecimais;
	}
    }
    
   public static class RespostaCadastrarCartaoCredito {
	   public boolean isErro;
	   public String cardId;
	   
	   public RespostaCadastrarCartaoCredito(boolean isErro, String cardId) {
		this.isErro = isErro;
		this.cardId = cardId;
	   }
   }
    
    
    
    public static class RespostaEfetuarPagamentoNoCartaoCredito {
	public boolean isErroInterno;
	public StatusTransacaoPagarMe status;
	public Integer idTransacao;
	public StatusReasonTransacaoPagarMe statusReason;
	
	// Mensagem de resposta do adquirente referente ao status da transa��o.
	public String acquirerResponseCode;
	public String descErroInterno;
	

	@Override
	public String toString() {

	    return "[status=" + status + ", idTransacao=" + idTransacao + ", acquirerResponseCode=" + acquirerResponseCode + "]";

	}
    }
    
    public RespostaEfetuarPagamentoNoCartaoCredito efetuarPagamentoNoCartaoCredito(ParametrosEfetuarPagamentoNoCartaoCredito parametros) throws Exception {
	
	RespostaEfetuarPagamentoNoCartaoCredito resposta = new RespostaEfetuarPagamentoNoCartaoCredito();
	
	try {
	    String url = "https://api.pagar.me/1/transactions";
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("api_key", parametros.apiKey);
	    map.put("card_id", parametros.cardId);
	    map.put("amount", ModelUtils.converterValorMonetarioDeDecimalParaCentavos(parametros.valorEmDecimais));

	    if (ModelUtils.isNotEmptyTrim(parametros.softDescriptor)) {
	        map.put("soft_descriptor", ModelUtils.trunc(parametros.softDescriptor, 13));
	    } 
	    if (parametros.installments != null && parametros.installments >= 1 && parametros.installments <= 12) {
	        map.put("installments", parametros.installments);
	    } 
	    
	    for (String keyMetadata : parametros.metadata.keySet()) {
	        map.put("metadata["+keyMetadata+"]", parametros.metadata.get(keyMetadata));
	    }
	    
	    String respostaJson = WebUtils.fazerChamadaWebservice(url, HttpMethod.POST, map);
	    Map<String, Object> mapResposta = WebUtils.mapJsonDeserializer.deserialize(respostaJson);
	    
	    resposta.idTransacao = (Integer) mapResposta.get("id");
	    resposta.status = criarEnumStatusTransacaoPagarMe((String) mapResposta.get("status"));

	    resposta.statusReason = criarEnumStatusReasonTransacaoPagarMe((String) mapResposta.get("status_reason"));
	    resposta.acquirerResponseCode = (String) mapResposta.get("acquirer_response_code");

	} catch (Exception e) {
	    resposta.isErroInterno = true;
	    resposta.descErroInterno = e.toString();
	    e.printStackTrace();
	}
	
	return resposta;
    }

    private StatusReasonTransacaoPagarMe criarEnumStatusReasonTransacaoPagarMe(String object) {
	try {
	    return StatusReasonTransacaoPagarMe.valueOf(object.toUpperCase());
	} catch (Exception e) {
	    return null;
	}
    }

    public StatusTransacaoPagarMe criarEnumStatusTransacaoPagarMe(String object) {
	try {
	    return StatusTransacaoPagarMe.valueOf(object.toUpperCase());
	} catch (Exception e) {
	    return null;
	}
    }
    
    public static enum StatusReasonTransacaoPagarMe {
	ACQUIRER, 
	
	ANTIFRAUD, 
	
	INTERNAL_ERROR, 
	
	NO_ACQUIRER, 
	
	ACQUIRER_TIMEOUT
    }
    /*
     * Para cada atualiza��o no processamento da transa��o, esta propriedade ser� alterada, e o objeto 
     * transaction retornado como resposta atrav�s da sua URL de postback ou ap�s o t�rmino do processamento da a��o atual. 
     * Valores poss�veis: processing, authorized, paid, refunded, waiting_payment, pending_refund, refused
     * Bom dia Felipe,
     * 
     * n�o conhe�o o tipo de sistema que voc� est� implementando, e os tipos de pagamento que voc� permite, ent�o falar o que � certo e/ou errado pode ser relativo.
     * 
     * No caso do pagamento por boleto, quando este � criado a transa��o fica com status waiting_payment at� que o cliente pague o boleto, ent�o isso n�o � um erro.
     * 
     * Se voc� utilizar uma url de postback (o que � altamente recomendado), sempre que houver uma altera��o no status da transa��o voc� ser� notificado, ent�o voc� pode refinar sua verifica��o condicional com base nessas informa��es (https://pagar.me/docs/api/#estados-das-transaes).
     * 
     * Quando voc� cria uma transa��o com uma url de postback definida, o primeiro status retornado � o processing, e a medida que a transa��o � processada voc� recebe os pr�ximos status.
     * 
     * Se voc� n�o utilizar uma url de postback, caso a transa��o demore mais tempo que o habitual para ser realizada, voc� pode perder a conex�o com nosso sistema e dessa forma tratar como recusada uma transa��o que seria capturada com sucesso.
     * 
     * Sobre as informa��es que voc� deve mostrar para seu cliente, isso � um guia que n�o temos definido neste momento. Pelo tipo de sistema que voc� est� criando e seu p�blico, voc� deve analisar a melhor forma de comunicar com seu cliente, baseado nos status que voc� ir� receber.
     * 
     * Caso tenha mais d�vidas, estamos � disposi��o.
     * 
     * Att,
     * 
     * Eric Douglas
     */
    public static enum StatusTransacaoPagarMe {

	// quando � feita alguma transa��o de cartao de credito com url de postback retorna esse estado
        PROCESSING, 
        
        AUTHORIZED, 
        
        // transa��o paga (autorizada e capturada).
        PAID, 
        
        // transa��o estornada.
        REFUNDED, 
        
        // quando � feita alguma transa��o de boleto bancario retorna essa constante
        WAITING_PAYMENT, 
        
        // transa��o paga com boleto aguardando para ser estornada.
        PENDING_REFUND, 
        
        // transa��o n�o autorizada (acredito q seja para boleto ou cartao de credito).
        REFUSED
    }
    
    public Map<String, Object> getTransacaoById(int idTransacao, Object apiKey) throws Exception {
	
	String url = "https://api.pagar.me/1/transactions/"+idTransacao;
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("api_key", apiKey);
	
	String respostaJson = WebUtils.fazerChamadaWebservice(url, HttpMethod.GET, map);
	
	Map<String, Object> mapResposta = WebUtils.mapJsonDeserializer.deserialize(respostaJson);
	return mapResposta;
    }

    /*public static void main(String[] args) throws Exception {
	PagarMeService pagarMeService = new PagarMeService();
	ParametrosEfetuarPagamentoNoCartaoCredito param = new ParametrosEfetuarPagamentoNoCartaoCredito("card_ci87syzxn003r30168pzo21sd", Config.PAGARME_API_KEY, new BigDecimal(25.15));
	param.softDescriptor = "Mensalidade teCurti";
	param.metadata.put("idUsuario", "89799696769");
	param.metadata.put("nomeUsuario", "Felipe");
	param.metadata.put("email", "usuario@gmail.com");
	RespostaEfetuarPagamentoNoCartaoCredito resposta = pagarMeService.efetuarPagamentoNoCartaoCredito(param);
	System.err.println("idTransacao: " + resposta.idTransacao);
	System.err.println("status: " + resposta.status);
	
//	new PagarMeService().cadastrarCartao("dewdewf", "dewfewfew");
	
	Map<String, Object> respostaMap = new PagarMeService().getTransacaoById(192902, Config.PAGARME_API_KEY);
	ModelUtils.outprintMap(respostaMap);
    }*/
    
}






















