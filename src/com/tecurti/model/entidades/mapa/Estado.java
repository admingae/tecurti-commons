package com.tecurti.model.entidades.mapa;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;
import com.tecurti.model.utils.ModelUtils;

public class Estado implements PontoNoMapa {

    final Long id;
    final String i18nNome;
    final String tokenMaps;
    final Pais pais;
    
    ZoomNoMapa zoom = new ZoomNoMapa();
    
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
    
    public String getNomeAsUTF8(i18nUsandoApiPadrao idioma) {
	return ModelUtils.encodeUTF8(getNome(idioma));
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

    public Pais getPais() {
        return pais;
    }

    public String getTokenMaps() {
        return tokenMaps;
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
