package com.tecurti.model.persistencia.dao.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;

public abstract class CodigoTransacional<T> {
    
    public void execute(DatastoreService datastore, Transaction txn) throws Exception {
    }
    
    public T executeComRetorno(DatastoreService datastore, Transaction txn) throws Exception {
	return null;
    }
}
