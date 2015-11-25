package com.tecurti.model.entidades.mapa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;
import com.tecurti.model.utils.ModelUtils;
import com.tecurti.view.util.WebUtils;

public class Pais implements PontoNoMapa {

    final Long id;
    final String i18nNome;
    final String tokenMaps;

    Continente continente;
    ZoomNoMapa zoom = new ZoomNoMapa();
    List<Estado> listEstados = new ArrayList<>();

    public Pais(Long id, String i18nNome, Continente continente, String tokenMaps) {
	super();
	this.id = id;
	this.i18nNome = i18nNome;
	this.continente = continente;
	this.tokenMaps = tokenMaps;
    }
    
    public Estado findEstado(Integer idEstado) {
	return idEstado == null ? null : findEstado(idEstado.longValue());
    }
    public Estado findEstado(Long idEstado) {
	for (Estado estado : listEstados) {
	    if (estado.getId().equals(idEstado)) {
		return estado;
	    }
	}
	return null;
    }
    
    public List<Estado> getEstadosOrdenadosPorNome(i18nUsandoApiPadrao idioma) {
	if (listEstados == null) {
	    return new ArrayList<>();
	}
	List<Estado> listEstadosListaModificavel = new ArrayList<>(listEstados);
	Collections.sort(listEstadosListaModificavel, new OrdenarEstadosPorNome(idioma));
	return listEstadosListaModificavel;
    }
    
    private static class OrdenarEstadosPorNome implements Comparator<Estado> {
	private i18nUsandoApiPadrao idioma;

	public OrdenarEstadosPorNome(i18nUsandoApiPadrao idioma) {
	    this.idioma = idioma;
	}

	@Override
	public int compare(Estado o1, Estado o2) {
	    return o1.getNome(idioma).compareTo(o2.getNome(idioma));
	}
    }
    
    public String getNomeAsUTF8(i18nUsandoApiPadrao idioma) {
	return ModelUtils.encodeUTF8(getNome(idioma));
    }
    public String getNomeAsHtml(i18nUsandoApiPadrao idioma) {
	return StringEscapeUtils.escapeHtml4(getNome(idioma));
    }
    public String getNome(i18nUsandoApiPadrao idioma) {
	return idioma.geti18nTexto(i18nNome);
    }
    
    public Long getId() {
	return id;
    }

    public String getI18nNome() {
	return i18nNome;
    }

    public Continente getContinente() {
	return continente;
    }

    public void setContinente(Continente continente) {
	this.continente = continente;
    }

    public List<Estado> getListEstados() {
	return listEstados;
    }

    public void setListEstados(List<Estado> listEstados) {
	this.listEstados = listEstados;
    }

    public boolean isPossuiEstados() {
	return listEstados.size() > 0;
    }

    public String getTokenMaps() {
        return tokenMaps;
    }

    public Estado findEstadoByTokenMaps(String tokenMaps) {
	for (Estado estado : listEstados) {
	    if (ModelUtils.equalsIn(estado.getTokenMaps(), tokenMaps)) {
		return estado;
	    }
	}
	return null;
    }

    @Override
    public String toString() {
	return i18nNome;
    }

    public ZoomNoMapa getZoom() {
        return zoom;
    }

    public void setZoom(ZoomNoMapa zoom) {
        this.zoom = zoom;
    }
    
}




























