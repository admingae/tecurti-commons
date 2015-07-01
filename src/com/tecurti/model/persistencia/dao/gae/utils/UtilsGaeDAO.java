package com.tecurti.model.persistencia.dao.gae.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.tecurti.model.persistencia.dao.gae.CodigoTransacional;
import com.tecurti.model.persistencia.dao.gae.QueryGae;
import com.tecurti.model.persistencia.dao.gae.entidades.EntityGae;
import com.tecurti.model.persistencia.dao.gae.entidades.SequenceGae;

public class UtilsGaeDAO {
    
    private static int WAIT_TIMEOUT_MILLI = 15000;
    private static Logger logger = Logger.getLogger(UtilsGaeDAO.class.getName());
    
    public static List<Key> deleteAll(final Class klass) throws Exception {
	return deleteAll(klass, false);
    }
    
    public static List<Key> deleteAll(final Class klass, final boolean waitDelete) throws Exception {

	final List<Key> listKeys = new QueryGae(klass, null).findKeys(null, null);
	
	UtilsGaeDAO.executarCodigoTentandoNovamenteQuandoConcurrentModificationException(new CodigoTentandoNovamenteQuandoConcurrentModificationException() {
	    public void execute() throws Exception {
		DatastoreServiceFactory.getDatastoreService().delete(listKeys);
		if (waitDelete) {
		    waitForRemovingOf(listKeys);
		}
	    }
	});
	return listKeys;
    }
    
    public static List<Key> toListKeys(Collection<Entity> listEntity) {
	
	List<Key> keys = new ArrayList<Key>(listEntity.size());
	for (Entity entity : listEntity) {
	    keys.add(entity.getKey());
	}
	
	return keys;
    }
    
    public static void waitForRemovingOf(final String kind) throws Exception {

	long timeInitial = System.currentTimeMillis();
	while (true) {
	    Query query = new Query(kind);
	    Iterator<Entity> iterator = DatastoreServiceFactory.getDatastoreService().prepare(query).asIterator();
	    if (iterator.hasNext() == false) {
		return;
	    } else {
		long timePassed = System.currentTimeMillis() - timeInitial;
		if (timePassed > WAIT_TIMEOUT_MILLI) {
		    throw new TimeoutException("Kind: " + kind);
		}

		logger.info("Waiting for deleting of " + kind + "");
		Thread.sleep(100);
	    }
	}
    }
    
    public static void waitForRemovingOf(final Key key) throws Exception {

	long timeInitial = System.currentTimeMillis();
	while (true) {
	    try {

		long timePassed = System.currentTimeMillis() - timeInitial;
		if (timePassed > WAIT_TIMEOUT_MILLI) {
		    throw new TimeoutException("key: " + key);
		}

		Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
		logger.info("Waiting for removing of " + key.getKind() + "[" + key.getId() + "]");
		Thread.sleep(100);

	    } catch (EntityNotFoundException e) {
		return;
	    }
	}
    }
    
    public static Object transformarBlobEmObject(Blob blob) {
	
	try {
	    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blob.getBytes());
	    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
	    
	    Object object = objectInputStream.readObject();
	    objectInputStream.close();
	    
	    return object;
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static Blob transformarObjectEmBlob(Object object) {
	
	try {
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	    objectOutputStream.writeObject(object);
	    objectOutputStream.flush();
	    objectOutputStream.close();
	    byte[] byteArray = byteArrayOutputStream.toByteArray();
	    
	    return new Blob(byteArray);
	} catch (IOException e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    public static void waitForInsertingOf(List<Key> listKeys) throws Exception {
	for (Key key : listKeys) {
	    waitForInsertingOf(key);
	}
    }
    
    public static void waitForInsertingOf(final Key key) throws Exception {

	long timeInitial = System.currentTimeMillis();
	while (true) {
	    try {
		Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
		return;
	    } catch (EntityNotFoundException e) {

		long timePassed = System.currentTimeMillis() - timeInitial;
		if (timePassed > WAIT_TIMEOUT_MILLI) {
		    throw new TimeoutException("key: " + key);
		}

		logger.info("Waiting for inserting of " + key.getKind() + "[" + key.getId() + "]");
		Thread.sleep(100);
	    }
	}
    }
    
    public static void waitForUpdateOf(final Entity entity) throws Exception {
	long timeInitial = System.currentTimeMillis();
	while (true) {
	    Entity persistentEntity = DatastoreServiceFactory.getDatastoreService().get(entity.getKey());
	    if (persistentEntity.equals(entity)) {
		return;
	    } else {

		long timePassed = System.currentTimeMillis() - timeInitial;
		if (timePassed > WAIT_TIMEOUT_MILLI) {
		    throw new TimeoutException("entity: " + entity);
		}

		logger.info("Waiting for update of " + entity.getKey().getKind() + "[" + entity.getKey().getId() + "]");
		Thread.sleep(100);
	    }
	}
    }
    
    public static Date calendarToDate(Calendar calendar) {
	return calendar != null ? calendar.getTime() : null;
    }
    
    public static Calendar getCalendarProperty(Entity entity, String propertyName) {

	if (entity.getProperty(propertyName) != null) {
	    GregorianCalendar gregorianCalendar = new GregorianCalendar();
	    gregorianCalendar.setTime((Date) entity.getProperty(propertyName));
	    return gregorianCalendar;
	} else {
	    return null;
	}
    }
    
    public static boolean getBooleanProperty(Entity entity, String propertyName) {
	
	if (entity.getProperty(propertyName) != null) {
	    return (boolean) entity.getProperty(propertyName);
	} else {
	    return false;
	}
    }

    public static void waitForRemovingOf(List<Key> listKeysParaRemover) throws Exception {
	
	for (Key key : listKeysParaRemover) {
	    waitForRemovingOf(key);
	}
    }

    public static void waitForRemovingEntitiesOf(List<Entity> asList) throws Exception {
	
	List<Key> keys = new ArrayList<Key>(asList.size());
	for (Entity entity : asList) {
	    keys.add(entity.getKey());
	}
	
	waitForRemovingOf(keys);
    }

    public static void waitForUpdateOf(EntityGae... entitiesGae) throws Exception {
	List<Entity> listEntity = new ArrayList<Entity>(entitiesGae.length);
	for (EntityGae entityGae : entitiesGae) {
	    listEntity.add(entityGae.toEntityGae());
	}
	
	waitForUpdateOf(listEntity);
    }
    
    public static void waitForUpdateOf(List<Entity> asIterable) throws Exception {
	
	for (Entity entity : asIterable) {
	    waitForUpdateOf(entity);
	}
    }
    
    public static Class converterClassJavaParaClassGae(Class<?> classParaObterClassGae) {
	
	if (classParaObterClassGae.isEnum()) {
	    return String.class;
	}
	if (Calendar.class.isAssignableFrom(classParaObterClassGae)) {
	    return Date.class;
	}
	if (boolean.class.equals(classParaObterClassGae)) {
	    return Boolean.class;
	}
	if (float.class.equals(classParaObterClassGae) 
		|| Float.class.equals(classParaObterClassGae)
		|| double.class.equals(classParaObterClassGae)) {
	    return Double.class;
	}
	if (byte[].class.equals(classParaObterClassGae)) {
	    return Blob.class;
	}
	if (byte.class.equals(classParaObterClassGae) || Byte.class.equals(classParaObterClassGae)
		|| short.class.equals(classParaObterClassGae) || Short.class.equals(classParaObterClassGae)
		|| int.class.equals(classParaObterClassGae) || Integer.class.equals(classParaObterClassGae)
		|| long.class.equals(classParaObterClassGae)) {
	    return Long.class;
	}
	
	return classParaObterClassGae;
    }

    public static SequenceGaeDAO sequenceGaeDAO = new SequenceGaeDAO();
    public static Long getNextValueForSequence(final String sequence) throws Exception {
	
	SequenceGae sequenceGae = sequenceGaeDAO.findById(SequenceGae.createKey(sequence));
	if (sequenceGae == null) {
	    sequenceGae = new SequenceGae();
	    sequenceGae.setNameId(sequence);
	    sequenceGae.setCount(1L);
	} else {
	    sequenceGae.setIncrementCount();
	}
	
	sequenceGaeDAO.insertOrUpdate(sequenceGae);
	return sequenceGae.getCount();
    }
    
    public static <T> T executarCodigoTransacional(CodigoTransacional<T> codigoParaExecutar) throws Exception {
	return executarCodigoTransacional(false, codigoParaExecutar);
    }
    
    public static <T> T executarCodigoTransacionalXG(CodigoTransacional<T> codigoParaExecutar) throws Exception {
	return executarCodigoTransacional(true, codigoParaExecutar);
    }
    
    public static <T> T executarCodigoTransacional(boolean xgTransaction, final CodigoTransacional<T> codigoParaExecutar) throws Exception {
	
	while (true) {
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    Transaction transaction = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(xgTransaction));
	    
	    try {
		
		codigoParaExecutar.execute(datastore, transaction);
		T retorno = codigoParaExecutar.executeComRetorno(datastore, transaction);
	        
		transaction.commit();
	        return retorno;
	    } catch (ConcurrentModificationException e) {
		int debugBreakpoint = 0; 
		logger.log(Level.WARNING, "UtilsGaeDAO.executarCodigoTransacional() ConcurrentModificationException >> " + e.toString());
		Thread.sleep(new Random().nextInt(100));
		// tenta novamente
	    } catch (Exception e) {
		logger.log(Level.SEVERE, "", e);
		throw e;
	    } finally {
	        if (transaction.isActive()) {
	            transaction.rollback();
	        }
	    }
	}
    }
    
    public static Object executarCodigoTentandoNovamenteQuandoConcurrentModificationException(CodigoTentandoNovamenteQuandoConcurrentModificationException codigoParaExecutar) throws Exception {

	while (true) {
	    
	    try {
	        codigoParaExecutar.execute();
	        return codigoParaExecutar.executeComRetorno();
	    } catch (ConcurrentModificationException e) {
		
		logger.log(Level.WARNING, "", e);
		Thread.sleep(new Random().nextInt(100));
		// tenta novamente
	    }
	}
    }
    
    public static class CodigoTentandoNovamenteQuandoConcurrentModificationException {
	public void execute() throws Exception {}
	public Object executeComRetorno() throws Exception {return null;}
    }

    public static Entity instanciarEntity(EntityGae entityGae) {
	
	Key key = entityGae.getKey();
	if (key != null) {
	    return new Entity(key);
	} else {
	    Key keyParent = entityGae.getParentKey();
	    String kind = UtilsGaeDAO.getKIND(entityGae.getClass());
	    return new Entity(kind, keyParent);
	}
    }
    
    public static void addFilter(Query query, FilterPredicate novoFilter) {
	Filter filtroDaQuery = query.getFilter();
	
	if (filtroDaQuery == null) {
	    query.setFilter(novoFilter);
	} else {
	    query.setFilter(CompositeFilterOperator.and(filtroDaQuery, novoFilter));
	}
    }

    public static String getKIND(Class klass) {
	return klass.getSimpleName();
    }

    public static Key createKey(String parentKind, Object parentIdOrName, String kind, Object idOrName) {
	
	Key parentKey = createKey(null, parentKind, parentIdOrName);
	Key key = createKey(parentKey, kind, idOrName);
	return key;
    }
    
    public static Key createKey(String kind, Object idOrName) {
	return createKey(null, kind, idOrName);
    }
    
    public static Key createKey(Key parentKey, String kind, Object idOrName) {
	
	// ----------------
	if (idOrName == null) {
	    return null;
	}
	
	// ----------------
	if (idOrName instanceof String) {
	    String name = (String) idOrName;
	    return KeyFactory.createKey(parentKey, kind, name);
	    
	} else if (idOrName instanceof Long) {
	    Long id = (Long) idOrName;
	    return KeyFactory.createKey(parentKey, kind, id);
	    
	} else {
	    throw new RuntimeException("["+idOrName+"] deve ser do tipo String ou Long");
	}
    }

    public static List<String> keysToStrings(List<Key> listKeys) {
	List<String> listString = new ArrayList<String>();
	for (Key key : listKeys) {
	    listString.add(KeyFactory.keyToString(key));
	}
	return listString;
    }

    public static Object enumToString(Enum enumerado) {
	return enumerado == null ? null : enumerado.toString();
    }
    
    public static List<String> listEnumToListString(List enumerado) {
	List<String> listString = new ArrayList<String>();
	for (Object object : enumerado) {
	    listString.add(object.toString());
	}
	return listString;
    }
    
    public static <T> List<T> arrayStringToListEnum(Class<T> classeEnumerado, String[] arrayEnumeradoAsString) {
	if (arrayEnumeradoAsString == null) {
	    return new ArrayList<T>();
	}
	List<String> arrayAsList = Arrays.asList(arrayEnumeradoAsString);
	return listStringToListEnum(classeEnumerado, arrayAsList);
    }
    public static <T> List<T> listStringToListEnum(Class<T> classeEnumerado, List<String> listEnumeradoAsString) {
	List<T> list = new ArrayList<>();
	for (String string : listEnumeradoAsString) {
	    T valorEnum = stringToEnum(classeEnumerado, string);
	    if (valorEnum != null) {
		list.add(valorEnum);
	    } 
	}
	return list;
    }
    public static <T> List<T> listStringToListEnum(Class<T> classeEnumerado, Entity entity, String nomePropriedade) {
	Object valor = entity.getProperty(nomePropriedade);
	if (valor == null) {
	    return null;
	}
	
	return listStringToListEnum(classeEnumerado, (List)valor);
    }
    
    public static <T> T stringToEnum(Class<T> classeEnumerado, Entity entity, String nomePropriedade) {
	Object valor = entity.getProperty(nomePropriedade);
	return stringToEnum(classeEnumerado, valor);
    }
    
    public static <T> T stringToEnum(Class<T> classeEnumerado, Object enumeradoAsObject) {
	if (enumeradoAsObject == null || enumeradoAsObject.toString().isEmpty()) {
	    return null;
	}

	for (Object constanteEnumerado : classeEnumerado.getEnumConstants()) {
	    if (constanteEnumerado.toString().equals(enumeradoAsObject.toString())) {
		return (T) constanteEnumerado;
	    }
	}
	
	throw new RuntimeException("Nao encontrou enumerado para '"+enumeradoAsObject+"'");
    }

    public static List<Long> transformarKeysEmIds(List<Key> keys) {
	List<Long> listIds = new ArrayList<Long>(keys.size());
	for (Key key : keys) {
	    listIds.add(key.getId());
	}
	return listIds;
    }

}













