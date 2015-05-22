package com.tecurti.model.persistencia.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.ResultTransformer;

import com.tecurti.model.persistencia.dao.hibernate.resulttransformer.ResultTransformerForMap;

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
	Transaction transaction = null;
	
	try {
	    transaction = session.beginTransaction();
	    
	    command.execute(session, transaction);
	    T retorno = command.executeComRetorno(session, transaction);
	    transaction.commit();
	    
	    return retorno;
	} catch (Exception e) {
	    if (transaction != null) {
		transaction.rollback();
	    }
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
	    executeTransaction(new HibernateSessionCommand<Object>() { @Override public I executeComRetorno(Session session, Transaction transaction) {
		return doInsert(objeto, session);
	    }});
	} else {
	     return doInsert(objeto, session);
	}
	return null;
    }

    private <I>I doInsert(final T objeto, Session session) {
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
    
    public void deleteAll() throws Exception {
	String hql = "delete from " + getGenericsClass().getName();
	executeHQL(null, hql, null);
    }
    
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
	
	if (parameters != null) {
	    for (DAOGenericoParameter param : parameters) {
		query.setParameter(param.getKey(), param.getValue());
	    }
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
	incluirParametrosNaQuery(query, parameters);
	query.addEntity(getGenericsClass());

	return query.list();
    }
    
    public List<Map<String, Object>> executeSQLQueryForMapResult(String sql, DAOGenericoParameter... parameters) throws Exception {
	return executeSQLQueryForMapResult(null, sql, parameters);
    }
    public List<Map<String, Object>> executeSQLQueryForMapResult(Session session, String sql, DAOGenericoParameter... parameters) throws Exception {
	return executeSQLQueryForDifferentResultType(session, sql, ResultTransformerForMap.RESULT_TRANSFORMER, parameters);
    }
    public List executeSQLQueryForDifferentResultType(String sql, ResultTransformer resultTransformer, DAOGenericoParameter... parameters) throws Exception {
	return executeSQLQueryForDifferentResultType(null, sql, resultTransformer, parameters);
    }
    public List executeSQLQueryForDifferentResultType(Session session, final String sql, final ResultTransformer resultTransformer, final DAOGenericoParameter... parameters) throws Exception {
	if (session == null) {
	    Object retorno = executeTransaction(new HibernateSessionCommand() {
		public Object executeComRetorno(Session sess, Transaction transaction) throws Exception {    
		    return doExecuteSQLQueryForDifferentResultType(sess, sql, resultTransformer, parameters);
		}
	    });
	    return (List) retorno;
	} else {
	    return doExecuteSQLQueryForDifferentResultType(session, sql, resultTransformer, parameters);
	}
    }
    private List doExecuteSQLQueryForDifferentResultType(Session session, final String sql, ResultTransformer resultTransformer, final DAOGenericoParameter... parameters) throws Exception {
	
	SQLQuery query = session.createSQLQuery(sql);
	incluirParametrosNaQuery(query, parameters);
	query.setResultTransformer(resultTransformer);
	
	return query.list();
    }

    private void incluirParametrosNaQuery(SQLQuery query, final DAOGenericoParameter... parameters) {
	for (DAOGenericoParameter param : parameters) {
	    if (param.getValue() instanceof Collection) {
		query.setParameterList(param.getKey(), (Collection) param.getValue());
	    } else {
		query.setParameter(param.getKey(), param.getValue());
	    }
	}
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
	    return executeTransaction(new HibernateSessionCommand<List<DiferentType>>() {
		public List<DiferentType> executeComRetorno(Session sess, Transaction transaction) {
		    return doExecuteQueryForDiferentType(sess, type, hql, parameters);
		}
	    });
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
    
    
    public T findById(final ID id) throws Exception {
	return findById(id, null);
    }
    public T findById(final ID id, Session session) throws Exception {
	if (id == null) {
	    return null;
	}
	
	// ----------------
	if (session == null) {
	    return executeTransaction(new HibernateSessionCommand<T>() {
		@Override
		public T executeComRetorno(Session session, Transaction transaction) {
		    return doFindById(id, session);
		}

	    });
	} else {
	    return doFindById(id, session);
	}
    }
    
    private T doFindById(final ID id, Session session) {
	T objectFinded = (T) session.get(getGenericsClass(), id);
	return objectFinded;
    }

}



















