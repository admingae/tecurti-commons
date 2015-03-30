package com.tecurti.model.persistencia.dao.gae;

import com.google.appengine.api.datastore.Query.SortDirection;

public class SortConfigurationGae {

    public String campo;
    public SortDirection direction;

    public SortConfigurationGae(String campo, SortDirection direction) {
	this.campo = campo;
	this.direction = direction;
    }

}
