package com.tecurti.model.persistencia.dao.gae.entidades;

import java.io.Serializable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO;

public class SequenceGae implements Serializable, EntityGae {
    
    String nameId;
    Long count = 0L;
    
    public static class Attr {
	public static String KIND = UtilsGaeDAO.getKIND(SequenceGae.class);
	
	public static String nameId = "nameId";
	public static String count = "count";
    }

    @Override
    public Key getParentKey() {
	return createParentKey(nameId);
    }
    
    @Override
    public Key getKey() {
	return createKey(nameId);
    }

    public static Key createKey(String nameId) {
	Key parentKey = createParentKey(nameId);
	return KeyFactory.createKey(parentKey, SequenceGae.Attr.KIND, nameId);
    }
    public static Key createParentKey(String nameId) {
	return KeyFactory.createKey(SequenceGae.Attr.KIND, nameId);
    }

    @Override
    public void setKey(Key key) {
	nameId = key.getName();
    }

    @Override
    public Entity toEntityGae() {
	Entity entity = UtilsGaeDAO.instanciarEntity(this);
	entity.setProperty(SequenceGae.Attr.count, getCount());
	return entity;
    }

    @Override
    public void setClassFields(Entity entity) {
	setKey(entity.getKey());
	setCount((Long) entity.getProperty(SequenceGae.Attr.count));
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void setIncrementCount() {
	count++;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }
    
    public Long increase() {
	count++;
	return count;
    }
}
