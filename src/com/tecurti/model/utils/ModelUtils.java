package com.tecurti.model.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;

import com.tecurti.model.entidades.Tempo;
import com.tecurti.model.entidades.UnidadeTempo;
import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;

public class ModelUtils {

    private static Logger logger = Logger.getLogger(ModelUtils.class.getName());

    public static TimeZone GMT_SAO_PAULO = TimeZone.getTimeZone("America/Sao_Paulo");


    public static Tempo getDataAmigavel(Calendar dataDoEvento) {
	GregorianCalendar dataAgora = new GregorianCalendar();
	return calcularTempoEntreDatas(dataDoEvento, dataAgora);
    }

    public static void removerElementosNull(List list) {
	for (int i = list.size() - 1; i >= 0; i--) {
	    if (list.get(i) == null) {
		list.remove(i);
	    }
	}
    }
    
    public static double regraDe3(double itemBase, double valorItemBase, double itemParaDescobriValor) {
	
	BigDecimal itemBaseBigDecimal = new BigDecimal(itemBase).setScale(2, RoundingMode.HALF_UP);
	BigDecimal valorItemBaseBigDecimal = new BigDecimal(valorItemBase).setScale(2, RoundingMode.HALF_UP);
	BigDecimal itemParaDescobriValorBigDecimal = new BigDecimal(itemParaDescobriValor).setScale(2, RoundingMode.HALF_UP);
	
	/*
	 * itemBase >>>>> valorItemBase
	 * itemParaDescobriValor >>>>> x 
	 * 
	 * x * itemBase = valorItemBase * itemParaDescobriValor 
	 * x = (valorItemBase * itemParaDescobriValor) / itemBase
	 */
	return valorItemBaseBigDecimal.multiply(itemParaDescobriValorBigDecimal).setScale(2, RoundingMode.HALF_UP).divide(itemBaseBigDecimal, 2, RoundingMode.HALF_UP).doubleValue();
	//return (valorItemBase * itemParaDescobriValor) / itemBase;
    }
    
    public static int converterValorMonetarioDeDecimalParaCentavos(BigDecimal valorEmDecimais) {
	BigDecimal valorCom2CasasDecimais = valorEmDecimais.setScale(2, RoundingMode.HALF_UP);
	BigDecimal valorCentavos = valorCom2CasasDecimais.multiply(new BigDecimal(100));
	return valorCentavos.intValue();
    }
    public static void outprintMap(Map map) {
	outprintMap("map", map);
    }
    public static void outprintMap(String nomeMap, Map map) {
	MapUtils.debugPrint(System.out, nomeMap, map);
    }
    public static Tempo calcularTempoEntreDatas(Calendar dataInicio, Calendar dataFim) {

	Date dataIniciaoAsDate = dataInicio.getTime();
	Date dataFimAsDate  = dataFim.getTime();
	long difMilli = dataFimAsDate.getTime() - dataIniciaoAsDate.getTime();

	// ----------------
	Tempo tempo = new Tempo();

	// ----------------
	long daysTotal    = TimeUnit.MILLISECONDS.toDays   (difMilli);
	int totalDiasUmAno = 365;
	boolean passouMaisDeUmAno = daysTotal >= totalDiasUmAno;
	if (passouMaisDeUmAno) { 

	    tempo.setTempo(daysTotal/totalDiasUmAno);
	    tempo.setUnidade(UnidadeTempo.ANOS);

	} else {

	    int totalDiasUmMes = 30;
	    boolean passouMaisDeUmMes = daysTotal >= totalDiasUmMes;
	    if (passouMaisDeUmMes) {
		tempo.setTempo(daysTotal/totalDiasUmMes);
		tempo.setUnidade(UnidadeTempo.MESES);  
	    } else {

		int totalDiasUmaSemana = 7;
		boolean passouMaisDeUmaSemana = daysTotal >= totalDiasUmaSemana;
		if (passouMaisDeUmaSemana) {
		    tempo.setTempo(Math.round(daysTotal/totalDiasUmaSemana));
		    tempo.setUnidade(UnidadeTempo.SEMANAS);
		} else {

		    boolean passouMaisDeUmDia = daysTotal >= 1;
		    if (passouMaisDeUmDia) {
			tempo.setTempo(daysTotal);
			tempo.setUnidade(UnidadeTempo.DIAS);
		    } else {

			long hoursTotal   = TimeUnit.MILLISECONDS.toHours(difMilli);
			boolean passouMaisDeUmaHora = hoursTotal >= 1;
			if (passouMaisDeUmaHora) {
			    tempo.setTempo(hoursTotal);
			    tempo.setUnidade(UnidadeTempo.HORAS);
			} else {

			    long minutesTotal = TimeUnit.MILLISECONDS.toMinutes(difMilli);
			    boolean passouMaisDeUmMinuto = minutesTotal >= 1;
			    if (passouMaisDeUmMinuto) {
				tempo.setTempo(minutesTotal);
				tempo.setUnidade(UnidadeTempo.MINUTOS);
			    } else {

				long secondsTotal = TimeUnit.MILLISECONDS.toSeconds(difMilli);
				tempo.setTempo(secondsTotal);
				tempo.setUnidade(UnidadeTempo.SEGUNDOS);
			    }
			}
		    }
		}

	    }
	}

	return tempo;
    }

    public static String dateToStr(Calendar date, i18nUsandoApiPadrao idioma) {
	return dateToStr(date, idioma, null);
    }
    public static String dateToStr(Calendar date, i18nUsandoApiPadrao idioma, TimeZone timeZone) {

	if (date == null || date.equals("")) {
	    return "";
	} 

	String dateFormat = idioma.geti18nTexto("java.simpleDateFormat.DDMMYYYY");
	return ModelUtils.dateToStr(date, dateFormat, timeZone);
    }    

    public static String dateTimeToStr(Date date, i18nUsandoApiPadrao idioma) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	
	return dateTimeToStr(calendar, idioma);
    }
    public static String dateTimeToStr(Calendar date, i18nUsandoApiPadrao idioma) {
	return dateTimeToStr(date, idioma, null);
    }

    public static String dateTimeToStr(Calendar date, i18nUsandoApiPadrao idioma, TimeZone timeZone) {
	String dateFormat = idioma.geti18nTexto("java.simpleDateFormat.DDMMYYYY_HHMMSS");
	return ModelUtils.dateToStr(date, dateFormat, timeZone);
    }   

    public static boolean isValidDate(String strDate, i18nUsandoApiPadrao idioma) {

	String dateFormat = idioma.geti18nTexto("java.simpleDateFormat.DDMMYYYY");
	return ModelUtils.isValidDate(strDate, dateFormat);

    }

    public static Calendar strToDate(String strDate, i18nUsandoApiPadrao idioma) {
	String dateFormat = idioma.geti18nTexto("java.simpleDateFormat.DDMMYYYY");
	return ModelUtils.strToDate(strDate, dateFormat, GMT_SAO_PAULO);
    }  

    public static String encrypt(String string) {
	if (isEmptyTrim(string)) {
	    return "";
	}
	return DigestUtils.sha256Hex(string);
    }

    public static boolean isEmptyTrim(String valor) {
	return valor == null || valor.trim().equals("");
    }

    public static boolean isEmpty(String valor) {
	return valor == null || valor.equals("");
    }

    public static DecimalFormat df00 = new DecimalFormat("00");
    public static TimeZone createTimeZoneFromJavascriptTimezoneDateOffset(String strFromJavaScript) {

	String gmtSinal;
	String gmtHora;
	String gmtMinutos;

	double timeZoneJsAsDoubleComSinal = Double.parseDouble(strFromJavaScript);  
	gmtSinal = timeZoneJsAsDoubleComSinal >= 0 ? "+" : "-";

	double timeZoneJsAsDoubleSemSinal = Math.abs(timeZoneJsAsDoubleComSinal);  
	int timeZoneJsAsIntSemSinal = (int) Math.abs(timeZoneJsAsDoubleSemSinal);
	gmtHora = df00.format(timeZoneJsAsIntSemSinal);

	double valorQuebradoJs = timeZoneJsAsDoubleSemSinal - timeZoneJsAsIntSemSinal;
	double percentualDeMinutos = valorQuebradoJs;
	int totalDeMinutosEmUmaHora = 60;
	int valoQuebradoRelogio = (int) (percentualDeMinutos * totalDeMinutosEmUmaHora);
	gmtMinutos = df00.format(valoQuebradoRelogio);

	String GMT = "GMT" + gmtSinal + gmtHora + ":" + gmtMinutos;
	TimeZone tz = TimeZone.getTimeZone(GMT);  
	return tz;
    }

    private static Pattern patternValidacaoEmail = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$"); 
    public static boolean isEmailValido(String email) {
	if (ModelUtils.isEmptyTrim(email)) {
	    return false;
	}
	Matcher matcher = patternValidacaoEmail.matcher(email); 
	return matcher.find();
    }

    public static boolean isEmailInvalido(String email) {
	return isEmailValido(email) == false;
    }

    public static void aumentarTamanhoDaListaCasoNecessario(List listOriginal, int tamanhoPretendido) {

	if (listOriginal.isEmpty() || listOriginal.size() >= tamanhoPretendido) {
	    return;
	}

	int totalRestante = tamanhoPretendido - listOriginal.size();
	List listaRestantes = new ArrayList(totalRestante);

	int indiceListaOriginal = 0;
	for (int i = 0; i < totalRestante; i++) {
	    listaRestantes.add(listOriginal.get(indiceListaOriginal++));
	    if (indiceListaOriginal >= listOriginal.size()) {
		indiceListaOriginal = 0;
	    }
	}

	listOriginal.addAll(listaRestantes);
    }

    public static void aumentarTamanhoDaListaParaEsquerdaCasoNecessario(List listOriginal, int tamanhoPretendido) {

	if (listOriginal.isEmpty() || listOriginal.size() >= tamanhoPretendido) {
	    return;
	}

	int totalRestante = tamanhoPretendido - listOriginal.size();
	List listaRestantes = new ArrayList(totalRestante);

	int indiceListaOriginal = listOriginal.size()-1;
	for (int i = 0; i < totalRestante; i++) {
	    listaRestantes.add(0, listOriginal.get(indiceListaOriginal--));
	    if (indiceListaOriginal < 0) {
		indiceListaOriginal = listOriginal.size()-1;
	    }
	}

	listOriginal.addAll(0, listaRestantes);
    }

    public static <T> List<T> slice(List<T> list, int index, int count) {
	List<T> result = new ArrayList<T>();
	if (index >= 0 && index < list.size()) {
	    int end = index + count < list.size() ? index + count : list.size();
	    for (int i = index; i < end; i++) {
		result.add(list.get(i));
	    }
	}
	return result;
    }

    public static Calendar criarCalendar(Long dataMillis) {

	if (dataMillis == null) {
	    return null;
	}

	Calendar data = new GregorianCalendar();
	data.setTimeInMillis(dataMillis);
	return data;
    }

    public static boolean isArquivoDoTipoImagem(String nome) {

	// ----------------
	String[] extensoes = {".PNG", ".JPG", ".JPEG", ".GIF"};

	// ----------------
	for (String extensao : extensoes) {
	    if (nome.toUpperCase().endsWith(extensao)) {
		return true;
	    }
	}

	// ----------------
	return false;
    }

    public static byte[] getFileAsBytes(File file) {

	try {
	    FileInputStream is = new FileInputStream(file);
	    byte[] b = new byte[is.available()];
	    is.read(b);
	    is.close();

	    return b;
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static String getConteudoDoArquivo(File file) {
	return new String(getFileAsBytes(file));
    }

    public static void copiarArquivo(File de, File para) throws Exception {
	/*FileInputStream is = new FileInputStream(de);
	FileOutputStream out = new FileOutputStream(para);


	byte[] b = new byte[1024];
	while (true) {
	    int totalLido = is.read(b);

	    if (totalLido == -1) {
		break;
	    } else {
		out.write(b, 0, totalLido);
		out.flush();
	    }
	}

	out.flush();
	out.close();

	is.close();*/
    }

    public static boolean isNotEmpty(String nome) {
	return !isEmpty(nome);
    }

    public static boolean isNotEmptyTrim(String nome) {
	return !isEmptyTrim(nome);
    }

    public static String colocarTagEmPalavrasGrandes(String texto, String token, int tamanhoMaximo) {

	String[] split = texto.split(" ");
	StringBuilder builder = new StringBuilder();

	for (int i = 0; i < split.length; i++) {
	    String palavra = split[i];

	    if (i > 0) {
		builder.append(" ");
	    }

	    if (palavra.length() > tamanhoMaximo) {
		String novaPalavra = inserirTokenNaPalavraNaoDeixandoExcederTamanhoMaximo(palavra, token, tamanhoMaximo);
		builder.append(novaPalavra);
	    } else {
		builder.append(palavra);
	    }

	}

	return builder.toString();
    }

    public static String inserirTokenNaPalavraNaoDeixandoExcederTamanhoMaximo(String palavra, String token, int tamanhoMaximo) {

	if (palavra.length() <= tamanhoMaximo) {
	    return palavra;
	}

	StringBuilder palavraBuilder = new StringBuilder(palavra);

	int indice = 0;
	while (true) {
	    indice += tamanhoMaximo;

	    int ultimoIndice = palavraBuilder.length() - 1;
	    if (indice <= ultimoIndice) {
		palavraBuilder.insert(indice, token);
		indice += token.length();
	    } else {
		break;
	    }
	}

	return palavraBuilder.toString();
    }

    public static boolean isValidDate(String strDate, String dateFormat) {
	boolean isValid = false;

	try {
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    sdf.setLenient(false);
	    Date date = sdf.parse(strDate);
	    isValid = true;
	} catch (ParseException ex) {
	    isValid = false;
	} catch (IllegalArgumentException ex) {
	    isValid = false;
	}

	return isValid;
    }


    public static Calendar strToDate(String strDate, String dateFormat) {
	return strToDate(strDate, dateFormat, GMT_SAO_PAULO);
    }
    public static Calendar strToDate(String strDate, String dateFormat, TimeZone timezone) {

	Calendar cal = null;

	try {

	    DateFormat formatter = new SimpleDateFormat(dateFormat);
	    Date date = (Date)formatter.parse(strDate);

	    cal = Calendar.getInstance();
	    cal.setTime(date);

	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	return cal;
    }    

    public static String dateToStr(Calendar date, String dateFormat) {
	return dateToStr(date, dateFormat, null);
    }

    public static String dateToStr(Calendar date, String dateFormat, TimeZone timeZone) {

	String result = "";
	if (date == null) {
	    return result;
	}

	try {

	    SimpleDateFormat out = new SimpleDateFormat(dateFormat);  
	    if (timeZone != null) {
		out.setTimeZone(timeZone);
	    }
	    result = out.format(date.getTime());  

	} catch (IllegalArgumentException ex) {

	}

	return result;
    }

    public static Calendar getDataHoraAgora() {
	Calendar date = Calendar.getInstance();
	return date;
    }

    public static Calendar getDataHoje() {
	return getDataHoje(null);
    }
    public static Calendar getDataHojeSaoPauloBrasil() {
	return getDataHoje(GMT_SAO_PAULO);
    }
    public static Calendar getDataHoje(TimeZone timeZone) {
	Calendar date = timeZone == null ? Calendar.getInstance() : Calendar.getInstance(timeZone);
	GregorianCalendar data = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
	return data;
    }

    public static String trunc(String string, int maximoDeCaracteres) {

	if (string == null) {
	    return string;
	}

	if (string.length() > maximoDeCaracteres) {
	    return string.substring(0, maximoDeCaracteres);
	}

	return string;
    }

    public static String removeEnters(String string) {

	if (string == null) {
	    return string;
	}

	return string.replaceAll("(\\r|\\n)", "");
    }

    public static String limitarCaracteresColocandoTresPontosAoFinal(String string, Integer maximoCaracteres) {

	if (string == null || maximoCaracteres == null) {
	    return string;
	}

	if (string.length() > maximoCaracteres) {
	    return string.substring(0, maximoCaracteres) + "...";
	} else {
	    return string;
	}
    }

    public static boolean podeConverterParaLong(String id) {

	try {
	    Long.parseLong(id);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static String trim(Object valor) {
	if (valor == null) {
	    return "";
	}
	return valor.toString().trim();
    }

    public static Calendar getDataDeAnosAtras(int anosAtras) {
	Calendar dataXAnosAtras = ModelUtils.getDataHoje();
	dataXAnosAtras.add(Calendar.YEAR, anosAtras*-1);
	return dataXAnosAtras;
    }

    public static String toLowerCase(String texto) {
	if (texto == null) {
	    return null;
	}
	return texto.toLowerCase();
    }

    public static boolean equalsIn(Object valor, Object... valoresParaComparar) {
	for (Object valorComp : valoresParaComparar) {
	    if (valor == null) {
		if (valorComp == null) {
		    return true;
		}
	    } else {
		if (valor.equals(valorComp)) {
		    return true;
		}
	    }
	}
	return false;
    }

    public static boolean isCampoNaoPreenchidoFromHtml(String valor) {
	return isEmptyTrimOr(valor, "null", "undefined");
    }

    public static boolean isEmptyTrimOr(String valor, String... valoresParaComparar) {
	return isEmptyTrim(valor) || equalsIn(valor, valoresParaComparar);
    }

    public static byte[] getResourseAsStreamAsBytes(String pathArquivo) {
	try {
	    InputStream is = ModelUtils.class.getResourceAsStream(pathArquivo);
	    byte[] bytes = new byte[is.available()];
	    is.read(bytes);
	    return bytes;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    public static String getResourseAsStreamAsString(String pathArquivo) {
	return new String(getResourseAsStreamAsBytes(pathArquivo));
    }

    public static Calendar getCalendarFromDataMilli(Long dataMilli) {
	if (dataMilli == null) {
	    return null;
	}
	Calendar calendar = new GregorianCalendar();
	calendar.setTimeInMillis(dataMilli);
	return calendar;
    }

    public static byte[] compactarBytes(byte[] bytes) {
	try {
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);
	    zipOut.setLevel(Deflater.BEST_COMPRESSION);
	    zipOut.putNextEntry(new ZipEntry("arquivoUnico"));
	    zipOut.write(bytes);
	    zipOut.close();
	    return byteArrayOutputStream.toByteArray();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static byte[] descompactarBytes(byte[] bytes) {
	try {
	    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes));
	    ZipEntry entry = zipInputStream.getNextEntry();

	    byte[] buffer = new byte[2048];
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    int len = 0;
	    while ((len = zipInputStream.read(buffer)) > 0) {
		output.write(buffer, 0, len);
	    }

	    zipInputStream.close();
	    return output.toByteArray();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static <T> List<T> mesclarArraysSemRepetirValor(List<T> array1, List<T> array2) {

	if (array1 == null && array2 == null) {
	    return null;
	} else {
	    if (array1 == null) {
		array1 = new ArrayList<>();
	    } 
	    if (array2 == null) {
		array2 = new ArrayList<>();
	    } 
	}

	List<T> listResultado = new ArrayList<>();
	listResultado.addAll(array1);
	for (T valor2 : array2) {
	    if (listResultado.contains(valor2)) {
		continue;
	    } else {
		listResultado.add(valor2);
	    }
	}

	return listResultado;
    }

    public static boolean isEnumValido(Class enumClass, String valor) {
	if (isEmpty(valor)) {
	    return false;
	}
	for (Object constanteEnumerado : enumClass.getEnumConstants()) {
	    if (constanteEnumerado.toString().equals(valor)) {
		return true;
	    }
	}

	return false;
    }

    public static boolean isValidInteger(String string) {
	try {
	    Integer.parseInt(string);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }
    
    public static boolean isValidDouble(String string) {
	try {
	    Double.parseDouble(string);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static boolean isValidLong(String string) {
	try {
	    Long.parseLong(string);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static boolean isValidIntegers(String[] inteirosAsString) {
	for (String string : inteirosAsString) {
	    if (ModelUtils.isValidInteger(string) == false) {
		return false;
	    }
	}
	return true;
    }

    public static Long[] asLongArray(String[] arrayString) {
	if (arrayString == null) {
	    return null;
	}
	Long[] arrayInt = new Long[arrayString.length];
	for (int i = 0; i < arrayString.length; i++) {
	    arrayInt[i] = Long.parseLong(arrayString[i]);
	}
	return arrayInt;
    }

    public static boolean in(Object objetoParaComparar, List listObjects) {
	for (Object object : listObjects) {
	    if (object.equals(objetoParaComparar)) {
		return true;
	    }
	}
	return false;
    }

    public static boolean equals(List list1, List list2) {
	if (list1.size() != list2.size()) {
	    return false;
	}

	for (int i = 0; i < list1.size(); i++) {
	    boolean valorDiferentes = list1.get(i).equals(list2.get(i)) == false;
	    if (valorDiferentes) {
		return false;
	    }
	}
	return true;
    }

    public static void truncarListaSeNecessario(List list, int max) {
	if (list.size() >= max) {
	    for (int i = list.size()-1; i >= 0; i--) {
		list.remove(i);
		if (list.size() == max) {
		    return;
		}
	    }
	}
    }

    public static String getStackTrace(Throwable throwable, int maxLength) {
	String stacktrace = getStackTrace(throwable); 
	if (stacktrace.length() <= maxLength) {
	    return stacktrace;
	} else {
	    return stacktrace.substring(0, maxLength);
	}
    }

    public static String getStackTrace(Throwable throwable) {
	ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	PrintStream printstream = new PrintStream(bytearrayoutputstream);
	throwable.printStackTrace(printstream);
	return bytearrayoutputstream.toString();
    }

    public static boolean isCnpjValido(String cnpj){
	String cnpjFormatado = cnpj.replace(".", "").replace("-", "").replace("/", "");
	if(cnpjFormatado == null || cnpjFormatado.equals("")||cnpjFormatado.length()<14){
	    return false;
	}else{
	    try{
		String cnpjSemDigito = cnpjFormatado.substring(0,12);
		int digitoVerificador = 0;
		int digitoVerificador2 = 0;
		int somaCnpj = 0;
		int peso =5;
		for(int pos = 0;pos<cnpjSemDigito.length();pos++){
		    if(peso < 2){
			peso = 9;
		    }
		    int digitoAtual= Integer.parseInt(cnpjSemDigito.substring(pos,pos+1)) * peso;
		    peso--;

		    somaCnpj = somaCnpj + digitoAtual; 
		}
		digitoVerificador = somaCnpj %11;
		if(digitoVerificador < 2){
		    digitoVerificador = 0;
		}else{
		    digitoVerificador = 11 - digitoVerificador;
		}

		String cnpjComPrimeiroDigito = cnpjSemDigito + digitoVerificador;
		somaCnpj = 0;
		peso = 6;
		for(int pos = 0;pos<cnpjComPrimeiroDigito.length();pos++){
		    if(peso < 2){
			peso = 9;
		    }
		    int digitoAtual= Integer.parseInt(cnpjComPrimeiroDigito.substring(pos,pos+1)) * peso;
		    peso--;
		    somaCnpj = somaCnpj + digitoAtual; 
		}

		digitoVerificador2 = somaCnpj %11;
		if(digitoVerificador2 < 2){
		    digitoVerificador2 = 0;
		}else{
		    digitoVerificador2 = 11 - digitoVerificador2;
		}
		String cnpjCompleto = cnpjComPrimeiroDigito+digitoVerificador2;

		if(!cnpjFormatado.equals(cnpjCompleto)){
		    return false;
		}
	    }catch(NumberFormatException e){
		return false;
	    }
	}
	return true;
    }
    public static boolean isCpfValido(String cpf){
	String cpfFormatado = cpf.replace(".", "").replace("-", "");
	if(cpfFormatado == null || cpfFormatado.equals("")||cpfFormatado.length()<11){
	    return false;
	}else{
	    try{
		String cpfSemDigito = cpfFormatado.substring(0,9);
		int digitoVerificador = 0;
		int digitoVerificador2 = 0;
		int somaCpf = 0;
		int peso =10;
		for(int pos = 0;pos<cpfSemDigito.length();pos++){
		    int digitoAtual= Integer.parseInt(cpfSemDigito.substring(pos,pos+1)) * peso;
		    peso--;
		    somaCpf = somaCpf + digitoAtual; 
		}
		digitoVerificador = somaCpf %11;
		if(digitoVerificador < 2){
		    digitoVerificador = 0;
		}else{
		    digitoVerificador = 11 - digitoVerificador;
		}

		String cpfComPrimeiroDigito = cpfSemDigito + digitoVerificador;
		somaCpf = 0;
		peso = 11;
		for(int pos = 0;pos<cpfComPrimeiroDigito.length();pos++){
		    int digitoAtual= Integer.parseInt(cpfComPrimeiroDigito.substring(pos,pos+1)) * peso;
		    peso--;
		    somaCpf = somaCpf + digitoAtual; 
		}

		digitoVerificador2 = somaCpf %11;
		if(digitoVerificador2 < 2){
		    digitoVerificador2 = 0;
		}else{
		    digitoVerificador2 = 11 - digitoVerificador2;
		}
		String cpfCompleto = cpfComPrimeiroDigito+digitoVerificador2;

		if(!cpfFormatado.equals(cpfCompleto)){
		    return false;
		}
	    }catch(NumberFormatException e){
		return false;
	    }
	}
	return true;
    }

    public static String convertStringToUTF8(String stringParaConverter) {
	try {
	    return URLEncoder.encode(stringParaConverter, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    return null;
	}
    }
    
    public static String getPropertieFromResourceAsStream(String pathResource, String key) {

	InputStream inputStream = null;
	try {
	    inputStream = ModelUtils.class.getResourceAsStream(pathResource);

	    Properties properties = new Properties();
	    properties.load(inputStream);

	    return properties.get(key).toString().trim();
	} catch (IOException e) {
	    logger.log(Level.SEVERE, "", e);
	    return null;
	    // e.printStackTrace();
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException e) {
		    logger.log(Level.SEVERE, "", e);
		    // e.printStackTrace();
		    throw new RuntimeException(e);
		}
	    }
	}

    }

    public static BigDecimal arredondarDinheiroParaDuasCasasDecimais(BigDecimal valorRestanteParaPagamento) {
	return valorRestanteParaPagamento.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isNotEmpty(List<Long> list) {
	return !isEmpty(list);
    }
    public static boolean isEmpty(List<Long> list) {
	return list == null || list.isEmpty();
    }

    public static String encodeUTF8(String string) {
	if (string == null) {
	    return null;
	}
	try {
	    return URLEncoder.encode(string, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return null;
	}
    }
    
    public static String decodeUTF8(String string) {
	if (string == null) {
	    return null;
	}
	try {
	    return URLDecoder.decode(string, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static Calendar getDataAdicionandoTempo(GregorianCalendar calendarInicial, int totalTempoParaAdicionar, int tipoCampo) {
	Calendar calendarClone = (Calendar) calendarInicial.clone();
	calendarClone.add(tipoCampo, totalTempoParaAdicionar);
	return calendarClone;
    }
    
    public static <T> List<T> newList(T... array) {
	return Arrays.asList(array);
    }

    public static String formatarDDMMYYYY_HHMMYYYY(GregorianCalendar calendar) {
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	return dateFormat.format(calendar.getTime());
    }
    public static String formatarDDMMYYYY(GregorianCalendar calendar) {
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	return dateFormat.format(calendar.getTime());
    }
    
    public static String retirarLetras(String texto) {
	if (texto == null) {
	    return null;
	}
	return texto.replaceAll("\\D", "");
    }
    
    public static void main(String[] args) {
	
	String c = "fe3li4pe55";
	
	System.err.println(c.replaceAll("\\d", ""));
    }

    public static boolean isTelefoneValido(String telefone) {
	if (isEmptyTrim(telefone)) {
	    return false;
	}
	return telefone.length() >= 10 && telefone.length() <= 11;
    }

    public static boolean isLinkHttpValido(String url) {
	return isNotEmpty(url) && url.startsWith("http://");
    }
}






















