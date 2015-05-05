package com.tecurti.model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tecurti.model.entidades.mapa.Continente;
import com.tecurti.model.entidades.mapa.Estado;
import com.tecurti.model.entidades.mapa.Pais;
import com.tecurti.model.entidades.mapa.Planeta;
import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;
import com.tecurti.model.utils.ModelUtils;

public class MapaService {
    
    private static List<Planeta> listPlanetas = createPlanetas();
    public static final Pais PAIS_OUTRO = new Pais(MapaServiceConstantes.ID_PAIS_OUTRO, "mapa.planeta.terra.continente.americaDoSul.pais.outro", null, null);
    
    private static Map<Long, Estado> cacheEstados = new HashMap<>();
    private static Map<Long, Pais> cachePaises = new HashMap<>();
    static {
	iniciarCaches();
	iniciarZoom();
    }

    private static void iniciarCaches() {
	cachePaises.put(PAIS_OUTRO.getId(), PAIS_OUTRO);
	for (Planeta planeta : listPlanetas) {
	    List<Continente> listContinentes = planeta.getListContinentes();
	    for (Continente continente : listContinentes) {
		List<Pais> listPaises = continente.getListPaises();
		for (Pais pais : listPaises) {
		    cachePaises.put(pais.getId(), pais);
		    
		    List<Estado> listEstados = pais.getListEstados();
		    for (Estado estado : listEstados) {
			cacheEstados.put(estado.getId(), estado);
		    }
		}
	    }
	}
    }
    
    public static void main(String[] args) {
	for (Planeta planeta : listPlanetas) {
	    for (Continente continente : planeta.getListContinentes()) {
		for (Pais pais : continente.getListPaises()) {
		    System.err.println(pais.getZoom().toString());
		    for (Estado estado : pais.getListEstados()) {
			System.err.println("estado");
			System.err.println(estado.getZoom().toString());
			System.err.println("\n");
		    }
		}
	    }
	} 
    }
    
    private static void iniciarZoom() {
	
	Pais brasil 	= findPais(MapaServiceConstantes.ID_PAIS_BRASIL);
	Estado acre 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_ACRE);
	Estado alagoas 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_ALAGOAS);
	Estado amapa 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_AMAPA);
	Estado amazonas 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_AMAZONAS);
	Estado bahia 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_BAHIA);
	Estado ceara 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_CEARA);
	Estado distritoFederal 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_DISTRITOFEDERAL);
	Estado espiritoSanto 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_ESPIRITOSANTO);
	Estado goias 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_GOIAS);
	Estado maranhao 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_MARANHAO);
	Estado matoGrosso 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_MATOGROSSO);
	Estado matoGrossoDoSul 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_MATOGROSSODOSUL);
	Estado minasGerais 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_MINASGERAIS);
	Estado para 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PARA);
	Estado paraiba 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PARAIBA);
	Estado parana 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PARANA);
	Estado pernambuco 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PERNAMBUCO);
	Estado piaui 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PERNAMBUCO);
	Estado rioDeJaneiro 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_PIAUI);
	Estado rioGrandeDoNorte = findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_RIODEJANEIRO);
	Estado rioGrandeDoSul 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_RIOGRANDEDOSUL);
	Estado rondonia 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_RONDONIA);
	Estado roraima 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_RORAIMA);
	Estado santaCatarina 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_SANTACATARINA);
	Estado saoPaulo 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_SAOPAULO);
	Estado sergipe 		= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_SERGIPE);
	Estado tocantins 	= findEstado(MapaServiceConstantes.ID_ESTADO_BRASIL_TOCANTINS);
	
	int nivel1 = 1;
	int nivel2 = 2;
	int nivel3 = 3;
	
	// ----------------
	brasil.getZoom().addPontoNoMapa(nivel1, brasil);
	
	// ----------------
	List<Estado> listEstados = brasil.getListEstados();
	for (Estado estado : listEstados) {
	    estado.getZoom().addPontoNoMapa(nivel1, estado);
	    estado.getZoom().addPontoNoMapa(nivel3, brasil);
	}
	
	// ----------------
	alagoas.getZoom().addPontoNoMapa(nivel2, pernambuco);
	alagoas.getZoom().addPontoNoMapa(nivel2, bahia);
	alagoas.getZoom().addPontoNoMapa(nivel2, sergipe);
	
	amapa.getZoom().addPontoNoMapa(nivel2, para);
	
	amazonas.getZoom().addPontoNoMapa(nivel2, roraima);
	amazonas.getZoom().addPontoNoMapa(nivel2, para);
	amazonas.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	amazonas.getZoom().addPontoNoMapa(nivel2, rondonia);
	amazonas.getZoom().addPontoNoMapa(nivel2, acre);
	
	bahia.getZoom().addPontoNoMapa(nivel2, sergipe);
	bahia.getZoom().addPontoNoMapa(nivel2, alagoas);
	bahia.getZoom().addPontoNoMapa(nivel2, pernambuco);
	bahia.getZoom().addPontoNoMapa(nivel2, piaui);
	bahia.getZoom().addPontoNoMapa(nivel2, maranhao);
	bahia.getZoom().addPontoNoMapa(nivel2, tocantins);
	bahia.getZoom().addPontoNoMapa(nivel2, goias);
	bahia.getZoom().addPontoNoMapa(nivel2, minasGerais);
	bahia.getZoom().addPontoNoMapa(nivel2, espiritoSanto);

	ceara.getZoom().addPontoNoMapa(nivel2, rioGrandeDoNorte);
	ceara.getZoom().addPontoNoMapa(nivel2, paraiba);
	ceara.getZoom().addPontoNoMapa(nivel2, pernambuco);
	ceara.getZoom().addPontoNoMapa(nivel2, piaui);
	
	distritoFederal.getZoom().addPontoNoMapa(nivel2, goias);
	distritoFederal.getZoom().addPontoNoMapa(nivel2, minasGerais);
	distritoFederal.getZoom().addPontoNoMapa(nivel2, bahia);
	distritoFederal.getZoom().addPontoNoMapa(nivel2, tocantins);
	distritoFederal.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	distritoFederal.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	
	espiritoSanto.getZoom().addPontoNoMapa(nivel2, bahia);
	espiritoSanto.getZoom().addPontoNoMapa(nivel2, minasGerais);
	espiritoSanto.getZoom().addPontoNoMapa(nivel2, rioDeJaneiro);
	
	goias.getZoom().addPontoNoMapa(nivel2, distritoFederal);
	goias.getZoom().addPontoNoMapa(nivel2, minasGerais);
	goias.getZoom().addPontoNoMapa(nivel2, bahia);
	goias.getZoom().addPontoNoMapa(nivel2, tocantins);
	goias.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	goias.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	
	maranhao.getZoom().addPontoNoMapa(nivel2, para);
	maranhao.getZoom().addPontoNoMapa(nivel2, tocantins);
	maranhao.getZoom().addPontoNoMapa(nivel2, bahia);
	maranhao.getZoom().addPontoNoMapa(nivel2, piaui);
	
	matoGrosso.getZoom().addPontoNoMapa(nivel2, rondonia);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, amazonas);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, para);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, tocantins);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, distritoFederal);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, goias);
	matoGrosso.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, goias);
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, distritoFederal);
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, minasGerais);
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, saoPaulo);
	matoGrossoDoSul.getZoom().addPontoNoMapa(nivel2, parana);
	
	minasGerais.getZoom().addPontoNoMapa(nivel2, bahia);
	minasGerais.getZoom().addPontoNoMapa(nivel2, goias);
	minasGerais.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	minasGerais.getZoom().addPontoNoMapa(nivel2, saoPaulo);
	minasGerais.getZoom().addPontoNoMapa(nivel2, rioDeJaneiro);
	minasGerais.getZoom().addPontoNoMapa(nivel2, espiritoSanto);
	
	para.getZoom().addPontoNoMapa(nivel2, amapa);
	para.getZoom().addPontoNoMapa(nivel2, roraima);
	para.getZoom().addPontoNoMapa(nivel2, amazonas);
	para.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	para.getZoom().addPontoNoMapa(nivel2, tocantins);
	para.getZoom().addPontoNoMapa(nivel2, maranhao);
	
	paraiba.getZoom().addPontoNoMapa(nivel2, rioGrandeDoNorte);
	paraiba.getZoom().addPontoNoMapa(nivel2, ceara);
	paraiba.getZoom().addPontoNoMapa(nivel2, pernambuco);
	
	parana.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	parana.getZoom().addPontoNoMapa(nivel2, saoPaulo);
	parana.getZoom().addPontoNoMapa(nivel2, santaCatarina);
	
	pernambuco.getZoom().addPontoNoMapa(nivel2, paraiba);
	pernambuco.getZoom().addPontoNoMapa(nivel2, ceara);
	pernambuco.getZoom().addPontoNoMapa(nivel2, piaui);
	pernambuco.getZoom().addPontoNoMapa(nivel2, bahia);
	pernambuco.getZoom().addPontoNoMapa(nivel2, alagoas);
	pernambuco.getZoom().addPontoNoMapa(nivel2, sergipe);
	
	piaui.getZoom().addPontoNoMapa(nivel2, ceara);
	piaui.getZoom().addPontoNoMapa(nivel2, pernambuco);
	piaui.getZoom().addPontoNoMapa(nivel2, bahia);
	piaui.getZoom().addPontoNoMapa(nivel2, tocantins);
	piaui.getZoom().addPontoNoMapa(nivel2, maranhao);
	
	rioDeJaneiro.getZoom().addPontoNoMapa(nivel2, espiritoSanto);
	rioDeJaneiro.getZoom().addPontoNoMapa(nivel2, minasGerais);
	rioDeJaneiro.getZoom().addPontoNoMapa(nivel2, saoPaulo);
	
	rioGrandeDoNorte.getZoom().addPontoNoMapa(nivel2, ceara);
	rioGrandeDoNorte.getZoom().addPontoNoMapa(nivel2, paraiba);
	
	rioGrandeDoSul.getZoom().addPontoNoMapa(nivel2, santaCatarina);
	
	rondonia.getZoom().addPontoNoMapa(nivel2, acre);
	rondonia.getZoom().addPontoNoMapa(nivel2, amazonas);
	rondonia.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	
	roraima.getZoom().addPontoNoMapa(nivel2, amazonas);
	roraima.getZoom().addPontoNoMapa(nivel2, para);
	
	santaCatarina.getZoom().addPontoNoMapa(nivel2, rioGrandeDoSul);
	santaCatarina.getZoom().addPontoNoMapa(nivel2, parana);
	
	saoPaulo.getZoom().addPontoNoMapa(nivel2, rioDeJaneiro);
	saoPaulo.getZoom().addPontoNoMapa(nivel2, minasGerais);
	saoPaulo.getZoom().addPontoNoMapa(nivel2, matoGrossoDoSul);
	saoPaulo.getZoom().addPontoNoMapa(nivel2, parana);
	
	sergipe.getZoom().addPontoNoMapa(nivel2, alagoas);
	sergipe.getZoom().addPontoNoMapa(nivel2, pernambuco);
	sergipe.getZoom().addPontoNoMapa(nivel2, bahia);
	
	tocantins.getZoom().addPontoNoMapa(nivel2, para);
	tocantins.getZoom().addPontoNoMapa(nivel2, maranhao);
	tocantins.getZoom().addPontoNoMapa(nivel2, piaui);
	tocantins.getZoom().addPontoNoMapa(nivel2, bahia);
	tocantins.getZoom().addPontoNoMapa(nivel2, distritoFederal);
	tocantins.getZoom().addPontoNoMapa(nivel2, goias);
	tocantins.getZoom().addPontoNoMapa(nivel2, matoGrosso);
	
	// ----------------
	Pais outro = findPais(MapaServiceConstantes.ID_PAIS_OUTRO);
	outro.getZoom().addPontoNoMapa(nivel1, outro);
    }

    public static final Pais BRASIL = findPais(MapaServiceConstantes.ID_PAIS_BRASIL);
    
    public static List<Pais> getTodosPaisesOrdenadosPorNome(i18nUsandoApiPadrao idioma) {
	List<Pais> todosPaises = getTodosPaises();
	Collections.sort(todosPaises, new OrdenarPaisesPorNome(idioma));
	return todosPaises;
    }
    
    public static Pais findPais(Integer idPais) {
	return idPais == null ? null : findPais(idPais.longValue());
    }
    public static Pais findPais(Long idPais) {
	if (idPais == null) {
	    return null;
	}
	
	return cachePaises.get(idPais);
	/*
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
	*/
    }
    
    public static Estado findEstado(Integer idEstado) {
	return idEstado == null ? null : findEstado(idEstado.longValue());
    }
    public static Estado findEstado(Long idEstado) {
	if (idEstado == null) {
	    return null;
	}
	
	return cacheEstados.get(idEstado);
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
	
	Estado acre 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_ACRE,  i18nBaseEstadoBrasil + "acre", "AC");
	Estado alagoas 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_ALAGOAS,  i18nBaseEstadoBrasil + "alagoas", "AL");
	Estado amapa 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_AMAPA,  i18nBaseEstadoBrasil + "amapa", "AP");
	Estado amazonas 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_AMAZONAS,  i18nBaseEstadoBrasil + "amazonas", "AM");
	Estado bahia 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_BAHIA,  i18nBaseEstadoBrasil + "bahia", "BA");
	Estado ceara 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_CEARA,  i18nBaseEstadoBrasil + "ceara", "CE");
	Estado distritoFederal 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_DISTRITOFEDERAL,  i18nBaseEstadoBrasil + "distritoFederal", "DF");
	Estado espiritoSanto 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_ESPIRITOSANTO,  i18nBaseEstadoBrasil + "espiritoSanto", "ES");
	Estado goias 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_GOIAS,  i18nBaseEstadoBrasil + "goias", "GO");
	Estado maranhao 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_MARANHAO, i18nBaseEstadoBrasil + "maranhao", "MA");
	Estado matoGrosso 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_MATOGROSSO, i18nBaseEstadoBrasil + "matoGrosso", "MT");
	Estado matoGrossoDoSul 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_MATOGROSSODOSUL, i18nBaseEstadoBrasil + "matoGrossoDoSul", "MS");
	Estado minasGerais 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_MINASGERAIS, i18nBaseEstadoBrasil + "minasGerais", "MG");
	Estado para 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_PARA, i18nBaseEstadoBrasil + "para", "PA");
	Estado paraiba 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_PARAIBA, i18nBaseEstadoBrasil + "paraiba", "PB");
	Estado parana 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_PARANA, i18nBaseEstadoBrasil + "parana", "PR");
	Estado pernambuco 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_PERNAMBUCO, i18nBaseEstadoBrasil + "pernambuco", "PE");
	Estado piaui 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_PIAUI, i18nBaseEstadoBrasil + "piaui", "PI");
	Estado rioDeJaneiro 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_RIODEJANEIRO, i18nBaseEstadoBrasil + "rioDeJaneiro", "RJ");
	Estado rioGrandeDoNorte = criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_RIOGRANDEDONORTE, i18nBaseEstadoBrasil + "rioGrandeDoNorte", "RN");
	Estado rioGrandeDoSul 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_RIOGRANDEDOSUL, i18nBaseEstadoBrasil + "rioGrandeDoSul", "RS");
	Estado rondonia 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_RONDONIA, i18nBaseEstadoBrasil + "rondonia", "RO");
	Estado roraima 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_RORAIMA, i18nBaseEstadoBrasil + "roraima", "RR");
	Estado santaCatarina 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_SANTACATARINA, i18nBaseEstadoBrasil + "santaCatarina", "SC");
	Estado saoPaulo 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_SAOPAULO, i18nBaseEstadoBrasil + "saoPaulo", "SP");
	Estado sergipe 		= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_SERGIPE, i18nBaseEstadoBrasil + "sergipe", "SE");
	Estado tocantins 	= criarEstado(brasil, MapaServiceConstantes.ID_ESTADO_BRASIL_TOCANTINS, i18nBaseEstadoBrasil + "tocantins", "TO");
	
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

















