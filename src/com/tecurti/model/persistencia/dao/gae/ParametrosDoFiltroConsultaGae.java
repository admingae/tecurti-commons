package com.tecurti.model.persistencia.dao.gae;

import java.util.Calendar;

import com.google.appengine.api.datastore.Query.FilterOperator;

public class ParametrosDoFiltroConsultaGae {
    
    private String name;
    private FilterOperator filterOperator;
    private Object value;

    public ParametrosDoFiltroConsultaGae(String name, Object valor) {
	this(name, FilterOperator.EQUAL, valor);
    }
    
    public ParametrosDoFiltroConsultaGae(String name, FilterOperator filterOperator, Object valor) {
	super();
	this.name = name;
	this.filterOperator = filterOperator;
	this.value = valor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterOperator getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }

    public Object getValue() {
        return value;
    }
    
    public Object getValueConvertidoParaFiltroGae() {
	return converterValorJavaParaFiltroGae(value);
    }

    public static Object converterValorJavaParaFiltroGae(Object valorNoPadraoJavaParaConverter) {
	
	if (valorNoPadraoJavaParaConverter != null) {
	    if (valorNoPadraoJavaParaConverter.getClass().isEnum() || valorNoPadraoJavaParaConverter instanceof Enum) {
		return valorNoPadraoJavaParaConverter.toString();
	    }
	    if (valorNoPadraoJavaParaConverter instanceof Calendar) {
		return ((Calendar)valorNoPadraoJavaParaConverter).getTime();
	    }
	} 
	
	return valorNoPadraoJavaParaConverter;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    
}