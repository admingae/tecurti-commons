package com.tecurti.model.entidades.mapa;

import java.util.List;

public class Continente implements PontoNoMapa {
    
    Long id;
    String i18nNome;
    Planeta planeta;
    private List<Pais> listPaises;
    
    public Continente(Long id, String i18nNome, Planeta planeta) {
	super();
	this.id = id;
	this.i18nNome = i18nNome;
	this.planeta = planeta;
    }

    public String getI18nNome() {
	return i18nNome;
    }

    public void setI18nNome(String i18nNome) {
	this.i18nNome = i18nNome;
    }

    public Planeta getPlaneta() {
	return planeta;
    }

    public void setPlaneta(Planeta planeta) {
	this.planeta = planeta;
    }

    public List<Pais> getListPaises() {
	return listPaises;
    }

    public void setListPaises(List<Pais> listPaises) {
	this.listPaises = listPaises;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
