package com.tecurti.model.persistencia.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class HibernateSessionCommand {
    
    public void execute(Session session, Transaction transaction) throws Exception {
    }

    public Object executeComRetorno(Session session, Transaction transaction) throws Exception {
	return null;
    }
    
}
