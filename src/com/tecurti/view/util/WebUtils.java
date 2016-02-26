package com.tecurti.view.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.tecurti.model.entidades.Dimension;
import com.tecurti.model.entidades.MimeType;
import com.tecurti.model.entidades.TipoErroCommons;
import com.tecurti.model.utils.ModelUtils;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class WebUtils {
    
    private static Logger logger = Logger.getLogger(WebUtils.class.getName());
    
    public static final int STATUS_RESPONSE_NOT_MODIFIED = 304;
    public static final int STATUS_RESPONSE_NORMAL = 200;
    private static String VERSAO_SEM_INTERROGACAO = "v=";
    private static String VERSAO_COM_INTERROGACAO = "?"+VERSAO_SEM_INTERROGACAO;
    
//    public static String URL_SEM_USUARIO = "/recursos/imagens/usuario_sem_foto.gif"; 
    public static String URL_SEM_IMAGEM = "/recursos/imagens/sem_imagem.png"; 

    public static JSONSerializer jsonSerializer = new JSONSerializer().exclude(/*"class", "key", "parentKey"*/);
    public static JSONDeserializer<Map<String, Object>> mapJsonDeserializer = new JSONDeserializer<>();
    public static JSONDeserializer<List<Map<String, Object>>> listMapJsonDeserializer = new JSONDeserializer<>();
    
    public static String lastModifiedParaCache = criarHttpDateFormat().format(new GregorianCalendar(2000, 1, 1).getTime());
    public static String expires = criarHttpDateFormat().format(createDataExpires());

    public static String HASH_EXIBIR_IMAGEM = "exibirImagem";
    public static String HASH_URL_GENERICA = "urlGenerica";
    
    public static String versionar(boolean colocarInterrogacao, Object valorVersao) {
	String prefix = colocarInterrogacao ? VERSAO_COM_INTERROGACAO : VERSAO_SEM_INTERROGACAO;
	return prefix + valorVersao;
    }
    public static String versionar(Object valorVersao) {
	return versionar(true, valorVersao);
    }
    
    public static void popularObjectComParameters(Object object, HttpServletRequest request) throws IllegalAccessException {

	if (object instanceof Map) {
	    Map map = (Map) object;
	    Set<String> keySet = request.getParameterMap().keySet();
	    for (String key : keySet) {
		String valor = request.getParameter(key);
		map.put(key, valor);
	    }
	    return;
	}
	
	Field[] fields = object.getClass().getDeclaredFields();
	for (Field field : fields) {
	    field.setAccessible(true);
	    boolean naoEhStatic = false == Modifier.isStatic(field.getModifiers());
	    if (naoEhStatic) {
		String valor = request.getParameter(field.getName());
		if (field.getType().equals(String.class)) {
		    field.set(object, valor == null ? "" : valor);
		}
	    }
	}
    }
    
    public static void popularObjectComParametersMultipartFormData(Object object, HttpServletRequest request) throws IllegalAccessException, Exception {
	popularObjectComParametersMultipartFormData(object, request, false);
    }
    public static void popularObjectComParametersMultipartFormData(Object object, HttpServletRequest request, boolean log) throws IllegalAccessException, Exception {
	
	Map map = null;
	boolean isMap = false;
	if (object instanceof Map) {
	    isMap = true;
	    map = (Map) object;
	} 
	
	// ----------------
	ServletFileUpload uploadHandler = new ServletFileUpload();
	FileItemIterator iterator = uploadHandler.getItemIterator(request);
	while (iterator.hasNext()) {
	    FileItemStream item = iterator.next();
	    if (log) {
		System.err.println("item.getFieldName(): " + item.getFieldName());
	    }
	    InputStream stream = item.openStream();
	    byte[] byteArray = IOUtils.toByteArray(stream);

	    Field field = null;
	    if (isMap == false) {
		field = findFieldByReflection(object.getClass(), item.getFieldName());
		if (field == null) {
		    continue;
		} 
		field.setAccessible(true);
	    }
	    
	    if (item.isFormField()) {
		String valor = URLDecoder.decode(new String(byteArray), "UTF-8");
		
		if (isMap) {
		    map.put(item.getFieldName(), valor);
		} else {
		    Object valorFinal = null;
		    if (valor != null) {
			if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
			    if (valor.equalsIgnoreCase("on")) {
				valorFinal = true;
			    } else {
				valorFinal = Boolean.parseBoolean(valor);
			    }
			} else {
			    valorFinal = valor;
			}
		    }
		    
		    field.set(object, valorFinal);
		}
	    } else {
		if (byteArray.length == 0) {
		    
		    UploadedFile uploadedFile = null;
		    if (isMap) {
			map.put(item.getFieldName(), uploadedFile);
		    } else {
			field.set(object, uploadedFile);
		    }
		} else {
		    UploadedFile uploadedFile = new UploadedFile(byteArray, MimeType.findByDescricaoMimeType(item.getContentType()));
		    uploadedFile.nomeArquivo = item.getName();
		    uploadedFile.nomeParametro = item.getFieldName();
		    
		    if (isMap) {
			map.put(item.getFieldName(), uploadedFile);
		    } else {
			field.set(object, uploadedFile);
		    }
		}
	    }
	}
    }
    
    private static Field findFieldByReflection(Class klass, String nomeField) {
	try {
	    Field field = klass.getDeclaredField(nomeField);
	    return field;
	} catch (NoSuchFieldException e) {
	    return null;
	} catch (SecurityException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    
    public static String fromJavaToJavascriptString(String valorJava) {
	return valorJava
		.replace("'", "\\'") 	// fe'li'pe ===>> fe\'li\'pe
		.replace("\"", "\\\"") 	// fe"li"pe ===>> fe\"li\"pe
		.replace("\r", "").replace("\n", "\\n");
    }
    
    public static List<Browser> getBrowsers(String userAgent) {
	
	List<Browser> listBrowser = new ArrayList<Browser>();
	for (Browser browser : Browser.values()) {
	    if (browser.isUserAgent(userAgent)) {
		listBrowser.add(browser);
	    }
	}
	return listBrowser;
    }
    
    public static enum Browser {
	IE() {
	    @Override
	    public int getVersao(String userAgent) {
		if (userAgent == null) {return -1;}
		try {
		    userAgent = userAgent.toUpperCase();
		    
		    if (userAgent.replaceAll(" ", "").contains("RV:11.")) {
			return 11;
		    }
		    
		    Pattern pattern = Pattern.compile("MSIE.*?;");
		    Matcher matcher = pattern.matcher(userAgent);

		    if(matcher.find())
		    {
		        String match = matcher.group(0);
		        String numeroAsText = match.replace("MSIE", "").replace(";", "").trim();
		        int numero  = (int) Double.parseDouble(numeroAsText);
		        return numero;
		    }
		} catch (Exception e) {
		    logger.log(Level.SEVERE, "", e);
		    //e.printStackTrace();
		}
	        
	        throw new RuntimeException("Não foi possivel encontrar versão do Internet Explorer");
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		String userAgentLowerSemEspacos = userAgent.toLowerCase().replaceAll(" ", "");
		
		if (userAgentLowerSemEspacos.contains("msie")) {
		    return true;
		}
		
		return userAgentLowerSemEspacos.contains("rv:11.") && userAgentLowerSemEspacos.contains("trident/7");
	    }
	}, FIREFOX() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		String userAgentLower = userAgent.toLowerCase();
		return userAgentLower.contains("firefox");
	    }
	}, CHROME() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		String userAgentLower = userAgent.toLowerCase();
		return userAgentLower.contains("chrome");
	    }
	}, SAFARI() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		String userAgentLower = userAgent.toLowerCase();
		return userAgentLower.contains("mobile") == false
			&& userAgentLower.contains("chrome") == false
			&& userAgentLower.contains("safari");
	    }
	}, MOBILE() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		return userAgent.toLowerCase().contains("mobile");
	    }
	}, ANDROID() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		return userAgent.toLowerCase().contains("android");
	    }
	}, IPHONE() {
	    @Override
	    public int getVersao(String userAgent) {
		return -1;
	    }
	    @Override
	    public boolean isUserAgent(String userAgent) {
		if (userAgent == null) {return false;}
		return userAgent.toLowerCase().contains("iphone");
	    }
	};
	
	public abstract int getVersao(String userAgent);
	public abstract boolean isUserAgent(String userAgent);
    }
    public static boolean isBrowser(HttpServletRequest request, Browser... browsers) throws Exception {
	
	for (Browser browser : browsers) {
	    
	    String userAgent = request.getHeader("user-agent");
	    if (browser.isUserAgent(userAgent)) {
		return true;
	    }
	}
	
	return false;
    }
    
    public static int getVersaoBrowser(HttpServletRequest request) throws Exception {
	Browser[] browsers = Browser.values();
	for (Browser browser : browsers) {
	    if (isBrowser(request, browser)) {
		return getVersaoBrowser(request, browser);
	    }
	}
	
	return -1;
    }
    
    public static int getVersaoBrowser(HttpServletRequest request, Browser browser) {

	String userAgent = request.getHeader("user-agent");
	return browser.getVersao(userAgent);
    }
    
    /**
     * @deprecated usar as classes do pacote com.tecurti.model.entidades.mapa 
     */
    public static Map<String,String> getEstadosBrasil(){
	return null;
	/*Map<String,String>estados = new TreeMap<>();
	estados.put("AC", "Acre");
	estados.put("AL", "Alagoas");
	estados.put("AM", "Amazonas");
	estados.put("BA", "Bahia");
	estados.put("CE", "Ceará");
	estados.put("DF", "Distrito Federal");
	estados.put("ES", "Espírito Santo");
	estados.put("GO", "Goiás");
	estados.put("MA", "Maranhão");
	estados.put("MT", "Mato Grosso");
	estados.put("MS", "Mato Grosso do Sul");
	estados.put("MG", "Minas Gerais");
	estados.put("PA", "Pará");
	estados.put("PB", "Paraíba");
	estados.put("PR", "Paraná");
	estados.put("PE", "Pernambuco");
	estados.put("RJ", "Rio de Janeiro");
	estados.put("RN", "Rio Grande do Norte");
	estados.put("RS", "Rio Grande do Sul");
	estados.put("RO", "Rondônia");
	estados.put("RR", "Roraima");
	estados.put("SC", "Santa Catarina");
	estados.put("SP", "São Paulo");
	estados.put("SE", "Sergipe");
	estados.put("TO", "Tocantins");
	return estados;*/
	
    }
    
    public static String toJavascriptObjectIncluindoCampos(Class classeContantes, String... camposParaIncluir) {
	
	
	List<String> listCamposParaIncluir = Arrays.asList(camposParaIncluir);
	
	List<String> listCamposParaExcluir = new ArrayList<>();
	for (Field field : classeContantes.getDeclaredFields()) {
	    boolean isCamposParaExcluir = listCamposParaIncluir.contains(field.getName()) == false;
	    if (isCamposParaExcluir) {
		listCamposParaExcluir.add(field.getName());
	    }
	}
	
	return toJavascriptObject(classeContantes, listCamposParaExcluir.toArray(new String[]{}));
    }
    
    public static String toJavascriptObject(Class classeContantes, String... camposParaExcluir) {
	
	try {
	    StringBuilder s =new StringBuilder();
		    
	    s.append("var ").append(classeContantes.getSimpleName()).append(" = {");
	    List<Field> fields = filtrarCamposParaJavascripObjects(classeContantes.getDeclaredFields(), camposParaExcluir);
	    
	    for (int i = 0; i < fields.size(); i++) {
		Field field = fields.get(i);

		field.setAccessible(true);

		boolean isTipoNumerico = isTipoNumerico(field); 
		boolean naoEhTipoNumerico = isTipoNumerico == false;

		s.append("\n\t").append(field.getName()).append(": ");
		if (naoEhTipoNumerico) {
		    s.append("'");
		}
		s.append(field.get(null));
		if (naoEhTipoNumerico) {
		    s.append("'");
		}
		
		boolean isUltimaIteracao = i == (fields.size()-1);
		if (isUltimaIteracao == false) {
		    s.append(",");
		}
	    }
	    s.append("\n};");
	    
	    return s.toString();
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    //e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    
    private static List<Field> filtrarCamposParaJavascripObjects(Field[] declaredFields, String[] camposParaExcluir) {
	    
	List<Field> fieldsFiltrados = new ArrayList<Field>();
	
	for (Field field : declaredFields) {

	    boolean deveExcluirCampo = field.getName().equals("ENUM$VALUES") || contains(field.getName(), camposParaExcluir);
	    if (deveExcluirCampo == false) { 
		boolean deveFiltrarCampo = field.isEnumConstant() || Modifier.isStatic(field.getModifiers());
		if (deveFiltrarCampo) {
		    fieldsFiltrados.add(field);
		}
	    }
	}
	
	return fieldsFiltrados;
    }
    private static boolean contains(String name, String[] camposParaExcluir) {
	for (String campo : camposParaExcluir) {
	    if (campo.equals(name)) {
		return true;
	    }
	}
	return false;
    }
    public static boolean isTipoNumerico(Field field) {
	return field.getType().equals(int.class) 
	    			|| field.getType().equals(Integer.class)
	    			|| field.getType().equals(long.class)
	    			|| field.getType().equals(Long.class) 
	    			|| field.getType().equals(short.class) 
	    			|| field.getType().equals(Short.class) 
	    			|| field.getType().equals(byte.class) 
	    			|| field.getType().equals(Byte.class);
    }

    public static String encodeURIComponent(String texto) {
	return (String) evalJavascript(texto, "encodeURIComponent('"+texto+"')");
    }
    
    public static String decodeURIComponent(String texto) {
	return (String) evalJavascript(texto, "decodeURIComponent('"+texto+"')");
    }

    private static Object evalJavascript(String texto, String scriptParaEval) {
	if (texto == null) {
	    return null;
	}
	try {
	    
	    ScriptEngineManager factory = new ScriptEngineManager();
	    ScriptEngine engine = factory.getEngineByName("JavaScript");
	    Object eval = engine.eval(scriptParaEval);
	    return eval;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
    
    public static Date createDataExpires() {
	GregorianCalendar gregorianCalendar = new GregorianCalendar();
	gregorianCalendar.add(Calendar.YEAR, 15);
	return gregorianCalendar.getTime();
    }
    private static DateFormat criarHttpDateFormat() {
	DateFormat dateFormatParaCache = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	dateFormatParaCache.setTimeZone(TimeZone.getTimeZone("GMT"));
	return dateFormatParaCache;
    }

    public static int gerarEtag(byte[] bytes) {
	return new HashCodeBuilder().append(bytes).toHashCode();
    }

    public static int setHeaderAtributosParaCache(HttpServletRequest request, HttpServletResponse response, byte[] bytes, int tempoEmSegundosCache) {
	return setDadosParaCache(request, response, tempoEmSegundosCache, gerarEtag(bytes));
    }
    
    private static int TEMPO_EM_SEGUNDOS_CACHE_ETERNO = 60 * 60 * 24 * 365 * 15; 
    public static int setHeaderAtributosParaCacheEterno(HttpServletRequest request, HttpServletResponse response) {
	return setDadosParaCache(request, response, TEMPO_EM_SEGUNDOS_CACHE_ETERNO, 1);
    }

    public static int setDadosParaCache(HttpServletRequest request, HttpServletResponse response, int tempoEmSegundosCache, int eTag) {

	String etagResponse = "\""+eTag+"\"";
	response.setHeader("ETag", etagResponse);
	response.setHeader("Vary", "Accept-Encoding");
	response.setHeader("Connection", "keep-alive");
	response.setHeader("Cache-Control", "public, max-age=" + tempoEmSegundosCache);

	// ----------------
	String headerEtagRequest = request.getHeader("If-None-Match");
	if (etagResponse.equals(headerEtagRequest)) {
	    response.setStatus(STATUS_RESPONSE_NOT_MODIFIED);
	    return STATUS_RESPONSE_NOT_MODIFIED;
	} else {
	    return STATUS_RESPONSE_NORMAL;
	}
    }
    
    public static void enviarJSONDeepSerializedParaOutputStream(HttpServletResponse response, Object object) throws Exception {
	enviarJSONDeepSerializedParaOutputStream(response, object, jsonSerializer);
    }

    public static void enviarJSONDeepSerializedParaOutputStream(HttpServletResponse response, Object object, JSONSerializer json) throws Exception {
	
	String asJson = json.deepSerialize(object);
	enviarTextoParaOutputStream(response, asJson);
    }
    
    public static void enviarJSONParaOutputStream(HttpServletResponse response, Object object) throws Exception {
	enviarJSONParaOutputStream(response, object, jsonSerializer);
    }

    public static void enviarJSONParaOutputStream(HttpServletResponse response, Object object, JSONSerializer json) throws Exception {
	
	String asJson = json.serialize(object);
	enviarTextoParaOutputStream(response, asJson);
    }
    
    /*public static void enviarTextoParaOutputStream(HttpServletResponse response, Object string) throws Exception {
	
	PrintWriter writer = response.getWriter();
	writer.write(string != null ? string.toString() : string + "");
	writer.flush();
	writer.close();
    }*/

    public static void enviarTextoParaOutputStream(HttpServletResponse response, Object texto) throws Exception {
	enviarTextoParaOutputStream(response, null, texto);
    }
    
    public static void enviarTextoParaOutputStream(HttpServletResponse response, String mimeType, Object string) throws Exception {
	
	if (mimeType != null) {
	    response.setContentType(mimeType);
	}
	
	PrintWriter writer = response.getWriter();
	writer.write(string != null ? string.toString() : string + "");
	writer.flush();
	writer.close();
    }
    
    public static void enviarBytesParaOutputStream(HttpServletResponse response, String mimeType, byte[] bytes) throws Exception {
	
	if (mimeType != null) {
	    response.setContentType(mimeType);
	}
	
	ServletOutputStream outputStream = response.getOutputStream();
	outputStream.write(bytes);
	outputStream.flush();
	outputStream.close();
    }
    
    public static String converterTextoParaHTML(String texto) {
	
	if (texto == null) {
	    return "";
	}
	
	String replaceAll = texto.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<BR />");
	return replaceAll;
    }
    
    public static void apagarTodosArquivosDentroDaPasta(File file) {
	
	if (file.isFile()) {
	    file.delete();
	} else if (file.isDirectory()) {
	    for (File arquivoParaApagar : file.listFiles()) {
		if (arquivoParaApagar.isDirectory()) {
		    apagarPastaETodosArquivosDentro(arquivoParaApagar);
		} else {
		    arquivoParaApagar.delete();
		}
	    }
	}
    }

    private static void apagarPastaETodosArquivosDentro(File pastaParaApagar) {
	
	for (File arquivoParaApagar : pastaParaApagar.listFiles()) {
	    if (arquivoParaApagar.isDirectory()) {
		apagarPastaETodosArquivosDentro(arquivoParaApagar);
	    } else {
		arquivoParaApagar.delete();
	    }
	}
	
	pastaParaApagar.delete();
    }
    
    public static String getConteudoDoArquivo(File file) {

	try {
	    FileInputStream is = new FileInputStream(file);
	    byte[] b = new byte[is.available()];
	    is.read(b);
	    is.close();
	    
	    return new String(b);
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    //e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static void setCookie(HttpServletResponse response, String name, String value, String domain, String path, int maxAgeEmSegundos) {
	if (value == null) {
	    value = "";
	}
	Cookie cookie = new Cookie(name, value);
	cookie.setMaxAge(maxAgeEmSegundos);
	if (domain != null && !"".equals(domain)) {
	    cookie.setDomain(domain);
	}
	if (path != null && !"".equals(path)) {
	    cookie.setPath(path);
	}
	response.addCookie(cookie);
    }
    
    public static void removeCookie(HttpServletResponse response, String name, String path) {
	
	Cookie cookie = new Cookie(name, null);
	cookie.setMaxAge(0);
	cookie.setPath(path);
	response.addCookie(cookie);
    }

    public static Cookie getCookie(HttpServletRequest request, String name) {
	Cookie cookies[] = request.getCookies();
	// Return null if there are no cookies or the name is invalid.
	if (cookies == null || name == null || name.length() == 0) {
	    return null;
	}
	// Otherwise, we do a linear scan for the cookie.
	Cookie cookie = null;
	for (int i = 0; i < cookies.length; i++) {
	    // If the current cookie name matches the one we're looking for,
	    // we've
	    // found a matching cookie.
	    if (cookies[i].getName().equals(name)) {
		cookie = cookies[i];
		// The best matching cookie will be the one that has the correct
		// domain name. If we've found the cookie with the correct
		// domain name,
		// return it. Otherwise, we'll keep looking for a better match.
		break;
	    }
	}
	return cookie;
    }

    public static String getValorCookie(HttpServletRequest request, String name) {
	Cookie cookie = getCookie(request, name);
	return cookie == null ? null : cookie.getValue();
    }
    
    /*public static String escapeTextoParaHTML(String texto, Integer limiteCaracteres, boolean isMostrarEmoticons) {
	ParametrosParaEscapeTexto parametros = new ParametrosParaEscapeTexto();
	parametros.limiteCaracteres = limiteCaracteres;
	parametros.isConverterEmoticons = isMostrarEmoticons;
	return escapeTextoParaHTML(texto, parametros);
    }*/
    
    /*public static String escapeTextoParaHTML(String texto, Integer limiteCaracteres) {
	ParametrosParaEscapeTexto parametros = new ParametrosParaEscapeTexto();
	parametros.limiteCaracteres = limiteCaracteres;
	return escapeTextoParaHTML(texto, parametros);
    }*/ 
    
    public static String simpleEscapeTextoParaHTML(String texto, Integer limiteCaracteres) {
	ParametrosParaEscapeTexto parametros = new ParametrosParaEscapeTexto();
	parametros.limiteCaracteres = limiteCaracteres;
	parametros.isConverterBarraN_PorBR = false;
	parametros.isConverterEmoticons = false;
	parametros.isConverterEmail = false;
	parametros.isConverterLink = false;
	parametros.isInserirWbr = false;
	return escapeTextoParaHTML(texto, parametros);
    } 
    
    /*public static String escapeTextoParaHTML(String texto, boolean emoticons) {
	ParametrosParaEscapeTexto parametros = new ParametrosParaEscapeTexto();
	parametros.isConverterEmoticons = emoticons;
	return escapeTextoParaHTML(texto, parametros);
    }*/ 
    
    public static String escapeTextoParaHTML(String texto) {
	return escapeTextoParaHTML(texto, new ParametrosParaEscapeTexto());
    } 
    
    private static Map<String, Emoticon> mapEmoticons = initMapEmoticons();
    
    
    public static class ParametrosParaEscapeTexto {
	public Integer limiteCaracteres = null;
	public boolean isConverterEmail = true;
	public boolean isReduzirLink = true;
	public boolean isConverterLink = true;
	public boolean isConverterEmoticons = true;
	public boolean isInserirWbr = true;
	public boolean isConverterBarraN_PorBR = true;
    }
    public static String escapeTextoParaHTML(String texto, ParametrosParaEscapeTexto parametros) {
	
	if (texto == null) {
	    return "";
	}
	
	// ----------------
	String textoLimitado;
	if (parametros.limiteCaracteres != null) {
	    textoLimitado = ModelUtils.limitarCaracteresColocandoTresPontosAoFinal(texto, parametros.limiteCaracteres);
	} else {
	    textoLimitado = texto;
	}
	textoLimitado = textoLimitado.replaceAll("\r", "");
	
	// ----------------
	StringBuilder buildTextoAsHTML = new StringBuilder();
	String[] splitQuebraDeLinha = textoLimitado.split("\n");
	for (int iQuebra = 0; iQuebra < splitQuebraDeLinha.length; iQuebra++) {
	    String linha = splitQuebraDeLinha[iQuebra];
	    
	    String[] splitPalavras = linha.split(" ");
	    
	    for (int i = 0; i < splitPalavras.length; i++) {
		String palavraOriginal = splitPalavras[i].trim();
		
		if (i > 0) {
		    buildTextoAsHTML.append(" ");
		}
		
		// ----------------
		String palavraEscapedAsHtml4 = transformarTextEmHtml4(palavraOriginal, parametros.isInserirWbr);
		
		// ------------------
		if (parametros.isConverterLink && isLinkWeb(palavraOriginal)) {
		    
		    String linkTamanhoReduzidoParaExibir = parametros.isReduzirLink ? reduzirTamanhoDoLink(palavraOriginal, 70, 30) : palavraOriginal;
		    String linkTamanhoReduzidoParaExibirAsHtml4 = transformarTextEmHtml4(linkTamanhoReduzidoParaExibir, parametros.isInserirWbr);
		    
		    String linkWeb = toLinkAhref(palavraOriginal);
		    String targetAhref = isDeveRedirecionarParaOutraJanela(linkWeb) ? "target=\"_blank\"" : "";
		    
		    String linkWebAsHTML = "<a href=\""+linkWeb+"\" "+targetAhref+">"+linkTamanhoReduzidoParaExibirAsHtml4+"</a>";

		    buildTextoAsHTML.append(linkWebAsHTML);
		} else if (parametros.isConverterEmail && ModelUtils.isEmailValido(palavraOriginal)) {
		
		    buildTextoAsHTML.append("<span class=\"emailOnMessage\">").append(palavraEscapedAsHtml4).append("</span>");
		    
		} else if (parametros.isConverterEmoticons && mapEmoticons.get(palavraOriginal) != null) { 
		    Emoticon emoticon = mapEmoticons.get(palavraOriginal);
		    buildTextoAsHTML.append(emoticon.getTagHtml());
		} else {
		    buildTextoAsHTML.append(palavraEscapedAsHtml4);
		}
	    }

	    boolean isUltimaLinha = iQuebra == (splitQuebraDeLinha.length-1);
	    boolean existemMaisLinhasParaProcessar = isUltimaLinha == false;
	    if (existemMaisLinhasParaProcessar) {
		if (parametros.isConverterBarraN_PorBR) {
		    buildTextoAsHTML.append("</BR>");
		} else {
		    buildTextoAsHTML.append("\n");
		}
	    }
	}
	
	return buildTextoAsHTML.toString();
    }
    private static String transformarTextEmHtml4(String palavraOriginal, boolean isInserirWbr) {
	
	if (isInserirWbr) {
	    int tamanhoMaximoPalavra = 4;
	    String novaPalavraComTokensWbr = ModelUtils.inserirTokenNaPalavraNaoDeixandoExcederTamanhoMaximo(palavraOriginal, "<wbr>", tamanhoMaximoPalavra);
	    return StringEscapeUtils.escapeHtml4(novaPalavraComTokensWbr).replaceAll("&lt;wbr&gt;", "<wbr>");
	} else {
	    return StringEscapeUtils.escapeHtml4(palavraOriginal);
	}
    }
    private static String reduzirTamanhoDoLink(String link, int tamanhoDaPrimeiraParte, int tamanhoDaSegundaParte) {
	
	int tamanhoMaximo = tamanhoDaPrimeiraParte + tamanhoDaSegundaParte;
	if (link.length() <= tamanhoMaximo) {
	    return link;
	}
	
	String primeiraParte = link.substring(0, tamanhoDaPrimeiraParte);
	String segundaParte = link.substring(link.length() - tamanhoDaSegundaParte);
	String textoResumido = new StringBuilder().append(primeiraParte).append("~").append(segundaParte).toString();
	return textoResumido;
    }
    public static boolean isDeveRedirecionarParaOutraJanela(String linkWeb) {
	String linkWebAsLowercase = linkWeb.toLowerCase();

	boolean isLinkDoTeCurti = linkWebAsLowercase.contains("ss");//linkWebAsLowercase.contains(Config.DNS_TECURTI);
	if (isLinkDoTeCurti) {
	    boolean isRedirecionandoParaVisualizarImagem = linkWebAsLowercase.contains("/view/imagerender");
	    boolean isRedirecionandoParaPaginaDeAdmin = linkWebAsLowercase.contains("/admin/");
	    boolean isVaiRedirecionarParaVisualizarImagemDeHash = linkWeb.contains("#"+HASH_EXIBIR_IMAGEM) || linkWeb.contains("#"+HASH_URL_GENERICA);

	    if (isRedirecionandoParaVisualizarImagem || isRedirecionandoParaPaginaDeAdmin || isVaiRedirecionarParaVisualizarImagemDeHash) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return true;
	}
    }
    
    private static boolean isEmoticon(String palavraOriginal) {
	return mapEmoticons.get(palavraOriginal) != null;
    }
    private static String toLinkAhref(String linkDigitadoPeloUsuario) {
	if (linkDigitadoPeloUsuario.toLowerCase().startsWith("http")) {
	    return linkDigitadoPeloUsuario;
	} else {
	    return "http://"+linkDigitadoPeloUsuario;
	}
    }
    
    public static Map<String, Emoticon> initMapEmoticons() {
	
	// ----------------
	List<Emoticon> listEmoticons = new ArrayList<Emoticon>();
	listEmoticons.add(new Emoticon("emoticon-sorriso", 	  new String[]{":)", ":-)", ":]", "=)",       "(:", "(-:", "[:", "(="}));
	listEmoticons.add(new Emoticon("emoticon-triste", 	  new String[]{":(", ":-(", ":[", "=(",       "):", ")-:", "]:", ")="}));
	listEmoticons.add(new Emoticon("emoticon-lingua",  	  new String[]{":-P", ":P", ":-p", ":p",      ":-P", ":P", ":-p", ":p"}));
	listEmoticons.add(new Emoticon("emoticon-sorrisao",       new String[]{":-D", ":D", "=D"}));
	listEmoticons.add(new Emoticon("emoticon-boquinha-uhhh",  new String[]{"-O", ":O", ":-o", ":o"}));
	listEmoticons.add(new Emoticon("emoticon-bravo",  	  new String[]{">:-(", ">:("}));
	listEmoticons.add(new Emoticon("emoticon-piscadinha",  	  new String[]{";-)", ";)"}));
	listEmoticons.add(new Emoticon("emoticon-tristao",  	  new String[]{":-/", ":/", ":-\\", ":\\"}));
	listEmoticons.add(new Emoticon("emoticon-chorando",  	  new String[]{":'(", ":´("}));
	listEmoticons.add(new Emoticon("emoticon-beijo",  	  new String[]{":-*", ":*"}));
	listEmoticons.add(new Emoticon("emoticon-kiki",  	  new String[]{"^_^", "^-^", "^^"}));
	listEmoticons.add(new Emoticon("emoticon-japones",  	  new String[]{"-_-"}));
	listEmoticons.add(new Emoticon("emoticon-confuso",  	  new String[]{"o.O", "O.o"}));
	listEmoticons.add(new Emoticon("emoticon-japones-doidao", new String[]{">-:O", ">:O", ">-:o", ">:o"}));
	listEmoticons.add(new Emoticon("emoticon-pacman",  	  new String[]{":v"}));
	listEmoticons.add(new Emoticon("emoticon-sorriso-bumbum", new String[]{":3"}));
	listEmoticons.add(new Emoticon("emoticon-oculos",  	  new String[]{"8-)", "8)", "B-)", "B)"}));
	listEmoticons.add(new Emoticon("emoticon-oculos-escuros", new String[]{"8-|", "8|", "B-|", "B|"}));
	listEmoticons.add(new Emoticon("emoticon-angel",  	  new String[]{"O:-)", "O:)"}));
	listEmoticons.add(new Emoticon("emoticon-vergonha",  	  new String[]{":$"}));
	listEmoticons.add(new Emoticon("emoticon-devil",  	  new String[]{"3:-)", "3:)"}));
	listEmoticons.add(new Emoticon("emoticon-coracao",  	  new String[]{"<3", "s2", "S2"}));
	listEmoticons.add(new Emoticon("emoticon-robot",  	  new String[]{":|]"}));
	listEmoticons.add(new Emoticon("emoticon-shark",  	  new String[]{"(^^^)"}));
	listEmoticons.add(new Emoticon("emoticon-pinguim",  	  new String[]{"<(\")"}));
	listEmoticons.add(new Emoticon("emoticon-curtir",  	  new String[]{"(y)", "(Y)"}));
	listEmoticons.add(new Emoticon("emoticon-cocozinhu",  	  new String[]{":poop:", ":lala:"}));
	
	// ----------------
	Map<String, Emoticon> mapEmoticons = new HashMap<String, Emoticon>();
	for (Emoticon emoticon : listEmoticons) {
	    for (String codigo : emoticon.getCodigos()) {
		mapEmoticons.put(codigo, emoticon);
	    }
	}
	
	return mapEmoticons;
    }
    
    public static class Emoticon {
	String[] codigos;
	String classImagem;
	String tagHtml;
	String titleCodigos;
	public Emoticon(String classImagem, String[] codigos) {
	    super();
	    this.codigos = codigos;
	    this.classImagem = classImagem;
	    
	    titleCodigos = toStringArrayTitlesEmoticonParaHtml(codigos);
	    tagHtml = "<span title=\""+titleCodigos+"\" class=\"emoticon "+classImagem+"\"></span>";
	}
	public String getTagHtml() {
	    return tagHtml;
	}
	public String[] getCodigos() {
	    return codigos;
	}
    }
    
/*    public static String escapeTextoParaHTML(String texto) {
	if (texto == null) {
	    return "";
	}
	texto = texto.replaceAll("\r", "");
	texto = ModelUtils.colocarTagEmPalavrasGrandes(texto, "<wbr>", 4);
	texto = StringEscapeUtils.escapeHtml4(texto).replaceAll("&lt;wbr&gt;", "<wbr>");
	texto = texto.replaceAll("\n", "<BR>");
	return texto;
    }
*/    

    public static void aguardarGuardarObjetoNaSessao(HttpSession session, Object objetoParaVerificar, String keySession) {

	try {
	    while (true) {
		Object sessionObject = session.getAttribute(keySession);
		if (sessionObject != null && sessionObject.equals(objetoParaVerificar)) {
		    return;
		} else {
		    logger.log(Level.SEVERE, "Não encontrou valor na sessao para keySession= '"+keySession+ "' e valor= '" + objetoParaVerificar + "'");
		    Thread.sleep(500);
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}	
    }
    public static String adequarTextoParaValorXmlSeo(String texto) {
	
	String novoTexto = texto.toLowerCase();
	novoTexto = substituirCaracteresEspeciais(novoTexto);
	novoTexto = novoTexto.replaceAll(" +","-");
	novoTexto = novoTexto.replaceAll("^-|-$", "");
	novoTexto = StringEscapeUtils.escapeXml(novoTexto);
	
	return novoTexto;
    }

    public static String substituirCaracteresEspeciais(String textoLower) {
	String comAcento = "áàãâäéèêëíìîïóòõôöúùûüç \"'!@#$%¨&*()_+=`´{[}]~^?/:;>.<,|\\";       
	String semAcento = "aaaaaeeeeiiiiooooouuuuc-                                      ";    
	
	for (int i = 0; i < comAcento.length(); i++) {
	    char caracterComAcento = comAcento.charAt(i);
	    char caracterSemAcento = semAcento.charAt(i);
	    textoLower = textoLower.replace(caracterComAcento, caracterSemAcento);
	} 
	
	textoLower = textoLower.replaceAll("[^0-9a-zA-Z]"," ");
	return textoLower;
    }
    
    public static Pattern PATTERN_LINKS_WEB = initPatternLinksWeb();
    private static Pattern initPatternLinksWeb() {
	String patternParaLinkHttp = "http([s]?)://[^ ]+";
	String patternParaLinkWWW = "www.[^ ]+";
	String patternParaLinkPontoCom = "[^ ]+?\\.(com|org|net)[^ ]*";
	return Pattern.compile(patternParaLinkHttp + "|" +patternParaLinkWWW + "|" + patternParaLinkPontoCom, Pattern.CASE_INSENSITIVE);
    }
    
    public static boolean isLinkWeb(String textoParaVerificar) {
	return PATTERN_LINKS_WEB.matcher(textoParaVerificar).matches() && ModelUtils.isEmailValido(textoParaVerificar) == false;
    }
    
    public static List<String> findLinksWeb(String texto) {

	/*
	 * Todas as palavras q começam com http ou https
	 */
	// String patternParaLinkHttp = "[hH][tT][tT][pP]([sS]?)://[^ ]+";
	
	Matcher matcher = PATTERN_LINKS_WEB.matcher(texto);
	
	List<String> links = new ArrayList<String>();
	while (matcher.find()) {
	    links.add(matcher.group());
	}
	
	return links;
    }
    
    /*public static void main(String[] args) {
	
	String texto = "wWw.uol.com.br Olha esse httpvideo https://www.youtube.com/watch?v=-YzDsDMYqdw oi é por "+
		"aqui mesmo e tem esse site aqui tb www.tecurti.CoM www.tecurti.CoM?acao=home" + 
		" o hTTp://felipe.fr http://mari.fr https://felipe.com? tecurti.com?home yahoo.org?home=éporra&noissx";
	
	List<String> links = findLinksWeb(texto);
	for (int i = 0; i < links.size(); i++) {
	    String string = links.get(i);
	    System.err.println((i+1) + "- '" + string + "'");
	}
    }*/
    
    public static String toStringArrayTitlesEmoticonParaHtml(String[] arrayString) {
	
	StringBuilder codigosToString = new StringBuilder("");
	for (int i = 0; i < arrayString.length; i++) {
	    if (i > 0) {
		codigosToString.append("&nbsp;&nbsp;&nbsp;");
	    }
	    codigosToString.append(StringEscapeUtils.escapeHtml4(arrayString[i]));
	}
	
	return codigosToString.toString();
    }
    public static Object getURIComQueryString(HttpServletRequest req) {
	return req.getContextPath() + req.getRequestURI() + (ModelUtils.isEmptyTrim(req.getQueryString()) ? "" : "?"+req.getQueryString());
    }
 

    public static class LinkVideo {
	
	public TipoVideo tipo;
	public String linkAsString;
	public LinkVideo(TipoVideo tipo, String linkAsString) {
	    super();
	    this.tipo = tipo;
	    this.linkAsString = linkAsString;
	}
	
	public String asEmbedHtml() {
	    return tipo.stringToEmbedHtml(linkAsString);
	}
	
	public String extractIdVideo() {
	    return tipo.extractIdVideo(linkAsString);
	}
    }

    public static final int WIDTH_IDADEL_PARA_VIDEO_EMBED = 500;
    public enum TipoVideo {
	YOUTUBE {
	    @Override
	    public String stringToEmbedHtml(String linkAsString) {
		String idVideo = extractIdVideo(linkAsString);
		if (idVideo == null) {
		    return null;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<div style=\"width: 100%; max-width: "+WIDTH_IDADEL_PARA_VIDEO_EMBED+";\">");
		builder.append(	    "<div style=\"width: 100%; height: 0; padding-bottom: 65%; position: relative;\">");
		builder.append(           "<iframe style=\"width: 100%; height: 100%; position: absolute;\" title=\"YouTube video player\" class=\"youtube-player containerDeVideo\" type=\"text/html\" src=\"https://www.youtube.com/embed/"+idVideo+"\" frameborder=\"0\" allowFullScreen></iframe>");
		builder.append(	    "</div>");
		builder.append("</div>");
		
		return builder.toString();
	    }

	    @Override
	    public String extractIdVideo(String link) {
		
		String tokenWatch = "/watch?v=";
		int indexWatch = link.toLowerCase().indexOf(tokenWatch);
		if (indexWatch == -1) {
		    return null;
		}
		
		int startIndexOfId = indexWatch + tokenWatch.length();
		String id = link.substring(startIndexOfId);
		
		int indexOf_Ecomercial = id.indexOf("&");
		if (indexOf_Ecomercial != -1) {
		    id = id.substring(0, indexOf_Ecomercial);
		}
		return ModelUtils.isEmptyTrim(id) ? null : id;
	    }
	}, 
	VIMEO {
	    @Override
	    public String stringToEmbedHtml(String linkAsString) {
		String idVideo = extractIdVideo(linkAsString);
		if (idVideo == null) {
		    return null;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("<div style=\"width: 100%; max-width: "+WIDTH_IDADEL_PARA_VIDEO_EMBED+";\">");
		builder.append(	    "<div style=\"width: 100%; height: 0; padding-bottom: 65%; position: relative;\">");
		builder.append(           "<iframe style=\"width: 100%; height: 100%; position: absolute;\" class=\"containerDeVideo\" src=\"//player.vimeo.com/video/" + idVideo + "\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>");
		builder.append(	    "</div>");
		builder.append("</div>");
		
		return builder.toString();
	    }

	    @Override
	    public String extractIdVideo(String link) {
		
		String tokenBarra = "/";
		int indexBarraFinal = link.toLowerCase().lastIndexOf(tokenBarra);
		if (indexBarraFinal == -1) {
		    return null;
		}
		
		int startIndexOfId = indexBarraFinal + tokenBarra.length();
		String id = link.substring(startIndexOfId);
		
		int indexOf_Ecomercial = id.indexOf("&");
		if (indexOf_Ecomercial != -1) {
		    id = id.substring(0, indexOf_Ecomercial);
		}
		
		return ModelUtils.isEmptyTrim(id) ? null : id;
	    }
	};
	
	public abstract String stringToEmbedHtml(String linkAsString);

	public abstract String extractIdVideo(String linkAsString);
    }
    
    private static Pattern patternYoutube;
    private static Pattern patternVimeo;
    static {
	String umOuMaisCaracteresQueNaoSejamEspaco = "\\S+";
	String youtube = "youtube\\.com"+umOuMaisCaracteresQueNaoSejamEspaco+"watch\\?v=";
	String vimeo = "vimeo\\.com";
	String regexYoutube = umOuMaisCaracteresQueNaoSejamEspaco + youtube + umOuMaisCaracteresQueNaoSejamEspaco;
	String regexVimeo = umOuMaisCaracteresQueNaoSejamEspaco + vimeo + umOuMaisCaracteresQueNaoSejamEspaco;
	
	patternYoutube = Pattern.compile(regexYoutube, Pattern.CASE_INSENSITIVE);
	patternVimeo = Pattern.compile(regexVimeo, Pattern.CASE_INSENSITIVE);
    }
    public static List<LinkVideo> findLinksDeVideo(String texto) {
	
	List<String> linksYoutube = findListLinks(patternYoutube, texto);
	List<String> linksVimeo = findListLinks(patternVimeo, texto);
	
	List<LinkVideo> videos = new ArrayList<>();
	addNaLista(videos, linksYoutube, TipoVideo.YOUTUBE);
	addNaLista(videos, linksVimeo, TipoVideo.VIMEO);
	
	return videos;
	
    }
    private static void addNaLista(List<LinkVideo> linksVimeo, List<String> linksAsString, TipoVideo tipoVideo) {
	for (String link : linksAsString) {
	    linksVimeo.add(new LinkVideo(tipoVideo, link));
	}
    }
    private static List<String> findListLinks(Pattern pattern, String texto) {
	List<String> links = new ArrayList<>();
	if (texto == null) {
	    return links;
	}
	
	Matcher matcher = pattern.matcher(texto);
	while (matcher.find()) {
	    links.add(matcher.group());
	}
	return links;
    }
    
    public static enum HttpMethod {
	GET, POST
    }
    
    private static class ParametroSimplesWebService {
	public String name;
	public String value;
    }
    public static String fazerChamadaWebservice(String url, HttpMethod method, Map<String,Object> params) throws Exception {
	byte[] respostaAsBytes = fazerChamadaWebserviceAsBytes(url, method, params);
	return new String(respostaAsBytes);
    }
    public static Map<String, Object> fazerChamadaWebserviceAsJson(String url, HttpMethod method, Map<String,Object> params) throws Exception {
	String respostaAsStringJson = fazerChamadaWebservice(url, method, params);
	Map<String, Object> mapResposta = mapJsonDeserializer.deserialize(respostaAsStringJson);
	return mapResposta;
    }
    public static List<Map<String, Object>> fazerChamadaWebserviceAsListJson(String url, HttpMethod method, Map<String,Object> params) throws Exception {
	String respostaAsStringJson = fazerChamadaWebservice(url, method, params);
	List<Map<String, Object>> listMapResposta = listMapJsonDeserializer.deserialize(respostaAsStringJson);
	return listMapResposta;
    }
    public static byte[] fazerChamadaWebserviceAsBytes(String url, HttpMethod method, Map<String,Object> params) throws Exception {
	
	int timeout = 120000;
	
	// ----------------
	List<Object> listParametros = new ArrayList<>();
	if (params != null) {
	    for (Map.Entry<String, Object> param : params.entrySet()) {
		Object value = param.getValue();
		if (value == null) {
		    continue;
		}
		if (value instanceof UploadedFile) {
		    listParametros.add(value);
		} else {
		    ParametroSimplesWebService parametro = new ParametroSimplesWebService();
		    parametro.name = URLEncoder.encode(param.getKey(), "UTF-8");
		    parametro.value = URLEncoder.encode(String.valueOf(value), "UTF-8");
		    listParametros.add(parametro);
		}
	    }
	}
	
	// ----------------
	HttpURLConnection conn = null;
	boolean isMultipart = isMultipart(listParametros);
	if (isMultipart) {

            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            
            conn = (HttpURLConnection)new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestMethod(HttpMethod.POST.toString());
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("Connection", "Keep-Alive");
	    conn.setConnectTimeout(timeout);
            
            DataOutputStream request = new DataOutputStream(conn.getOutputStream());
            
            for (Object object : listParametros) {
        	request.writeBytes(crlf + twoHyphens + boundary);
        	
        	if (object instanceof ParametroSimplesWebService) {
        	    ParametroSimplesWebService paramSimples = (ParametroSimplesWebService) object;

        	    request.writeBytes(crlf + "Content-Disposition: form-data; name=\""+paramSimples.name+"\"");
        	    request.writeBytes(crlf + crlf);
        	    request.writeBytes(paramSimples.value);
        	    
		} else {
		    UploadedFile uploadedFile = (UploadedFile) object;
		    String attachmentName = URLEncoder.encode(uploadedFile.nomeParametro, "UTF-8");
		    String attachmentFileName = URLEncoder.encode(uploadedFile.nomeArquivo, "UTF-8");
		    
		    request.writeBytes(crlf + "Content-Disposition: form-data; name=\"" + attachmentName + "\"; filename=\"" + attachmentFileName + "\"");
		    request.writeBytes(crlf + crlf);
		    request.write(uploadedFile.bytes);
		}

            }
            
            request.writeBytes(crlf + twoHyphens + boundary + twoHyphens);
            request.flush();
            request.close();
            
	} else {
	    StringBuilder queryString = new StringBuilder();
	    for (Object object : listParametros) {
		ParametroSimplesWebService param = (ParametroSimplesWebService) object;
		
		if (queryString.length() != 0){
		    queryString.append('&');
		}
		queryString.append(param.name);
		queryString.append('=');
		queryString.append(param.value);
	    }
	    byte[] queryStringAsBytes = queryString.toString().getBytes("UTF-8");
	    
	    // ----------------
	    if (method == HttpMethod.POST) {
		conn = (HttpURLConnection)new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setRequestMethod(method.toString());
		conn.setConnectTimeout(timeout);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(queryStringAsBytes.length));
		conn.getOutputStream().write(queryStringAsBytes);
	    } else {
		String queryStringFinalGET = new String(queryStringAsBytes);
		queryStringFinalGET = ModelUtils.isEmptyTrim(queryStringFinalGET) ? "" : "?" + queryStringFinalGET;
		String urlComQueryString = url + queryStringFinalGET;
		
	        conn = (HttpURLConnection)new URL(urlComQueryString).openConnection();
	        conn.setRequestMethod(method.toString());
	        conn.setConnectTimeout(timeout);
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Cache-Control", "no-cache");
	    }
	}
	
	// ----------------
        /*Reader readerResponse = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	byte[] respostaAsBytes = IOUtils.toByteArray(readerResponse);*/
	byte[] respostaAsBytes = ModelUtils.toByteArray(conn.getInputStream());
	return respostaAsBytes;
    }
    
    public static class ConteudoGcm {
	public List<String> listRegistrationIds = new ArrayList<>();
	public Map<String, Object> dadosParaEnviar = new HashMap<>();
    }
    
    public static <X> List<List<X>> quebrarListsEmVariasSublists(List<X> list, int tamanhoDeCadaSublista) {
	
	List<List<X>> listReturn = new ArrayList<>();
	
	int indice = 0;
	while(true) {
	    int indiceFinalNecessario = indice + tamanhoDeCadaSublista;
	    if (indiceFinalNecessario > list.size()) {
		indiceFinalNecessario = list.size();
	    }
	    
	    List<X> subList = list.subList(indice, indiceFinalNecessario);
	    listReturn.add(subList);
	    
	    indice = indiceFinalNecessario;
	    if (indice >= list.size()) {
		break;
	    }
	}
	
	return listReturn;
    }
    
    public static class AlteracaoRegistrationId {
	public String registrationIdAntes;
	public String registrationIdAtual;
	public AlteracaoRegistrationId(String registrationIdAntes, String registrationIdAtual) {
	    super();
	    this.registrationIdAntes = registrationIdAntes;
	    this.registrationIdAtual = registrationIdAtual;
	}
	@Override
	public String toString() {
	    return "[registrationIdAntes=" + registrationIdAntes + ", registrationIdAtual=" + registrationIdAtual + "]";
	}
    }
    public static class RespostaFazerChamadaGcm {
	public boolean isErro = false;
	
	public List<String> listMulticastId = new ArrayList<String>();
	public int totalSuccess = 0;
	public int totalFailure = 0;
	
	/*
	 * Canonical é quando um registration_id foi alterado por outro
	 */
	public int totalCanonicalIds = 0;
	public List<AlteracaoRegistrationId> listAlteracoesRegistrationId = new ArrayList<>();
	public List<String> listRegistrationIdParaRemover = new ArrayList<>();
	
	public TipoErroCommons tipoErro;
	public int responseCode;
	public String descricaoResposta;
    }
    
    /*
     * https://developers.google.com/cloud-messaging/server-ref#table4
     * https://developers.google.com/cloud-messaging/http
     * http://hmkcode.com/android-google-cloud-messaging-tutorial/
     */
    public static List<RespostaFazerChamadaGcm> fazerChamadaGcm(ConteudoGcm conteudo, String apiKey) throws Exception {
	List<RespostaFazerChamadaGcm> listRespostas = new ArrayList<>();
	
	Map<String, Object> dadosParaEnviar = conteudo.dadosParaEnviar;
	List<List<String>> sublistasDeRegistrationId = quebrarListsEmVariasSublists(conteudo.listRegistrationIds, 1000);
	for (List<String> listRegistrationId : sublistasDeRegistrationId) {
	    RespostaFazerChamadaGcm resposta = fazerChamadaGcm(dadosParaEnviar, listRegistrationId, apiKey);
	    listRespostas.add(resposta);
	}
	
	return listRespostas;
    }
    private static RespostaFazerChamadaGcm fazerChamadaGcm(Map<String, Object> dadosParaEnviar, List<String> listRegistrationId, String apiKey) throws Exception {

	// ----------------
	Map<String, Object> mapRequest = new HashMap<>();
	mapRequest.put("data", dadosParaEnviar);
	mapRequest.put("registration_ids", listRegistrationId);
	String dataAsJson = jsonSerializer.deepSerialize(mapRequest);
	byte[] dataJsongAsBytes = dataAsJson.toString().getBytes("UTF-8");

	// ----------------
	URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoOutput(true);
	conn.setConnectTimeout(120000);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Content-Type", "application/json");
	conn.setRequestProperty("Authorization", "key="+apiKey);
	conn.setRequestProperty("Content-Length", String.valueOf(dataJsongAsBytes.length));
	conn.getOutputStream().write(dataJsongAsBytes);

	// ----------------
	Reader readerResponse = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	byte[] respostaAsBytes = IOUtils.toByteArray(readerResponse);
	int responseCode = conn.getResponseCode();
	String respostaAsString = new String(respostaAsBytes);
	
	// ----------------
	RespostaFazerChamadaGcm resposta = new RespostaFazerChamadaGcm();
	
	boolean isSucesso = responseCode == 200;
	resposta.responseCode = responseCode;
	if (isSucesso) {
	    resposta.isErro = false;
	    
	    Map<String, Object> mapResposta = mapJsonDeserializer.deserialize(respostaAsString);
	    resposta.listMulticastId.add(mapResposta.get("multicast_id").toString());
	    resposta.totalSuccess = Integer.parseInt(mapResposta.get("success").toString());
	    resposta.totalFailure = Integer.parseInt(mapResposta.get("failure").toString());
	    resposta.totalCanonicalIds = Integer.parseInt(mapResposta.get("canonical_ids").toString());
	    
	    boolean isTodasAsMensagensEnviadasComSucesso = resposta.totalFailure == 0 && resposta.totalCanonicalIds == 0;
	    if (isTodasAsMensagensEnviadasComSucesso == false) {
		List<Map<String, Object>> results = (List<Map<String, Object>>) mapResposta.get("results");
		for (int i = 0; i < results.size(); i++) {
		    Map<String, Object> r = results.get(i);
		    String message_id = (String) r.get("message_id");
		    String registration_id = (String) r.get("registration_id");
		    String error = (String) r.get("error");
		    
		    boolean deuErro = ModelUtils.isEmptyTrim(message_id);
		    if (deuErro) {
			boolean ehUmErroQuePodeTentarNovamenteMaisTarde = error.equalsIgnoreCase("Unavailable");
			boolean deveRemoverRegistrationId = ehUmErroQuePodeTentarNovamenteMaisTarde == false;
			if (deveRemoverRegistrationId) {
			    String registrationIdEnviado = listRegistrationId.get(i);
			    resposta.listRegistrationIdParaRemover.add(registrationIdEnviado);
			}
		    } else {
			boolean isTrocouRegistrationId = ModelUtils.isNotEmptyTrim(registration_id);
			if (isTrocouRegistrationId) {
			    String registrationIdEnviado = listRegistrationId.get(i);
			    resposta.listAlteracoesRegistrationId.add(new AlteracaoRegistrationId(registrationIdEnviado, registration_id));
			}
		    }
		}
	    }
	    
	    return resposta;
	} else {
	    resposta.isErro = true;
	    resposta.tipoErro = TipoErroCommons.ERRO_ACESSAR_WEBSERVICE;
	    resposta.descricaoResposta = respostaAsString;
	    return resposta;
	}
    }

    private static boolean isMultipart(List listParametros) {
	for (Object object : listParametros) {
	    if (object instanceof UploadedFile) {
		return true;
	    }
	}
	return false;
    }
    public static String getUrlBase(HttpServletRequest request) {

	String scheme = request.getScheme();             // http
	String serverName = request.getServerName();     // hostname.com
	int serverPort = request.getServerPort();        // 80
	String contextPath = request.getContextPath();   // /mywebapp
//	String servletPath = request.getServletPath();   // /servlet/MyServlet
//	String pathInfo = request.getPathInfo();         // /a/b;c=123
//	String queryString = request.getQueryString();          // d=789

	// Reconstruct original requesting URL
	StringBuffer url =  new StringBuffer();
	url.append(scheme).append("://").append(serverName);

	if ((serverPort != 80) && (serverPort != 443)) {
	    url.append(":").append(serverPort);
	}
	
	url.append(contextPath);
	/*
	url.append(servletPath);

	if (pathInfo != null) {
	    url.append(pathInfo);
	}
	if (queryString != null) {
	    url.append("?").append(queryString);
	}
	*/
	return url.toString();
    }
    
    public static byte[] redimensionarImagem(byte[] bytes, Dimension dimension) {
	
	Image imagemOriginal = ImagesServiceFactory.makeImage(bytes);
	
	Transform resized = ImagesServiceFactory.makeResize(dimension.getWidth(), dimension.getHeight());
	Image newImage = ImagesServiceFactory.getImagesService().applyTransform(resized, imagemOriginal);
	byte[] newImageData = newImage.getImageData();
	return newImageData;
    }
    
    public static boolean isArquivoAnexoDoTipo(String nomeArquivo, String... tiposAceitos) {
	
	String contentTypeUpper = nomeArquivo.toUpperCase();
	for (String tipo : tiposAceitos) {
	    if (contentTypeUpper.endsWith("."+tipo.toUpperCase())) {
		return true;
	    }
	}
	
	return false;
    }
    
    public static Map<String, Object> criarMapComParametrosDoRequestVerificandoTipoDoConteudo(HttpServletRequest request) throws Exception {
	Map<String, Object> parametros = new HashMap<String, Object>();
	popularObjetoComParametrosDoRequestVerificandoTipoDoConteudo(parametros, request);
	return parametros;
    }
    public static void popularObjetoComParametrosDoRequestVerificandoTipoDoConteudo(Object object, HttpServletRequest request) throws Exception {
	if (ServletFileUpload.isMultipartContent(request)) {
	    WebUtils.popularObjectComParametersMultipartFormData(object, request);
	} else {
	    WebUtils.popularObjectComParameters(object, request);
	}	
    }
    
    /*
     * http://stackoverflow.com/questions/25148567/list-of-all-the-app-engine-images-service-get-serving-url-uri-options
     * SIZE / CROP
     * 
     * s640 — generates image 640 pixels on largest dimension
     * s0 — original size image
     * w100 — generates image 100 pixels wide
     * h100 — generates image 100 pixels tall
     * p — smart square crop, attempts cropping to faces
     * pp — alternate smart square crop, does not cut off faces (?)
     * cc — generates a circularly cropped image
     * ci — square crop to smallest of: width, height, or specified =s parameter
     * ROTATION
     * 
     * fv — flip vertically
     * fh — flip horizontally
     * r90 — rotate 90 degrees (or 180 or 270)
     * IMAGE FORMAT
     * 
     * rj — forces the resulting image to be JPG
     * rp — forces the resulting image to be PNG
     * rw — forces the resulting image to be WebP
     * rg — forces the resulting image to be GIF
     * Forcing PNG, WebP and GIF outputs can work in combination with circular crops for a transparant background. Forcing JPG can be combined with border color to fill in backgrounds in transparent images.
     * 
     * ANIMATED GIFs
     * 
     * rh — generates an MP4 from the input image
     * k — kill animation (generates static image)
     * MISC.
     * 
     * b10 — add a 10px border to image
     * c0xAARRGGBB — set border color, eg. =c0xffff0000 for red
     * d — adds header to cause browser download
     * e7 — set cache-control max-age header on response to 7 days
     * l100 — sets JPEG quality to 100% (1-100)
     * Filters
     * 
     * fSoften=1,100,0: - where 100 can go from 0 to 100 to blur the image
     * fVignette=1,100,1.4,0,000000 where 100 controls the size of the gradient and 000000 is RRGGBB of the color of the border shadow
     * Caveats
     * 
     * Some options (like =l for JPEG quality) do not seem to generate new images. If you change another option (size, etc.) and change the l value, the quality change should be visible. Some options also don't work well together. This is all undocumented by Google, probably with good reason.
     * 
     * Moreover, it's probably not a good idea to depend on any of these options existing forever. Google could remove most of them without notice at any time.
     */
    public static String appendParametrosGcsParaUrl(String url, int width, int height) {
	boolean isQuadrada = width == height;
	if (isQuadrada) {
	    return url + "=s"+width+"-c";
	} else {
	    return url + "=-w"+width+"-h"+height;
	}
    }
}




















