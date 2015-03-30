package com.tecurti.model.persistencia.dao.gae.entidades;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public interface EntityGae {
    
    public Key getParentKey();
    public Key getKey();
    public void setKey(Key key);
    
    public abstract Entity toEntityGae();
    public abstract void setClassFields(Entity entity);
}
