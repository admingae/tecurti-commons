package com.tecurti.model.persistencia.dao.gae;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO;

public class QueryGae {
    
    Key parentKey;
    
    List<String> listProjections = new ArrayList<String>();
    List<String> listProjectionsExceptions = new ArrayList<String>();
    
    List<ParametrosDoFiltroConsultaGae> listFiltros = new ArrayList<ParametrosDoFiltroConsultaGae>();
    List<SortConfigurationGae> listSortConfigurationGae = new ArrayList<SortConfigurationGae>();
    
    Integer offset;
    Integer limiteValoresRetornados;
    boolean keysOnly;
    
    PojoDetails pojoDetails;
    public QueryGae(Class<?> entityClass, Key parentKey) {
	pojoDetails = new PojoDetails(entityClass);
	this.parentKey = parentKey;
    }
    
    public void addProjectionException(String... projectionsException) {
	for (String projectionException : projectionsException) {
	    listProjectionsExceptions.add(projectionException);
	}
    }
    
    public void addProjection(String... projections) {
	for (String projection : projections) {
	    listProjections.add(projection);
	}
    }

    public void addFilterCasoSejaNotNull(String nomeCampo, Object valor) {
	if (valor != null) {
	    addFilter(nomeCampo, valor);
	}
    }
    
    public void addFilterCasoSejaNotNull(String nomeCampo, FilterOperator filterOperator, Object valor) {
	if (valor != null) {
	    addFilter(nomeCampo, filterOperator, valor);
	}
    }

    public void addFilter(String nomeCampo, FilterOperator filterOperator, Object valor) {
	listFiltros.add(new ParametrosDoFiltroConsultaGae(nomeCampo, filterOperator, valor));
    }
    
    public void addFilter(String nomeCampo, Object valor) {
	addFilter(nomeCampo, FilterOperator.EQUAL, valor);
    }

    public QueryGae addSortAsc(String... campos) {
	for (String campo : campos) {
	    listSortConfigurationGae.add(new SortConfigurationGae(campo, SortDirection.ASCENDING));
	}
	return this;
    }
    
    public QueryGae addSortDesc(String... campos) {
	
	for (String campo : campos) {
	    listSortConfigurationGae.add(new SortConfigurationGae(campo, SortDirection.DESCENDING));
	}
	
	return this;
    }

    public List<String> calcProjectionFieldsParaMetodoAdd(List<CampoPojoDetail> todosCampos) {

	if (listProjections.isEmpty() && listProjectionsExceptions.isEmpty()) {
	    return new ArrayList<String>();
	}
	
	// ----------------
	if (listProjectionsExceptions.size() > 0) {
	    
	    List<String> listCalculadaProjections = new ArrayList<String>();
	    for (CampoPojoDetail campoPojoDetail : todosCampos) {
		if (campoPojoDetail.getName().equals("id")) {
		    continue;
		}
		listCalculadaProjections.add(campoPojoDetail.getName());
	    }
	    
	    // ----------------
	    for (int i = listCalculadaProjections.size()-1; i >= 0; i--) {
		String nomeCampo = listCalculadaProjections.get(i);
		
		if (listProjectionsExceptions.contains(nomeCampo) || isFiltroDeIgualdade(nomeCampo)) {
		    listCalculadaProjections.remove(i);
		}
	    }
	    
	    return listCalculadaProjections;
	}
	
	// if (listProjections.size() > 0) {
	List<String> listCalculadaProjections = new ArrayList<String>();
	for (String campo : listProjections) {
	    if (isFiltroDeIgualdade(campo) == false) {
		listCalculadaProjections.add(campo);
	    }
	}

	return listCalculadaProjections;
    }

    public Filter getFilters() {

	// ----------------
	if (listFiltros.isEmpty()) {
	    return null;
	}

	// ----------------
	List<Filter> filtros = new ArrayList<Filter>();

	for (ParametrosDoFiltroConsultaGae param : listFiltros) {
	    filtros.add(new FilterPredicate(param.getName(), param.getFilterOperator(), param.getValueConvertidoParaFiltroGae()));
	}

	if (filtros.size() == 1) {
	    return filtros.get(0);
	} else {
	    return CompositeFilterOperator.and(filtros);
	}
    }

    public List<SortConfigurationGae> getSortConfiguration() {
	return listSortConfigurationGae;
    }

    public FetchOptions getFetchOptions() {
	
	FetchOptions fetchOptions = limiteValoresRetornados == null ? FetchOptions.Builder.withDefaults() : FetchOptions.Builder.withLimit(limiteValoresRetornados);
	
	if (offset != null) {
	    fetchOptions.offset(offset);
	}
	
	return fetchOptions;
    }

    public boolean isProjectionField(String campo) {
	
	if (campo.equals("id")) {
	    return false;
	}
	
	if (listProjections.isEmpty() && listProjectionsExceptions.isEmpty()) {
	    return true;
	}
	
	if (listProjectionsExceptions.size() > 0) {
	    return listProjectionsExceptions.contains(campo) == false;
	}
	
	return listProjections.contains(campo);
    }

    public boolean isFiltroDeIgualdade(String name) {
	for (ParametrosDoFiltroConsultaGae parametros : listFiltros) {
	    if (parametros.getName().equals(name) && parametros.getFilterOperator() == FilterOperator.EQUAL) {
		return true;
	    }
	}
	
	return false;
    }

    public Object getValorOriginalDoCampoQueEhFiltroDeIgualdade(String name) {
	for (ParametrosDoFiltroConsultaGae parametros : listFiltros) {
	    if (parametros.getFilterOperator() == FilterOperator.EQUAL && parametros.getName().equals(name)) {
		return parametros.getValue();
	    }
	}
	
	throw new RuntimeException("Nao encontrou valor para campos de igualdade '"+name+"'");
    }

    public Integer getLimiteValoresRetornados() {
        return limiteValoresRetornados;
    }

    public void setLimiteValoresRetornados(Integer limiteValoresRetornados) {
        this.limiteValoresRetornados = limiteValoresRetornados;
    }

    public boolean isKeysOnly() {
        return keysOnly;
    }

    public void setKeysOnly() {
        this.keysOnly = true;
    }

    public List<String> getListProjections() {
        return listProjections;
    }

    public boolean existeCamposProjectionsCadastrados() {
	return listProjections.size() > 0 || listProjectionsExceptions.size() > 0;
    }

    public Key getParentKey() {
        return parentKey;
    }

    public List<Key> findKeys(DatastoreService datastore, Transaction txn) {
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	
	setKeysOnly();
	List<Entity> entityList = datastore.prepare(txn, newQuery()).asList(getFetchOptions());
	
	List<Key> listKeys = UtilsGaeDAO.toListKeys(entityList);
	
	return listKeys;
    }
    
    public List<Entity> findEntityList(DatastoreService datastore, Transaction txn) {
	datastore = datastore != null ? datastore : DatastoreServiceFactory.getDatastoreService();
	
	Query query = createQuery();
	
	List<Entity> entityList = datastore.prepare(txn, query).asList(getFetchOptions());
	
	return entityList;
    }

    public Query createQuery() {
	Query q = newQuery();
	
	List<String> projectionFieldsCalculados = calcProjectionFieldsParaMetodoAdd(pojoDetails.getTodosCampos());
	for (String projectionField : projectionFieldsCalculados) {
	    q.addProjection(new PropertyProjection(projectionField, pojoDetails.getFieldClass(projectionField)));
	}
	
	// ----------------
	Filter filter = getFilters();
	if (filter != null) {
	    q.setFilter(filter);
	}
	
	List<SortConfigurationGae> listOfSortConfig = getSortConfiguration();
	for (SortConfigurationGae sortConfig : listOfSortConfig) {
	    q.addSort(sortConfig.campo, sortConfig.direction);
	}
	
	if (isKeysOnly()) {
	    q.setKeysOnly();
	}
	return q;
    }

    public int count() {
	return count(null, null);
    }
    
    public int count(DatastoreService datastore, Transaction txn) {
	setKeysOnly();
	return findEntityList(datastore, txn).size();
    }
    
    public boolean exists() {
	return exists(null, null);
    }
    
    public boolean exists(DatastoreService datastore, Transaction txn) {
	setLimiteValoresRetornados(1);
	setKeysOnly();
	return findEntityList(datastore, txn).size() > 0;
    }

    public List<?> findDistinctListOfLiteralReturnValue() {
	return findDistinctListOfLiteralReturnValue(null, null);
    }
    
    public List<?> findDistinctListOfLiteralReturnValue(DatastoreService datastore, Transaction txn) {
	return findListOfLiteralReturnValue(true, datastore, txn);
    }
    
    public List<?> findListOfLiteralReturnValue() {
	return findListOfLiteralReturnValue(null, null);
    }
    public List<?> findListOfLiteralReturnValue(DatastoreService datastore, Transaction txn) {
	return findListOfLiteralReturnValue(false, datastore, txn);
    }
    
    public List<?> findListOfLiteralReturnValue(boolean isDistinct, DatastoreService datastore, Transaction txn) { 
	
	// ----------------
	List<Entity> listEntity = findEntityList(datastore, txn);
	CampoPojoDetail campoProjection = pojoDetails.findCampo(getListProjections().get(0));
	
	// ----------------
	List<Object> listReturn = new ArrayList<>();
	for (Entity entity : listEntity) {
	    Object valorJava = campoProjection.getValorFromEntityConvertendoNoPadraoJava(entity);
	    
	    if (isDistinct) {
		boolean valorAindaNaoFoiCadastrado = listReturn.contains(valorJava) == false;
		if (valorAindaNaoFoiCadastrado) {
		    listReturn.add(valorJava);
		}
	    } else {
		listReturn.add(valorJava);
	    }
	}
	
	return listReturn;
    }

    public Object findLiteralReturnValue() throws Exception {
	return findLiteralReturnValue(null, null);
    }
    public Object findLiteralReturnValue(DatastoreService datastore, Transaction txn) {
	setLimiteValoresRetornados(1);
	
	List listDeUmResultado = findListOfLiteralReturnValue(datastore, txn);
	return listDeUmResultado.isEmpty() ? null : listDeUmResultado.get(0);
    }
    private Query newQuery() {
	
	if (getParentKey() == null) {
	    return new Query(pojoDetails.getKind());
	} else {
	    return new Query(pojoDetails.getKind(), getParentKey());
	}
    }

    public List<Key> findListIdBy() throws Exception {
	return findListIdBy(null, null);
    }
    public List<Key> findListIdBy(DatastoreService datastore, Transaction txn) {
	
	setKeysOnly();
	
	List<Entity> listEntity = findEntityList(datastore, txn);
	
	return UtilsGaeDAO.toListKeys(listEntity);
    }

    public Key findKey(DatastoreService datastore, Transaction txn) {
	
	setLimiteValoresRetornados(1);
	
	List<Key> list = findListIdBy(datastore, txn);
	return list.isEmpty() ? null : list.get(0);
    }

    public PojoDetails getPojoDetails() {
        return pojoDetails;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

}















