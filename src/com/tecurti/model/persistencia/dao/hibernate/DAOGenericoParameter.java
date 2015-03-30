package com.tecurti.model.persistencia.dao.hibernate;

public class DAOGenericoParameter {

    private String key;
    private Object value;

    public DAOGenericoParameter(String key, Object value) {
	super();
	this.key = key;
	this.value = value;
    }

    public String getKey() {
	return key;
    }

    public Object getValue() {
	return value;
    }

}
