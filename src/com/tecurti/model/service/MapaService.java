package com.tecurti.model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tecurti.model.entidades.mapa.Continente;
import com.tecurti.model.entidades.mapa.Estado;
import com.tecurti.model.entidades.mapa.Pais;
import com.tecurti.model.entidades.mapa.Planeta;
import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;
import com.tecurti.model.utils.ModelUtils;

public class MapaService {
    
    private static List<Planeta> listPlanetas = createPlanetas();
    
    public static final Pais PAIS_OUTRO = new Pais(-1L, "mapa.planeta.terra.continente.americaDoSul.pais.outro", null, null);
    
    public static final Long ID_BRASIL = 1L;

    public static final Pais BRASIL = findPais(ID_BRASIL);
    
    public static List<Pais> getTodosPaisesOrdenadosPorNome(i18nUsandoApiPadrao idioma) {
	List<Pais> todosPaises = getTodosPaises();
	Collections.sort(todosPaises, new OrdenarPaisesPorNome(idioma));
	return todosPaises;
    }
    
    public static Pais findPais(Long idPais) {
	if (PAIS_OUTRO.getId().equals(idPais)) {
	    return PAIS_OUTRO;
	}
	List<Pais> todosPaises = getTodosPaises();
	for (Pais pais : todosPaises) {
	    if (pais.getId().equals(idPais)) {
		return pais;
	    }
	}
	return null;
    }
    
    public List<Estado> findEstadosDoPaisOrdenados(Long idPais, i18nUsandoApiPadrao idioma) {
	Pais pais = findPais(idPais);
	return pais == null ? new ArrayList<Estado>() : pais.getEstadosOrdenadosPorNome(idioma);
    }
    
    private static class OrdenarPaisesPorNome implements Comparator<Pais> {
	private i18nUsandoApiPadrao idioma;

	public OrdenarPaisesPorNome(i18nUsandoApiPadrao idioma) {
	    this.idioma = idioma;
	}

	@Override
	public int compare(Pais o1, Pais o2) {
	    return o1.getNome(idioma).compareTo(o2.getNome(idioma));
	}
    }
    
    public static List<Pais> getTodosPaises() {
	List<Pais> listTodosPaises = new ArrayList<Pais>();
	for (Planeta planeta : listPlanetas) {
	    for (Continente continente : planeta.getListContinentes()) {
		for (Pais pais : continente.getListPaises()) {
		    listTodosPaises.add(pais);
		}
	    }
	}
	return listTodosPaises;
    }
    
    private static List<Planeta> createPlanetas() {
	List<Planeta> listPlanetas = new ArrayList<>();
	
	listPlanetas.add(criarPlanetaTerra());
	return Collections.unmodifiableList(listPlanetas);
    }

    private static Planeta criarPlanetaTerra() {
	Planeta terra = new Planeta(1L, "mapa.planeta.terra");
	terra.setListContinentes(criarContinentesDaTerra(terra));
	return terra;
    }

    private static List<Continente> criarContinentesDaTerra(Planeta terra) {
	List<Continente> listContinentes = new ArrayList<>();
	
	Continente americaDoSul = new Continente(1L, "mapa.planeta.terra.continente.americaDoSul", terra);
	americaDoSul.setListPaises(criarPaisesDaAmericaDoSul(americaDoSul));
	
	listContinentes.add(americaDoSul);
	return Collections.unmodifiableList(listContinentes);
    }

    private static List<Pais> criarPaisesDaAmericaDoSul(Continente americaDoSul) {
	List<Pais> listPaises = new ArrayList<>();
	
	Pais brasil = new Pais(1L, "mapa.planeta.terra.continente.americaDoSul.pais.brasil", americaDoSul, "BR");
	brasil.setListEstados(criarEstadosDoBrasil(brasil));
	
	listPaises.add(brasil);
	return Collections.unmodifiableList(listPaises);
    }

    private static List<Estado> criarEstadosDoBrasil(Pais brasil) {
	
	String i18nBaseEstadoBrasil = "mapa.planeta.terra.continente.americaDoSul.pais.brasil.estado.";
	
	Estado acre 		= criarEstado(brasil, 1L,  i18nBaseEstadoBrasil + "acre", "AC");
	Estado alagoas 		= criarEstado(brasil, 2L,  i18nBaseEstadoBrasil + "alagoas", "AL");
	Estado amapa 		= criarEstado(brasil, 3L,  i18nBaseEstadoBrasil + "amapa", "AP");
	Estado amazonas 	= criarEstado(brasil, 4L,  i18nBaseEstadoBrasil + "amazonas", "AM");
	Estado bahia 		= criarEstado(brasil, 5L,  i18nBaseEstadoBrasil + "bahia", "BA");
	Estado ceara 		= criarEstado(brasil, 6L,  i18nBaseEstadoBrasil + "ceara", "CE");
	Estado distritoFederal 	= criarEstado(brasil, 7L,  i18nBaseEstadoBrasil + "distritoFederal", "DF");
	Estado espiritoSanto 	= criarEstado(brasil, 8L,  i18nBaseEstadoBrasil + "espiritoSanto", "ES");
	Estado goias 		= criarEstado(brasil, 9L,  i18nBaseEstadoBrasil + "goias", "GO");
	Estado maranhao 	= criarEstado(brasil, 10L, i18nBaseEstadoBrasil + "maranhao", "MA");
	Estado matoGrosso 	= criarEstado(brasil, 11L, i18nBaseEstadoBrasil + "matoGrosso", "MT");
	Estado matoGrossoDoSul 	= criarEstado(brasil, 12L, i18nBaseEstadoBrasil + "matoGrossoDoSul", "MS");
	Estado minasGerais 	= criarEstado(brasil, 13L, i18nBaseEstadoBrasil + "minasGerais", "MG");
	Estado para 		= criarEstado(brasil, 14L, i18nBaseEstadoBrasil + "para", "PA");
	Estado paraiba 		= criarEstado(brasil, 15L, i18nBaseEstadoBrasil + "paraiba", "PB");
	Estado parana 		= criarEstado(brasil, 16L, i18nBaseEstadoBrasil + "parana", "PR");
	Estado pernambuco 	= criarEstado(brasil, 17L, i18nBaseEstadoBrasil + "pernambuco", "PE");
	Estado piaui 		= criarEstado(brasil, 18L, i18nBaseEstadoBrasil + "piaui", "PI");
	Estado rioDeJaneiro 	= criarEstado(brasil, 19L, i18nBaseEstadoBrasil + "rioDeJaneiro", "RJ");
	Estado rioGrandeDoNorte = criarEstado(brasil, 20L, i18nBaseEstadoBrasil + "rioGrandeDoNorte", "RN");
	Estado rioGrandeDoSul 	= criarEstado(brasil, 21L, i18nBaseEstadoBrasil + "rioGrandeDoSul", "RS");
	Estado rondonia 	= criarEstado(brasil, 22L, i18nBaseEstadoBrasil + "rondonia", "RO");
	Estado roraima 		= criarEstado(brasil, 23L, i18nBaseEstadoBrasil + "roraima", "RR");
	Estado santaCatarina 	= criarEstado(brasil, 24L, i18nBaseEstadoBrasil + "santaCatarina", "SC");
	Estado saoPaulo 	= criarEstado(brasil, 25L, i18nBaseEstadoBrasil + "saoPaulo", "SP");
	Estado sergipe 		= criarEstado(brasil, 26L, i18nBaseEstadoBrasil + "sergipe", "SE");
	Estado tocantins 	= criarEstado(brasil, 27L, i18nBaseEstadoBrasil + "tocantins", "TO");
	
	alagoas.addEstadoVizinho(pernambuco);
	alagoas.addEstadoVizinho(bahia);
	alagoas.addEstadoVizinho(sergipe);
	
	amapa.addEstadoVizinho(para);
	
	amazonas.addEstadoVizinho(roraima);
	amazonas.addEstadoVizinho(para);
	amazonas.addEstadoVizinho(matoGrosso);
	amazonas.addEstadoVizinho(rondonia);
	amazonas.addEstadoVizinho(acre);
	
	bahia.addEstadoVizinho(sergipe);
	bahia.addEstadoVizinho(alagoas);
	bahia.addEstadoVizinho(pernambuco);
	bahia.addEstadoVizinho(piaui);
	bahia.addEstadoVizinho(maranhao);
	bahia.addEstadoVizinho(tocantins);
	bahia.addEstadoVizinho(goias);
	bahia.addEstadoVizinho(minasGerais);
	bahia.addEstadoVizinho(espiritoSanto);

	ceara.addEstadoVizinho(rioGrandeDoNorte);
	ceara.addEstadoVizinho(paraiba);
	ceara.addEstadoVizinho(pernambuco);
	ceara.addEstadoVizinho(piaui);
	
	distritoFederal.addEstadoVizinho(goias);
	distritoFederal.addEstadoVizinho(minasGerais);
	distritoFederal.addEstadoVizinho(bahia);
	distritoFederal.addEstadoVizinho(tocantins);
	distritoFederal.addEstadoVizinho(matoGrosso);
	distritoFederal.addEstadoVizinho(matoGrossoDoSul);
	
	espiritoSanto.addEstadoVizinho(bahia);
	espiritoSanto.addEstadoVizinho(minasGerais);
	espiritoSanto.addEstadoVizinho(rioDeJaneiro);
	
	goias.addEstadoVizinho(distritoFederal);
	goias.addEstadoVizinho(minasGerais);
	goias.addEstadoVizinho(bahia);
	goias.addEstadoVizinho(tocantins);
	goias.addEstadoVizinho(matoGrosso);
	goias.addEstadoVizinho(matoGrossoDoSul);
	
	maranhao.addEstadoVizinho(para);
	maranhao.addEstadoVizinho(tocantins);
	maranhao.addEstadoVizinho(bahia);
	maranhao.addEstadoVizinho(piaui);
	
	matoGrosso.addEstadoVizinho(rondonia);
	matoGrosso.addEstadoVizinho(amazonas);
	matoGrosso.addEstadoVizinho(para);
	matoGrosso.addEstadoVizinho(tocantins);
	matoGrosso.addEstadoVizinho(distritoFederal);
	matoGrosso.addEstadoVizinho(goias);
	matoGrosso.addEstadoVizinho(matoGrossoDoSul);
	
	matoGrossoDoSul.addEstadoVizinho(matoGrosso);
	matoGrossoDoSul.addEstadoVizinho(goias);
	matoGrossoDoSul.addEstadoVizinho(distritoFederal);
	matoGrossoDoSul.addEstadoVizinho(minasGerais);
	matoGrossoDoSul.addEstadoVizinho(saoPaulo);
	matoGrossoDoSul.addEstadoVizinho(parana);
	
	minasGerais.addEstadoVizinho(bahia);
	minasGerais.addEstadoVizinho(goias);
	minasGerais.addEstadoVizinho(matoGrossoDoSul);
	minasGerais.addEstadoVizinho(saoPaulo);
	minasGerais.addEstadoVizinho(rioDeJaneiro);
	minasGerais.addEstadoVizinho(espiritoSanto);
	
	para.addEstadoVizinho(amapa);
	para.addEstadoVizinho(roraima);
	para.addEstadoVizinho(amazonas);
	para.addEstadoVizinho(matoGrosso);
	para.addEstadoVizinho(tocantins);
	para.addEstadoVizinho(maranhao);
	
	paraiba.addEstadoVizinho(rioGrandeDoNorte);
	paraiba.addEstadoVizinho(ceara);
	paraiba.addEstadoVizinho(pernambuco);
	
	parana.addEstadoVizinho(matoGrossoDoSul);
	parana.addEstadoVizinho(saoPaulo);
	parana.addEstadoVizinho(santaCatarina);
	
	pernambuco.addEstadoVizinho(paraiba);
	pernambuco.addEstadoVizinho(ceara);
	pernambuco.addEstadoVizinho(piaui);
	pernambuco.addEstadoVizinho(bahia);
	pernambuco.addEstadoVizinho(alagoas);
	pernambuco.addEstadoVizinho(sergipe);
	
	piaui.addEstadoVizinho(ceara);
	piaui.addEstadoVizinho(pernambuco);
	piaui.addEstadoVizinho(bahia);
	piaui.addEstadoVizinho(tocantins);
	piaui.addEstadoVizinho(maranhao);
	
	rioDeJaneiro.addEstadoVizinho(espiritoSanto);
	rioDeJaneiro.addEstadoVizinho(minasGerais);
	rioDeJaneiro.addEstadoVizinho(saoPaulo);
	
	rioGrandeDoNorte.addEstadoVizinho(ceara);
	rioGrandeDoNorte.addEstadoVizinho(paraiba);
	
	rioGrandeDoSul.addEstadoVizinho(santaCatarina);
	
	rondonia.addEstadoVizinho(acre);
	rondonia.addEstadoVizinho(amazonas);
	rondonia.addEstadoVizinho(matoGrosso);
	
	roraima.addEstadoVizinho(amazonas);
	roraima.addEstadoVizinho(para);
	
	santaCatarina.addEstadoVizinho(rioGrandeDoSul);
	santaCatarina.addEstadoVizinho(parana);
	
	saoPaulo.addEstadoVizinho(rioDeJaneiro);
	saoPaulo.addEstadoVizinho(minasGerais);
	saoPaulo.addEstadoVizinho(matoGrossoDoSul);
	saoPaulo.addEstadoVizinho(parana);
	
	sergipe.addEstadoVizinho(alagoas);
	sergipe.addEstadoVizinho(pernambuco);
	sergipe.addEstadoVizinho(bahia);
	
	tocantins.addEstadoVizinho(para);
	tocantins.addEstadoVizinho(maranhao);
	tocantins.addEstadoVizinho(piaui);
	tocantins.addEstadoVizinho(bahia);
	tocantins.addEstadoVizinho(distritoFederal);
	tocantins.addEstadoVizinho(goias);
	tocantins.addEstadoVizinho(matoGrosso);
	
	List<Estado> listEstados = new ArrayList<Estado>();
	listEstados.add(acre);
	listEstados.add(alagoas);
	listEstados.add(amapa);
	listEstados.add(amazonas);
	listEstados.add(bahia);
	listEstados.add(ceara);
	listEstados.add(distritoFederal);
	listEstados.add(espiritoSanto);
	listEstados.add(goias);
	listEstados.add(maranhao);
	listEstados.add(matoGrosso);
	listEstados.add(matoGrossoDoSul);
	listEstados.add(minasGerais);
	listEstados.add(para);
	listEstados.add(paraiba);
	listEstados.add(parana);
	listEstados.add(pernambuco);
	listEstados.add(piaui);
	listEstados.add(rioDeJaneiro);
	listEstados.add(rioGrandeDoNorte);
	listEstados.add(rioGrandeDoSul);
	listEstados.add(rondonia);
	listEstados.add(roraima);
	listEstados.add(santaCatarina);
	listEstados.add(saoPaulo);
	listEstados.add(sergipe);
	listEstados.add(tocantins);
	
	return Collections.unmodifiableList(listEstados);
    }
    
    public static void main(String[] args) {
	
	List<String> list = new ArrayList<String>();
	list.add("felipe0");
	list.add("felipe1");
	list.add("felipe2");
	list.add("felipe3");
	
	List<String> listUmodi = Collections.unmodifiableList(list);
	listUmodi.add("ddd");
	for (String string : listUmodi) {
	    System.err.println(string);
	}
    }

    private static Estado criarEstado(Pais pais, long id, String nomei18n, String tokenMaps) {
	return new Estado(id, nomei18n, pais, tokenMaps);
    }

    public static Pais findPaisByTokenMaps(String tokenMaps) {
	for (Pais pais : getTodosPaises()) {
	    if (ModelUtils.equalsIn(pais.getTokenMaps(), tokenMaps)) {
		return pais;
	    }
	}
	return null;
    }

}

















