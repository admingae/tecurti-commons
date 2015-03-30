package com.tecurti.model.persistencia.dao.gae;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PojoDetails {

    List<CampoPojoDetail> listCampos = new ArrayList<CampoPojoDetail>();
    String kind;
    
    public PojoDetails(Class<?> klass) {
	
	kind = klass.getSimpleName();
	for (Field field : klass.getDeclaredFields()) {
	    if (Modifier.isStatic(field.getModifiers()) == false && Modifier.isTransient(field.getModifiers()) == false) {
		listCampos.add(new CampoPojoDetail(field));
	    }
	}
    }

    public Class<?> getFieldClass(String fieldName) {
	
	for (CampoPojoDetail campoPojoDetail : listCampos) {
	    if (campoPojoDetail.getName().equals(fieldName)) {
		return campoPojoDetail.getFieldClassConvertendoParaTipoGae();
	    }
	}
	
	return null;
    }

    public List<CampoPojoDetail> getTodosCampos() {
	return listCampos;
    }

    public CampoPojoDetail findCampo(String fieldName) {
	
	for (CampoPojoDetail campoPojoDetail : getTodosCampos()) {
	    if (campoPojoDetail.getName().equals(fieldName)) {
		return campoPojoDetail;
	    }
	}
	
	return null;
    }

    public String getKind() {
        return kind;
    }

}
