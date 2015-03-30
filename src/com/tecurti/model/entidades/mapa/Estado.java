package com.tecurti.model.entidades.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;

public class Estado {

    final Long id;
    final String i18nNome;
    final String tokenMaps;
    final Pais pais;
    List<Estado> listEstadosVizinhos = new ArrayList<>();
    
    public Estado(Long id, String i18nNome, Pais pais, String tokenMaps) {
	super();
	this.id = id;
	this.i18nNome = i18nNome;
	this.pais = pais;
	this.tokenMaps = tokenMaps;
    }

    public String getNomeAsHtml(i18nUsandoApiPadrao idioma) {
	return StringEscapeUtils.escapeHtml4(getNome(idioma));
    }
    
    public String getNome(i18nUsandoApiPadrao idioma) {
	return idioma.geti18nTexto(i18nNome);
    }
    
    public void addEstadoVizinho(Estado estado) {
	listEstadosVizinhos.add(estado);
    }

    public List<Estado> getEstadosVizinhos() {
	return listEstadosVizinhos;
    }

    public Long getId() {
	return id;
    }

    public String getI18nNome() {
	return i18nNome;
    }

    public List<Estado> getListEstadosVizinhos() {
	return listEstadosVizinhos;
    }

    public void setListEstadosVizinhos(List<Estado> listEstadosVizinhos) {
	this.listEstadosVizinhos = listEstadosVizinhos;
    }

    public Pais getPais() {
        return pais;
    }

    public String getTokenMaps() {
        return tokenMaps;
    }

}
