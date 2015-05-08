package com.tecurti.model.persistencia.dao.hibernate.resulttransformer;

import java.util.List;

import org.hibernate.transform.ResultTransformer;

public class ResultTransformerForLong {

    public static ResultTransformer RESULT_TRANSFORMER = new ResultTransformer() {
	@Override
	public Object transformTuple(Object[] values, String[] alias) {
	    Object valor = values[0];
	    return valor == null ? null : Long.parseLong(valor.toString());
	}

	@Override
	public List transformList(List list) {
	    return list;
	}
    };

}
