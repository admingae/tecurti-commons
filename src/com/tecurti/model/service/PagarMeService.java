package com.tecurti.model.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.tecurti.model.utils.ModelUtils;
import com.tecurti.view.util.WebUtils;
import com.tecurti.view.util.WebUtils.HttpMethod;

public class PagarMeService {

    public String cadastrarCartao(String cardHash, String apiKey) throws Exception {
	String url = "https://api.pagar.me/1/cards";
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("api_key", apiKey);
	map.put("card_hash", cardHash);
	
	String respostaJson = WebUtils.fazerChamadaWebservice(url, HttpMethod.POST, map);
	Map<String, Object> mapResposta = WebUtils.mapJsonDeserializer.deserialize(respostaJson);
	
	return (String) mapResposta.get("id");
    }
    
    /**
     * 
     * @param cardId
     * @param apiKey
     * @param valorEmDecimais
     * @param soft_descriptor Descrição que aparecerá na fatura depois do nome da loja. Máximo de 13 caracteres
     * @return
     * @throws Exception
     */
    public static class ParametrosEfetuarPagamentoNoCartaoCredito {
	public String cardId;
	public String apiKey;
	public BigDecimal valorEmDecimais;
	
	// Descrição que aparecerá na fatura depois do nome da loja. Máximo de 13 caracteres
	public String softDescriptor;
	
	// Você pode passar dados adicionais na criação da transação para posteriormente filtrar estas na nossa dashboard.
	public Map<String, String> metadata = new HashMap<String, String>();
	
	// Se o pagamento for boleto, o padrão é 1
	public Integer installments;
	public ParametrosEfetuarPagamentoNoCartaoCredito(String cardId, String apiKey, BigDecimal valorEmDecimais) {
	    super();
	    this.cardId = cardId;
	    this.apiKey = apiKey;
	    this.valorEmDecimais = valorEmDecimais;
	}
    }
    
    public static class RespostaEfetuarPagamentoNoCartaoCredito {
	public boolean isErro;
	public StatusTransacaoPagarMe status;
	public Integer idTransacao;
	
	// Mensagem de resposta do adquirente referente ao status da transação.
	public String mensagemReferenteAoStatus;

	@Override
	public String toString() {
	    return "[status=" + status + ", idTransacao=" + idTransacao + ", mensagemReferenteAoStatus=" + mensagemReferenteAoStatus + "]";
	}
    }
    
    public RespostaEfetuarPagamentoNoCartaoCredito efetuarPagamentoNoCartaoCredito(ParametrosEfetuarPagamentoNoCartaoCredito parametros) throws Exception {
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
//	ModelUtils.outprintMap(mapResposta);
	
	RespostaEfetuarPagamentoNoCartaoCredito resposta = new RespostaEfetuarPagamentoNoCartaoCredito();
	resposta.idTransacao = (Integer) mapResposta.get("id");
	resposta.status = criarEnumStatusTransacaoPagarMe((String) mapResposta.get("status"));
	resposta.mensagemReferenteAoStatus = (String) mapResposta.get("acquirer_response_code");
	return resposta;
    }

    public StatusTransacaoPagarMe criarEnumStatusTransacaoPagarMe(String object) {
	try {
	    return StatusTransacaoPagarMe.valueOf(object.toUpperCase());
	} catch (Exception e) {
	    return null;
	}
    }
    
    /*
     * Para cada atualização no processamento da transação, esta propriedade será alterada, e o objeto 
     * transaction retornado como resposta através da sua URL de postback ou após o término do processamento da ação atual. 
     * Valores possíveis: processing, authorized, paid, refunded, waiting_payment, pending_refund, refused
     */
    public static enum StatusTransacaoPagarMe {

        PROCESSING, AUTHORIZED, PAID, REFUNDED, WAITING_PAYMENT, PENDING_REFUND, REFUSED
        
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






















