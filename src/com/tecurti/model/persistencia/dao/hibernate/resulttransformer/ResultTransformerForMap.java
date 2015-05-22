package com.tecurti.model.persistencia.dao.hibernate.resulttransformer;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.ResultTransformer;

import com.tecurti.model.persistencia.dao.hibernate.HibernateUtil;

public class ResultTransformerForMap {

    public static ResultTransformer RESULT_TRANSFORMER = new ResultTransformer() {
	
	@Override
	public Object transformTuple(Object[] values, String[] alias) {
	    Map<String, Object> mapResultado = new HashMap<>();
	    for (int i = 0; i < alias.length; i++) {
		Object valorFinal = values[i];
		if (valorFinal instanceof Timestamp) {
		    Calendar asCalendar = HibernateUtil.timestampToCalendar((Timestamp) valorFinal);
		    valorFinal = asCalendar;
		}
		mapResultado.put(alias[i], valorFinal);
	    }
	    
	    return mapResultado;
	}

	@Override
	public List transformList(List list) {
	    return list;
	}
    };
}
