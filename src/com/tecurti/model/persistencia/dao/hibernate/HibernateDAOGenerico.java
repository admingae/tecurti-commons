package com.tecurti.model.persistencia.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class HibernateDAOGenerico<T, ID extends Serializable> {

    protected Class<T> getGenericsClass() {

	// ---------------------
	Class directSubclass = getClass();
	while (directSubclass.getSuperclass().equals(HibernateDAOGenerico.class) == false) {
	    directSubclass = directSubclass.getSuperclass();
	}

	// ---------------------
	return (Class<T>) ((ParameterizedType) directSubclass.getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public static <T> T executeTransaction(HibernateSessionCommand<T> command) throws Exception {
	
	Session session = HibernateUtil.getSession();
	Transaction transaction = session.beginTransaction();
	
	try {
	    command.execute(session, transaction);
	    T retorno = command.executeComRetorno(session, transaction);
	    transaction.commit();
	    
	    return retorno;
	} catch (Exception e) {
	    transaction.rollback();
	    throw e;
	} finally {
	    session.close();
	}
    }

    public void insertOrUpdate(final T objeto) throws Exception {
	executeTransaction(new HibernateSessionCommand() { @Override public void execute(Session session, Transaction transaction) {
	    session.saveOrUpdate(objeto);
	}});
    }
    
    public <I>I insert(final T objeto) throws Exception {
	 return insert(objeto, null);
    }
    public <I>I insert(final T objeto, Session session) throws Exception {
	if (session == null) {
	    executeTransaction(new HibernateSessionCommand<Object>() { @Override public I executeComRetorno(Session sess, Transaction transaction) {
		return doSave(objeto, sess);
	    }});
	} else {
	     return doSave(objeto, session);
	}
	return null;
    }

    private <I>I doSave(final T objeto, Session session) {
	return (I) session.save(objeto);
    }
    
    public void update(final T objeto) throws Exception {
	update(objeto, null);
    }
    public void update(final T objeto, Session session) throws Exception {
	if (session == null) {
	    executeTransaction(new HibernateSessionCommand<Object>() {
		@Override public void execute(Session session, Transaction transaction) {
		    doUpdate(objeto, session);
		}
	    });
	} else {
	     doUpdate(objeto, session);
	}
    }
    private void doUpdate(final T objeto, Session session) {
	session.update(objeto);
    }
    
    public void delete(final T objeto) throws Exception {
	delete(objeto, null);
    }
    public void delete(final T objeto, Session session) throws Exception {
	if (session == null) {
	    executeTransaction(new HibernateSessionCommand<Object>() {
		@Override public void execute(Session session, Transaction transaction) throws Exception {
		    doDelete(objeto, session);
		}
	    });
	} else {
	    doDelete(objeto, session);
	}
    }

    private void doDelete(final T objeto, Session session) throws Exception {
	session.delete(objeto);
    }
    
    public void deleteById(final ID id) throws Exception {
	deleteById(id, null);
    }
    
    public void deleteById(final ID id, Session session) throws Exception {
	String hql = "delete from " + getGenericsClass().getName() + " where id = :id";
	DAOGenericoParameter param = new DAOGenericoParameter("id", id);
	executeHQL(session, hql, param);
    }
    
    /*public void executeSQL(String sql) {
	
	Session session = HibernateUtil.getSession();
	Transaction transaction = session.beginTransaction();
	
	try {
	    session.createSQLQuery(sql).executeUpdate();
	    transaction.commit();
	} catch (Exception e) {
	    transaction.rollback();
	    e.printStackTrace();
	    throw new RuntimeException();
	} finally {
	    session.close();
	}
    }*/
    
    public void executeHQL(final String hql, final DAOGenericoParameter... parameters) throws Exception {
	executeHQL(null, hql, parameters);
    }
    
    public void executeHQL(final Session session, final String hql, final DAOGenericoParameter... parameters) throws Exception {

	if (session == null) {
	    executeTransaction(new HibernateSessionCommand() { @Override public void execute(Session sess, Transaction transaction) {
		doExecuteHQL(sess, hql, parameters);
	    }});
	} else {
	    doExecuteHQL(session, hql, parameters);
	}
    }
    
    private void doExecuteHQL(Session session, String hql, DAOGenericoParameter... parameters) {
	Query query = session.createQuery(hql);
	for (DAOGenericoParameter param : parameters) {
	    query.setParameter(param.getKey(), param.getValue());
	}
	
	query.executeUpdate();
    }
    

    public List<T> executeSQLQuery(final String sql, final DAOGenericoParameter... parameters) throws Exception {
	return executeSQLQuery(null, sql, parameters);
    }
    
    public List<T> executeSQLQuery(Session session, final String sql, final DAOGenericoParameter... parameters) throws Exception {
	if (session == null) {
	    Object retorno = executeTransaction(new HibernateSessionCommand() {
		public Object executeComRetorno(Session sess, Transaction transaction) throws Exception {    
		    return doExecuteSQLQuery(sess, sql, parameters);
		}
	    });
	    return (List<T>) retorno;
	} else {
	    return doExecuteSQLQuery(session, sql, parameters);
	}
	
    }

    private List<T> doExecuteSQLQuery(Session session, final String sql, final DAOGenericoParameter... parameters) throws Exception {

	SQLQuery query = session.createSQLQuery(sql);
	for (DAOGenericoParameter param : parameters) {
	    query.setParameter(param.getKey(), param.getValue());
	}
	query.addEntity(getGenericsClass());

	return query.list();
    }
    
    public void executeSQL(final String sql, final DAOGenericoParameter... parameters) throws Exception {
	executeSQL(null, sql, parameters);
    }
    
    public void executeSQL(final Session session, final String hql, final DAOGenericoParameter... parameters) throws Exception {
	
	if (session == null) {
	    executeTransaction(new HibernateSessionCommand() { @Override public void execute(Session sess, Transaction transaction) {
		doExecuteSQL(sess, hql, parameters);
	    }});
	} else {
	    doExecuteSQL(session, hql, parameters);
	}
    }
    
    private void doExecuteSQL(Session session, String hql, DAOGenericoParameter... parameters) {
	Query query = session.createSQLQuery(hql);
	for (DAOGenericoParameter param : parameters) {
	    query.setParameter(param.getKey(), param.getValue());
	}
	
	query.executeUpdate();
    }
    
    public List<T> findAll() throws Exception {
	return executeQuery("from " + getGenericsClass().getName());
    }

    public T findByAttributesAndReturnUniqueResult(DAOGenericoParameter... attributesToFilter) throws Exception {
	return findByAttributesAndReturnUniqueResult(null, attributesToFilter);
    }
    
    public T findByAttributesAndReturnUniqueResult(Session session, DAOGenericoParameter... attributesToFilter) throws Exception {
	List<T> objetos = findByAttributes(session, attributesToFilter);
	return objetos.isEmpty() ? null : objetos.get(0);
    }
    
    public List<T> findByAttributes(DAOGenericoParameter... attributesToFilter) throws Exception {
	return findByAttributes(null, attributesToFilter);
    }
    public List<T> findByAttributes(Session session, DAOGenericoParameter... attributesToFilter) throws Exception {
	
	// -----------------
	String hql = "from " + getGenericsClass().getName();
	for (int i = 0; i < attributesToFilter.length; i++) {
	    
	    DAOGenericoParameter filterAttribute = attributesToFilter[i];
	    if (i == 0) {
		hql += " where " + filterAttribute.getKey() + " = :" + filterAttribute.getKey();
	    } else {
		hql += " and " + filterAttribute.getKey() + " = :" + filterAttribute.getKey();
	    }
	}
	
	// -----------------
	return executeQuery(session, hql, attributesToFilter);
    }
    
    public void deleteByAttributes(DAOGenericoParameter... attributesToFilter) throws Exception {
	deleteByAttributes(null, attributesToFilter);
    }
    
    public void deleteByAttributes(Session session, DAOGenericoParameter... attributesToFilter) throws Exception {
	
	// -----------------
	String hql = "delete from " + getGenericsClass().getName();
	for (int i = 0; i < attributesToFilter.length; i++) {
	    
	    DAOGenericoParameter filterAttribute = attributesToFilter[i];
	    if (i == 0) {
		hql += " where " + filterAttribute.getKey() + " = :" + filterAttribute.getKey();
	    } else {
		hql += " and " + filterAttribute.getKey() + " = :" + filterAttribute.getKey();
	    }
	}
	
	// -----------------
	executeHQL(session, hql, attributesToFilter);
    }
    
    public Long getTotal() throws Exception {
	return (Long) executeQuery("select count(obj) from " + getGenericsClass().getName() + " obj").get(0);
    }
    
    public T executeQueryAndReturnUniqueResult(String hql, DAOGenericoParameter... parameters) throws Exception {
	return executeQueryForDiferentTypeAndReturnUniqueResult(getGenericsClass(), hql, parameters);
    }
    
    public List<T> executeQuery(String hql, DAOGenericoParameter... parameters) throws Exception {
	return executeQuery(null, hql, parameters);
    }
    public List<T> executeQuery(Session session, String hql, DAOGenericoParameter... parameters) throws Exception {
	return executeQueryForDiferentType(session, getGenericsClass(), hql, parameters);
    }

    public <DiferentType> DiferentType executeQueryForDiferentTypeAndReturnUniqueResult(Class<DiferentType> type, String hql, DAOGenericoParameter... parameters ) throws Exception {
	
	List<DiferentType> resultList = executeQueryForDiferentType(type, hql, parameters);
	return resultList.isEmpty() ? null : resultList.get(0);
    }
    
    public <DiferentType> List<DiferentType> executeQueryForDiferentType(final Class<DiferentType> type, final String hql, final DAOGenericoParameter... parameters) throws Exception {
	return executeQueryForDiferentType(null, type, hql, parameters);
    }
    public <DiferentType> List<DiferentType> executeQueryForDiferentType(final Session session, final Class<DiferentType> type, final String hql, final DAOGenericoParameter... parameters) throws Exception {
	
	if (session == null) {
	    Object retorno = executeTransaction(new HibernateSessionCommand() {
		public List<DiferentType> executeComRetorno(Session sess, Transaction transaction) {
		    return doExecuteQueryForDiferentType(sess, type, hql, parameters);
		}
	    });
	    return (List<DiferentType>) retorno;
	} else {
	    return doExecuteQueryForDiferentType(session, type, hql, parameters);
	}
    }
    
    private <DiferentType> List<DiferentType> doExecuteQueryForDiferentType(Session session, Class<DiferentType> type, String hql, DAOGenericoParameter... parameters ) {
	
	Query query = session.createQuery(hql);
	for (DAOGenericoParameter param : parameters) {
	    query.setParameter(param.getKey(), param.getValue());
	}
	
	List<DiferentType> list = query.list();
	return list;
    }
    
    public T findById(ID id) {
	if (id == null) {
	    return null;
	}
	Session session = HibernateUtil.getSession();
	T objectFinded = (T) session.get(getGenericsClass(), id);
	session.close();

	return objectFinded;
    }

}



















