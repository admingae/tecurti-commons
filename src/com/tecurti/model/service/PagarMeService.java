package com.tecurti.model.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tecurti.model.entidades.TipoErroCommons;
import com.tecurti.model.utils.ModelUtils;
import com.tecurti.view.util.WebUtils;
import com.tecurti.view.util.WebUtils.HttpMethod;

public class PagarMeService {

    public static class RespostaCadastrarCartao {
	boolean isErro;
	TipoErroCommons tipoErro;
	public String cardId; 
    }
    public RespostaCadastrarCartao cadastrarCartao(String cardHash, String apiKey) throws Exception {
	String url = "https://api.pagar.me/1/cards";
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("api_key", apiKey);
	map.put("card_hash", cardHash);
	
	String respostaJson = WebUtils.fazerChamadaWebservice(url, HttpMethod.POST, map);
	Map<String, Object> mapResposta = WebUtils.mapJsonDeserializer.deserialize(respostaJson);
	
	RespostaCadastrarCartao resposta = new RespostaCadastrarCartao();
	List<Map<String, Object>> list = (List<Map<String, Object>>) mapResposta.get("errors");
	if (list == null || list.isEmpty()) {
	    resposta.isErro = false;
	    resposta.cardId = (String) mapResposta.get("id");
	    return resposta;
	} else {
	    resposta.isErro = true;
	    Map<String, Object> erro = list.get(0);
	    
	    String tipoErro = (String) erro.get("parameter_name");
	    if ("card_expiration_date".equalsIgnoreCase(tipoErro)) {
		resposta.tipoErro = TipoErroCommons.CARTAO_CREDITO_DATA_EXPIRACAO_INVALIDA;
		return resposta;
	    } else {
		resposta.tipoErro = TipoErroCommons.CARTAO_CREDITO_ERRO_DESCONHECIDO_CARD_ID;
		return resposta;
	    }
	}
    }
    
    /**
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
	
	public boolean capture = true;
	
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
	public boolean isErroInterno;
	public StatusTransacaoPagarMe status;
	public Integer idTransacao;
	public StatusReasonTransacaoPagarMe statusReason;
	
	// Mensagem de resposta do adquirente referente ao status da transação.
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
	    map.put("capture", parametros.capture);
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
     * Para cada atualização no processamento da transação, esta propriedade será alterada, e o objeto 
     * transaction retornado como resposta através da sua URL de postback ou após o término do processamento da ação atual. 
     * Valores possíveis: processing, authorized, paid, refunded, waiting_payment, pending_refund, refused
     * Bom dia Felipe,
     * 
     * não conheço o tipo de sistema que você está implementando, e os tipos de pagamento que você permite, então falar o que é certo e/ou errado pode ser relativo.
     * 
     * No caso do pagamento por boleto, quando este é criado a transação fica com status waiting_payment até que o cliente pague o boleto, então isso não é um erro.
     * 
     * Se você utilizar uma url de postback (o que é altamente recomendado), sempre que houver uma alteração no status da transação você será notificado, então você pode refinar sua verificação condicional com base nessas informações (https://pagar.me/docs/api/#estados-das-transaes).
     * 
     * Quando você cria uma transação com uma url de postback definida, o primeiro status retornado é o processing, e a medida que a transação é processada você recebe os próximos status.
     * 
     * Se você não utilizar uma url de postback, caso a transação demore mais tempo que o habitual para ser realizada, você pode perder a conexão com nosso sistema e dessa forma tratar como recusada uma transação que seria capturada com sucesso.
     * 
     * Sobre as informações que você deve mostrar para seu cliente, isso é um guia que não temos definido neste momento. Pelo tipo de sistema que você está criando e seu público, você deve analisar a melhor forma de comunicar com seu cliente, baseado nos status que você irá receber.
     * 
     * Caso tenha mais dúvidas, estamos à disposição.
     * 
     * Att,
     * 
     * Eric Douglas
     */
    public static enum StatusTransacaoPagarMe {

	// quando é feita alguma transação de cartao de credito com url de postback retorna esse estado
        PROCESSING, 
        
        AUTHORIZED, 
        
        // transação paga (autorizada e capturada).
        PAID, 
        
        // transação estornada.
        REFUNDED, 
        
        // quando é feita alguma transação de boleto bancario retorna essa constante
        WAITING_PAYMENT, 
        
        // transação paga com boleto aguardando para ser estornada.
        PENDING_REFUND, 
        
        // transação não autorizada (acredito q seja para boleto ou cartao de credito).
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






















