package com.tecurti.model.service.idioma;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class i18nUsandoApiPadrao implements Serializable {
    
    private static Logger logger = Logger.getLogger(i18nUsandoApiPadrao.class.getName());
    
    private static Map<Locale, ResourceBundle> mapCacheResourceBundle = new HashMap<>();
    
    ResourceBundle bundle;
    
    public i18nUsandoApiPadrao(Locale locale) {
	bundle = mapCacheResourceBundle.get(locale);
	if (bundle == null) {
	    ResourceBundle novoBundle = ResourceBundle.getBundle("messages", locale);
	    
	    mapCacheResourceBundle.put(locale, novoBundle);
	    bundle = novoBundle;
	}
    }

    private void doConstrutor(Locale locale) {
	bundle = mapCacheResourceBundle.get(locale);
	if (bundle == null) {
	    ResourceBundle novoBundle = ResourceBundle.getBundle("messages", locale);
	    
	    mapCacheResourceBundle.put(locale, novoBundle);
	    bundle = novoBundle;
	}
    }

    public String geti18nTexto(String chave, Object... parametros) {
	return geti18nTexto(chave, true, parametros);
    }
    
    public String geti18nTexto(String chave, boolean asHTML, Object... parametros) {
	
	String valor = getBundle().getString(chave);

	if (parametros.length > 0) {
	    valor = valor.replaceAll("'", "''"); // se tiver um apostrofe do tipo You've o metodo MessageFormat.format "se perde" precisa ficar You''ve pra dar certo
	    valor = MessageFormat.format(valor, parametros);
	}

	return valor;
    }
    
    public static void main(String[] args) {
	String string = "{0}felip'e{1}";
	string = string.replaceAll("'", "''");
	logger.info(MessageFormat.format(string, "000", "111"));
    }

    public ResourceBundle getBundle() {
        return bundle;
    }
}




