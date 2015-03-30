package com.tecurti.model.persistencia.dao.hibernate;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.Transaction;


public class ObjectDAO extends HibernateDAOGenerico<Object, Serializable> {

    @Override
    protected Class<Object> getGenericsClass() {
	return Object.class;
    }

    public void saveOrUpdate(final Object... objetos) throws Exception {
	executeTransaction(new HibernateSessionCommand() { @Override public void execute(Session session, Transaction transaction) {
	    for (Object object : objetos) {
		session.saveOrUpdate(object);
	    }
	}});
    }
    
    public void delete(final Object... objetos) throws Exception {
	
	executeTransaction(new HibernateSessionCommand() { @Override public void execute(Session session, Transaction transaction) {
	    for (Object object : objetos) {
		session.delete(object);
	    }
	}});
    }
}
