package com.tecurti.model.persistencia.dao.gae; 

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.tecurti.model.persistencia.dao.gae.entidades.EntityGae;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO.CodigoTentandoNovamenteQuandoConcurrentModificationException;

public abstract class DAOGenericoGAE<T extends EntityGae, AtributosDaClasse> {
    
    private static Logger logger = Logger.getLogger(DAOGenericoGAE.class.getName());
    
    protected AtributosDaClasse attr;

    ////////////////////// INSERT-UPDATE ////////////////////// 
    public void insertOrUpdate(final T object) throws Exception {
	insertOrUpdate(object, null, null);
	if (object.getParentKey() == null) {
	    UtilsGaeDAO.waitForUpdateOf(object.toEntityGae());
	}
    }
    public void insertOrUpdate(T object, DatastoreService datastore, Transaction txn) throws Exception {
	if (object.getKey() == null) {
	    insert(object, datastore, txn);
	} else {
	    update(object, datastore, txn);
	}
    }
    
    ////////////////////// INSERT ////////////////////// 
    public void insert(final T object) throws Exception {
	insert(object, true);
    }
    public void insert(final T object, boolean waitCommit) throws Exception {
	insert(object, null, null);
	if (object.getParentKey() == null && waitCommit) {
	    UtilsGaeDAO.waitForInsertingOf(object.getKey());
	} 
    }
    
    public void insert(final T object, DatastoreService datastore, Transaction txn) throws Exception {
	
	final Entity entity = object.toEntityGae();

	if (datastore != null) {
	    Key key = datastore.put(txn, entity);
	    object.setKey(key);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    Key key = DatastoreServiceFactory.getDatastoreService().put(null, entity);
		    object.setKey(key);
		}
	    });
	}
	
	/*datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	
	Entity entity = object.toEntityGae();
	Key key = datastore.put(txn, entity);
	object.setKey(key);*/
    }

    @SuppressWarnings("unchecked")
    public void insert(List<T> listObjects, DatastoreService datastore, Transaction txn) throws Exception {

	// ------------------
	final List<Entity> listEntities = toEntityList(listObjects);

	// ------------------
	List<Key> listKeys;
	if (datastore != null) {
	    listKeys = datastore.put(txn, listEntities);
	} else {
	    listKeys = (List<Key>) UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public Object executeComRetorno() {
		    return DatastoreServiceFactory.getDatastoreService().put(null, listEntities);
		}
	    });
	}
	
	// ------------------
	for (int i = 0; i < listKeys.size(); i++) {
	    Key key = listKeys.get(i);
	    T object = listObjects.get(i);
	    
	    object.setKey(key);
	}
	
	/*List<Entity> listEntities = new ArrayList<Entity>();
	for (T t : listObjects) {
	    listEntities.add(t.toEntityGae());
	}
	
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	List<Key> listKeys = datastore.put(txn, listEntities);
	for (int i = 0; i < listKeys.size(); i++) {
	    Key key = listKeys.get(i);
	    T object = listObjects.get(i);
	    
	    object.setKey(key);
	}*/
    }

    ////////////////////// UPDATE ////////////////////// 
    public void update(final T... arrayObjects) throws Exception {
	
	if (arrayObjects.length == 0) {
	    return;
	}
	
	update(null, null, arrayObjects);
	if (arrayObjects[0].getParentKey() == null) {
	    UtilsGaeDAO.waitForUpdateOf(arrayObjects);
	}
    }
    
    public void update(DatastoreService datastore, Transaction txn, T... arrayObjects) throws Exception {
	
	List<T> list = new ArrayList<T>();
	for (T t : arrayObjects) {
	    list.add(t);
	}
	update(list, datastore, txn);
    }
    
    public void update(List<T> listObjects) throws Exception {
	update(listObjects, null, null);
    }
    
    public void update(List<T> listObjects, DatastoreService datastore, Transaction txn) throws Exception {
	
	if (listObjects.isEmpty()) {
	    return;
	}
	
	final List<Entity> listEntities = toEntityList(listObjects);
	
	if (datastore != null) {
	    datastore.put(txn, listEntities);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    DatastoreServiceFactory.getDatastoreService().put(null, listEntities);
		}
	    });
	}
	
	/*List<Entity> listEntities = new ArrayList<Entity>();
	for (T t : listObjects) {
	    listEntities.add(t.toEntityGae());
	}
	
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	datastore.put(txn, listEntities);*/
    }
    private List<Entity> toEntityList(List<T> listObjects) {
	
	List<Entity> listEntities = new ArrayList<Entity>();
	for (T t : listObjects) {
	    listEntities.add(t.toEntityGae());
	}
	return listEntities;
    }

    public void update(T entityGae, DatastoreService datastore, Transaction txn) throws Exception {

	final Entity entity = entityGae.toEntityGae();

	if (datastore != null) {
	    datastore.put(txn, entity);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    DatastoreServiceFactory.getDatastoreService().put(null, entity);
		}
	    });
	}
    }

    public void updateTxn(final Key key, DatastoreService datastore, Transaction txn, final CampoNomeValor... listCamposParaAlterar) throws Exception {
	
	if (datastore != null) {
	    doUpdateTxn(key, datastore, txn, listCamposParaAlterar);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() throws EntityNotFoundException {
		    doUpdateTxn(key, DatastoreServiceFactory.getDatastoreService(), null, listCamposParaAlterar);
		}
	    });
	}
	
	/*datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	Entity entity = datastore.get(txn, key);

	for (CampoNomeValor nomeValor : listCamposParaAlterar) {
	    entity.setProperty(nomeValor.getNome(), nomeValor.getValueConvertidoParaFiltroGae());
	}
	
	datastore.put(txn, entity);*/
    }
    
    private void doUpdateTxn(Key key, DatastoreService datastore, Transaction txn, CampoNomeValor... listCamposParaAlterar) throws EntityNotFoundException {
	final Entity entity = datastore.get(txn, key);
	for (CampoNomeValor nomeValor : listCamposParaAlterar) {
	    entity.setProperty(nomeValor.getNome(), nomeValor.getValueConvertidoParaFiltroGae());
	}
	datastore.put(txn, entity);
    }
    
    public void update(final QueryGae queryGae, final CampoNomeValor... listCamposParaAlterar) throws Exception {
	List<Entity> entidadesAtualizadas = updateTxn(queryGae, null, null, listCamposParaAlterar);
	if (queryGae.getParentKey() == null) {
	    UtilsGaeDAO.waitForUpdateOf(entidadesAtualizadas);
	}
    }
    
    public List<Entity> updateTxn(QueryGae queryGae, DatastoreService datastore, Transaction txn, CampoNomeValor... listCamposParaAlterar) throws Exception {
	
	final List<Entity> listEntity = queryGae.findEntityList(datastore, txn);
	
	for (CampoNomeValor nomeValor : listCamposParaAlterar) {
	    for (Entity entity : listEntity) {
		entity.setProperty(nomeValor.getNome(), nomeValor.getValueConvertidoParaFiltroGae());
	    }
	}
	
	// ------------------
	if (datastore != null) {
	    datastore.put(txn, listEntity);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    DatastoreServiceFactory.getDatastoreService().put(null, listEntity);
		}
	    });
	}
	
	// ------------------
	return listEntity;
	
	/*List<Entity> listEntity = queryGae.findEntityList(datastore, txn);
	
	for (CampoNomeValor nomeValor : listCamposParaAlterar) {
	    for (Entity entity : listEntity) {
		entity.setProperty(nomeValor.getNome(), nomeValor.getValueConvertidoParaFiltroGae());
	    }
	}
	
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	datastore.put(txn, listEntity);
	
	return listEntity;*/
    }

    ////////////////////// DELETE ////////////////////// 
    public List<Key> deleteAll() throws Exception {
	return deleteAll(false);
    }
    
    public List<Key> deleteAll(boolean waitForDeleting) throws Exception {
	return UtilsGaeDAO.deleteAll(getTClass(), waitForDeleting);
    }

    public Key delete(Key key) throws Exception {
	return delete(key, null, null);
    }
    
    public Key deleteByKey(String keyAsString, DatastoreService datastore, Transaction txn) throws Exception {
	Key key;
	try {
	    key = KeyFactory.stringToKey(keyAsString);
	} catch (Exception e) {
	    return null;
	}
	return delete(key, datastore, txn);
    }
    
    public Key delete(final Key key, DatastoreService datastore, Transaction txn) throws Exception {

	if (datastore != null) {
	    datastore.delete(txn, key);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    Transaction txnNull = null;
		    DatastoreServiceFactory.getDatastoreService().delete(txnNull, key);
		}
	    });
	}
	
	return key;
	
	/*datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	datastore.delete(txn, key);
	return key;*/
    }
    
    public List<Key> delete(final QueryGae queryGae) throws Exception {
	return delete(queryGae, true);
    }
    public List<Key> delete(final QueryGae queryGae, boolean waitCommit) throws Exception {
	
	List<Key> deletedKeys = delete(queryGae, null, null);
	if (queryGae.getParentKey() == null && waitCommit) {
	    UtilsGaeDAO.waitForRemovingOf(deletedKeys);
	}
	return deletedKeys;
    }
    
    public List<Key> delete(final QueryGae queryGae, final DatastoreService datastore, Transaction txn) throws Exception {

	queryGae.setKeysOnly();
	List<Entity> listEntidadesParaRemover = queryGae.findEntityList(datastore, txn);
	return deleteByEntityList(listEntidadesParaRemover, datastore, txn);
    }
    
    public List<Key> deleteByEntityList(final List<Entity> listEntity, final DatastoreService datastore, Transaction txn) throws Exception {
	List<Key> listKeys = UtilsGaeDAO.toListKeys(listEntity);
	return deleteByKeyList(listKeys, datastore, txn);
    }
    
    public List<Key> deleteByKeyList(final List<Key> listKeys, DatastoreService datastore, Transaction txn) throws Exception {
	
	if (datastore != null) {
	    datastore.delete(txn, listKeys);
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    DatastoreServiceFactory.getDatastoreService().delete(null, listKeys);
		}
	    });
	}
	return listKeys;
    }
    
    public void delete(final List<T> listObjetos) throws Exception {
	delete(listObjetos, true);
    }
    
    public void delete(final List<T> listObjetos, boolean waitCommit) throws Exception {
	
	if (listObjetos.isEmpty()) {
	    return;
	}
	
	List<Key> deletedKeys = delete(listObjetos, null, null);
	if (listObjetos.get(0).getParentKey() == null && waitCommit) {
	    UtilsGaeDAO.waitForRemovingOf(deletedKeys);
	}
    }
    public List<Key> delete(List<T> listObjetos, DatastoreService datastore, Transaction txn) throws Exception {

	List<Key> keys = new ArrayList<Key>();
	for (T objeto : listObjetos) {
	    keys.add(objeto.getKey());    
	}
	
	return deleteByKeyList(keys, datastore, txn);
    }
    
    public void delete(final T object) throws Exception {
	delete(object, null, null);
    }
    
    public void delete(final T object, DatastoreService datastore, Transaction txn) throws Exception {
	if (object == null) {
	    return;
	}
	
	if (datastore != null) {
	    datastore.delete(txn, object.getKey());
	} else {
	    UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
		public void execute() {
		    Transaction txnNull = null;
		    DatastoreServiceFactory.getDatastoreService().delete(txnNull, object.getKey());
		}
	    });
	}
	
	/*datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	datastore.delete(txn, object.getKey());*/
    }
    
    ////////////////////// QUERY ////////////////////// 
    public T findByKey(final String keyAsString, DatastoreService datastore, Transaction txn) throws Exception {
	Key key;
	try {
	    key = KeyFactory.stringToKey(keyAsString);
	} catch (Exception e) {
	    return null;
	}
	return findById(key, datastore, txn);
    }
    public T findByKey(final String keyAsString) throws Exception {
	return findByKey(keyAsString, null, null);
    }
    public T findById(final Key key) throws Exception {
	return findById(key, null, null);
    }
    
    public Map<Key, T> findByKeyList(Collection<Key> listKeys) {
	return findById(listKeys, null, null);
    }
    
    public Map<Key, T> findById(Collection<Key> listKeys, DatastoreService datastore, Transaction txn) {

	try {
	    Map<Key, Entity> mapEntity = findEntityByKeys(listKeys, datastore, txn);
	    
	    Map<Key, T> mapRetorno = new HashMap<Key, T>();
	    for (Key key : listKeys) {
		Entity entity = mapEntity.get(key);
		T objeto = criarObjeto(entity);
		
		mapRetorno.put(key, objeto);
	    }
	    
	    return mapRetorno;
	    
	} catch (EntityNotFoundException e) {
	    return null;
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	} 
    }
    
    public T findById(Key key, DatastoreService datastore, Transaction txn) {
	if (key == null) {
	    return null;
	}
	try {
	    Entity entity = findEntityById(key, datastore, txn);
	    return criarObjeto(entity);
	} catch (EntityNotFoundException e) {
	    return null;
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	} 
    }

    public Entity findEntityById(Key key, DatastoreService datastore, Transaction txn) throws Exception {
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	return datastore.get(txn, key);
    }
    
    public Map<Key, Entity> findEntityByKeys(Collection<Key> listKeys, DatastoreService datastore, Transaction txn) throws Exception {
	if (listKeys.isEmpty()) {
	    return new HashMap<>();
	}
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	return datastore.get(txn, listKeys);
    }

    public T findBy(final QueryGae queryGae) throws Exception {
	return findBy(queryGae, null, null);
    }
    public T findBy(QueryGae queryGae, DatastoreService datastore, Transaction txn) throws Exception {
	queryGae.setLimiteValoresRetornados(1);
	List<T> listEncontrada = findListBy(queryGae, datastore, txn);
	
	return listEncontrada.isEmpty() ? null : listEncontrada.get(0);
    }
    
    public List<T> findAll() throws Exception {
	return findAll(null);
    }
    public List<T> findAll(DatastoreService datastore, Transaction txn) throws Exception {
	return findAll(null, datastore, txn);
    }
    
    public List<T> findAll(Integer limite) throws Exception {
	return findAll(limite, null, null);
    }
    public List<T> findAll(Integer limite, DatastoreService datastore, Transaction txn) throws Exception {
	
	QueryGae queryGae = new QueryGae(getTClass(), null);
	if (limite != null) {
	    queryGae.setLimiteValoresRetornados(limite);
	}
	return findListBy(queryGae, datastore, txn);
    }
    
    public List<T> findListBy(final QueryGae queryGae) throws Exception {
	return findListBy(queryGae, null, null);
    }
    
    public List<T> findListBy(QueryGae queryGae, DatastoreService datastore, Transaction txn) throws Exception {
	
	List<Entity> entityList = queryGae.findEntityList(datastore, txn);
	
	List<T> conteudolist = criarListObjetos(queryGae, entityList);
	
	return conteudolist;
    }

    ////////////////////// AUXILIARES ////////////////////// 
    protected List<T> criarListObjetos(List<Entity> entityList) {

	List<T> list = new ArrayList<T>();
	for (Entity entity : entityList) {
	    list.add(criarObjeto(entity));
	}

	return list;
    }

    public List<T> criarListObjetos(QueryGae queryGae, List<Entity> entityList) throws InstantiationException, IllegalAccessException, Exception {
	
	if (queryGae.existeCamposProjectionsCadastrados()) {
	    
	    List<T> conteudoList = new ArrayList<T>();
	    for (Entity entity : entityList) {
		
		T t = getTClass().newInstance();
		t.setKey(entity.getKey());
		for (CampoPojoDetail campo : queryGae.getPojoDetails().getTodosCampos()) {
		    
		    boolean devePreencher = queryGae.isProjectionField(campo.getName());
		    if (devePreencher) {
			if (queryGae.isFiltroDeIgualdade(campo.getName())) {
			    Object valor = queryGae.getValorOriginalDoCampoQueEhFiltroDeIgualdade(campo.getName());
			    campo.setValorJavaNoObjetoJava(t, valor);
			} else {
			    campo.setValorNoObjetoFromEntity(t, entity);
			}
		    } 
		}
		
		conteudoList.add(t);
	    }
	    return conteudoList;
	} else {
	    return criarListObjetos(entityList);
	}
    }

    public List<T> criarListObjetos(Iterable<Entity> asIterable) {
	
	List<T> list = new ArrayList<T>();
	for (Entity entity : asIterable) {
	    list.add(criarObjeto(entity));
	}
	return list;
    }

    protected T criarObjeto(Entity entity) {
	if (entity == null) {
	    return null;
	}
	try {
	    T t = getTClass().newInstance();
	    t.setClassFields(entity);
	    return t;
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public Class<T> getTClass() {

	// ---------------------
	Class directSubclass = getClass();
	while (directSubclass.getSuperclass().equals(DAOGenericoGAE.class) == false) {
	    directSubclass = directSubclass.getSuperclass();
	}

	// ---------------------
	return (Class<T>) ((ParameterizedType) directSubclass.getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public boolean exists(Key key, DatastoreService datastore, Transaction txn) {
	return findById(key, datastore, txn) != null;
    }
    
}

































