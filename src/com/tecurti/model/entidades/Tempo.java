package com.tecurti.model.entidades;

import com.tecurti.model.service.idioma.i18nUsandoApiPadrao;
import com.tecurti.view.util.WebUtils;

public class Tempo {

    private long tempo;
    private UnidadeTempo unidade;
    
    public long getTempo() {
        return tempo;
    }
    public void setTempo(long tempo) {
        this.tempo = tempo;
    }
    public UnidadeTempo getUnidade() {
        return unidade;
    }
    public void setUnidade(UnidadeTempo unidade) {
        this.unidade = unidade;
    }
    public String getDataAmigavel(i18nUsandoApiPadrao idioma) {
	String descricaoDataAmigavel = this.getUnidade().getDescricao(this.getTempo(), idioma);
	return descricaoDataAmigavel;
    }
    public String getDataAmigavelAsHtml(i18nUsandoApiPadrao idioma) {
	return WebUtils.escapeTextoParaHTML(getDataAmigavel(idioma));
    }
    
}
