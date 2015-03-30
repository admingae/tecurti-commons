package com.tecurti.model.persistencia.dao.gae;


public class CampoNomeValor {

    String nome;
    Object valor;

    public CampoNomeValor(String nome, Object valor) {
	super();
	this.nome = nome;
	this.valor = valor;
    }

    public String getNome() {
	return nome;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    public Object getValor() {
	return valor;
    }

    public void setValor(Object valor) {
	this.valor = valor;
    }
    
    public Object getValueConvertidoParaFiltroGae() {
	return ParametrosDoFiltroConsultaGae.converterValorJavaParaFiltroGae(valor);
    }
}
