package com.tecurti.model.persistencia.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class HibernateSessionCommand<T> {
    
    public void execute(Session session, Transaction transaction) throws Exception {
    }

    public T executeComRetorno(Session session, Transaction transaction) throws Exception {
	return null;
    }
    
}
