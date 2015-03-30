package com.tecurti.model.entidades.mapa;

import java.util.List;

public class Planeta {

    Long id;
    String i18nNome;
    private List<Continente> listContinentes;
    
    public Planeta(Long id, String i18nNome) {
	super();
	this.id = id;
	this.i18nNome = i18nNome;
    }

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public String getI18nNome() {
	return i18nNome;
    }

    public void setI18nNome(String i18nNome) {
	this.i18nNome = i18nNome;
    }

    public List<Continente> getListContinentes() {
	return listContinentes;
    }

    public void setListContinentes(List<Continente> listContinentes) {
	this.listContinentes = listContinentes;
    }

}
