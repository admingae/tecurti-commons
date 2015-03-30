package com.tecurti.model.persistencia.dao.gae;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO;

/**
 * 
 * Quando esta dentro de uma transacao a consulta (mesmo usando a conexao) traz sempre o mesmo resultado
 * pois � como se ele fizesse uma copia do estado no inicio da transacao. Em outras palavras, nao fica alterando
 * o valor conforme o andamento da transa��o.
 * 
 * Se fizer uma transacao do tipo read, mesmo se algu�m alterar o valor nao tem problema, e al�m disso nas transacoes
 * do tipo read pode dar commit q nao tem problema.
 * 
 * Mesmo se a entidade for sem ancestral, casa fizer uma transa��o nela tb funciona (lan�ando ConcurrentException)
 * 
 * A ConcurrentException � lan�ada qdo existe altera��o naquele grupo. Para identificar o grupo � visto a Key 
 * do MAIOR ancestral pai (vai subindo a cadeia). Ou seja lock da transacao � feito usando a Key do maior ancestral.
 * Detalhe q n�o � preciso q o Pai esteja no banco (entidade j� pode ter sido removida)
 * 
 * Uma busca por ancestral sempre traz o valor atual (dentro ou fora da transacao)
 * 
 * Uma busca get sempre funciona (pois no fundo � uma busca por ancestral).. ja uma Query q n�o usa ancestral pode nao 
 * funcionar precisando de um tempo maior.
 * 
 * Quando faz uma consulta dentro de uma transacao � possivel fazer consultas crossgroup... mas usando Query. Se fizer
 * uma consulta crossgroup usando key mesmo se usar outro datastore (DatastoreServiceFactory.getDatastoreService()) da erro
 * ele pede pra fazer crossgroup.
 * Agora se for fazer put ele nao deixa mesmo se usar DatastoreServiceFactory.getDatastoreService() ele nao deixa.
 * Para q seja possvel tem q ficar passando o txn (Transaction por parametro conforme for usando), ou colocar null
 * 
 * @author Felipe
 *
 */
public class TesteTransacao {
    
    private static Logger logger = Logger.getLogger(TesteTransacao.class.getName());
    
    public Key gravarUsuario(String nomeKey) {
	
	Entity entityUsuarioTeste = new Entity("UsuarioTeste", nomeKey);
	entityUsuarioTeste.setProperty("nome", nomeKey);
	entityUsuarioTeste.setProperty("idade", "27");
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Key keyUsuarioTeste = datastore.put(entityUsuarioTeste);
	
	return keyUsuarioTeste;
    }

    public void adicionarUmaImagem() throws Exception {
	
	/*Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = KeyFactory.createKey(keyFelipe, "ImagemTeste", 6252647749255168L);
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Entity entity = datastore.get(keyImagem);
	
	logger.info(entity);*/
	

	Key keyNull1 = KeyFactory.createKey("UsuarioTeste", "FelipeNull1");
//	DatastoreServiceFactory.getDatastoreService().put(new Entity(keyNull1));
	Key keyNull2 = KeyFactory.createKey("UsuarioTeste", "FelipeNull2");
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = KeyFactory.createKey(keyFelipe, "ImagemTeste", 1000L);
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction();
	try {
	    
	    Entity imagemFelipe2 = new Entity("DescricaoImg", "descImg1000", keyImagem);
	    imagemFelipe2.setProperty("nomeImagem", "imagem 222");
	    imagemFelipe2.setProperty("contador", 1);
	    datastore.put(imagemFelipe2);

	    Entity entity = datastore.get(imagemFelipe2.getKey());
	    
	    Query query = new Query(keyFelipe);
	    List<Entity> asList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
	    
	    /*Entity entity2 = DatastoreServiceFactory.getDatastoreService().get(keyNull1);
	    Entity entity3 = DatastoreServiceFactory.getDatastoreService().get(keyNull2);*/
	    
	    /*Entity entity2 = new Entity(keyNull1);
	    datastore.put(entity2);*/
	    
	    xx(datastore, txn);
	    
	    Query query2 = new Query("UsuarioTeste", keyNull1);
	    
	    /*DatastoreService xxxx = DatastoreServiceFactory.getDatastoreService();
	    Transaction currentTransaction1 = xxxx.getCurrentTransaction();
	    Transaction currentTransaction11 = datastore.getCurrentTransaction();
	    
	    Transaction txn2 = xxxx.beginTransaction();
	    Transaction currentTransaction2 = xxxx.getCurrentTransaction();
	    Transaction currentTransaction22 = datastore.getCurrentTransaction();
	    txn2.commit();
	    
	    Transaction currentTransaction3 = xxxx.getCurrentTransaction();
	    Transaction currentTransaction33 = datastore.getCurrentTransaction();*/
	    
	    /*DatastoreService xxxx = DatastoreServiceFactory.getDatastoreService();
	    Transaction currentTransaction1 = xxxx.getActiveTransactions().iterator().next();
	    Transaction currentTransaction11 = datastore.getActiveTransactions().iterator().next();
	    
	    Transaction txn2 = xxxx.beginTransaction();
	    Transaction currentTransaction2 = xxxx.getActiveTransactions().iterator().next();
	    Transaction currentTransaction22 = datastore.getActiveTransactions().iterator().next();
	    txn2.commit();
	    
	    Transaction currentTransaction3 = xxxx.getActiveTransactions().iterator().next();
	    Transaction currentTransaction33 = datastore.getActiveTransactions().iterator().next();*/
	    
	    Entity entity22ss = datastore.get(null, keyNull1);
	    
	    try {
		Entity entity22 = datastore.get(keyFelipe);
	    } catch (EntityNotFoundException e) {
		logger.info("Nao encontrou entidade");
	    }
	    
//	    Entity entity55 = datastore.get(keyNull1);
	    
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
	
	/*Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = null;
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(true));
	try {
	    
	    Entity imagemFelipe = new Entity("ImagemTeste", keyFelipe);
	    imagemFelipe.setProperty("nomeImagem", "imagem felipe grupo");
	    imagemFelipe.setProperty("contador", 1);
	    keyImagem = datastore.put(imagemFelipe);
	    
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
	
	logger.info(keyImagem);*/
    }

    private void xx(DatastoreService d, Transaction t) {

	/*DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Query query1 = new Query("UsuarioTeste", null);
	List<Entity> asList1 = datastore.prepare(null, query1).asList(FetchOptions.Builder.withDefaults());
	datastore.put(null, asList1);
	logger.info("xxx");*/


	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction();
	try {

	    Query query1 = new Query("UsuarioTeste", null);
	    List<Entity> asList1 = datastore.prepare(null, query1).asList(FetchOptions.Builder.withDefaults());
	    datastore.put(null, asList1);
	    logger.info("xxx");

	    txn.commit();

	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}

    }
    
    /*public void transacao1() throws Exception {

	final Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	
	UtilsGaeDAO.executarCodigoTransacional(new CodigoTransacional() {
	    public void execute(DatastoreService datastore, Transaction txn) throws Exception {
		
		Entity entity = datastore.get(keyFelipe);
		int idade = Integer.parseInt(entity.getProperty("idade").toString());
		idade++;
		entity.setProperty("idade", ""+idade);
		
		datastore.put(entity);
	    }
	});
    }*/
    
    public void transacao1() throws Exception {

	long currentTimeMillis = System.currentTimeMillis();
	
	Entity entityContador = new Entity("ContadorTeste");
	entityContador.setProperty("valor", currentTimeMillis);
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Key keyContador = datastore.put(entityContador);
	
	datastore = DatastoreServiceFactory.getDatastoreService();
	//Entity entity = datastore.get(keyContador);
	//logger.info("get " + entity.getProperty("valor").equals(currentTimeMillis));
	Thread.sleep(2000);
	Query query = new Query("ContadorTeste");
	query.setFilter(new FilterPredicate("valor", FilterOperator.EQUAL, currentTimeMillis));
	List<Entity> asList = DatastoreServiceFactory.getDatastoreService().prepare(query).asList(FetchOptions.Builder.withDefaults());
	if (asList.size() == 1) {
	    logger.info("achou na pesquisa");
	} else {
	    logger.info("ERRROOOO pesquisa");
	}
	logger.info("-------------");
    }
    
    // usuario
    /*public Object transacao2 transacao read() throws Exception {
	
	final Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	
	return UtilsGaeDAO.executarCodigoTransacional(true, new CodigoTransacional() {
	    public Object executeComRetorno(DatastoreService datastore, Transaction txn) throws Exception {
		
		Entity entity = datastore.get(keyFelipe);
		logger.info(entity.getProperty("idade"));
		
		entity = datastore.get(keyFelipe);
		logger.info(entity.getProperty("idade"));
		
		entity = datastore.get(keyFelipe);
		logger.info(entity.getProperty("idade"));
		
		return entity.getProperty("idade");
	    }
	});
    }*/
    
    public void transacao3() throws Exception {
	String sequence = "felipe";
	Long nextValue = UtilsGaeDAO.getNextValueForSequence(sequence);
	logger.info(sequence+ " " + nextValue);
    }
    
    public void transacao4() throws Exception {
	String sequence = "mari";
	Long nextValue = UtilsGaeDAO.getNextValueForSequence(sequence);
	logger.info(sequence+ " " + nextValue);
    }
    
    /*public void transacao3  KeyFactory.stringToKey(keyToString);  () throws Exception {
	
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = KeyFactory.createKey(keyFelipe, "ImagemTeste", 6252647749255168L);
	Key keyDesc = KeyFactory.createKey(keyImagem, "DescricaoImg", "descImg1");
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	Entity entity = datastore.get(keyDesc);
	logger.info(entity);
	
	String keyToString = KeyFactory.keyToString(keyDesc);
	logger.info(keyToString);
	
	Key stringToKey = KeyFactory.stringToKey(keyToString);
	entity = datastore.get(stringToKey);
	logger.info(entity);
    }*/
    
    public void transacao2() throws Exception {
	
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = KeyFactory.createKey(keyFelipe, "ImagemTeste", 6252647749255168L);
	Key keyDesc = KeyFactory.createKey(keyImagem, "DescricaoImg", "descImg1");
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(true));
	try {
	    
	    Entity entity = datastore.get(keyDesc);
	    entity.setProperty("descImagem", "mudei2 "+System.currentTimeMillis());
	    
	    datastore.put(entity);
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
    }
    
/*    public void transacao3() throws Exception {
	
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(true));
	try {
	    
	    Entity felipe = datastore.get(keyFelipe);
	    felipe.setProperty("nome", "Novo nome " + System.currentTimeMillis());
	    
	    datastore.put(felipe);
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
	
	logger.info();
    }
*/    
    /*public void transacao4() throws Exception {

	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyImagem = KeyFactory.createKey(keyFelipe, "ImagemTeste", 1000L);
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(true));
	try {
	    
	    Entity felipe = datastore.get(keyImagem);
	    felipe.setProperty("nome", "Novo nome para img 2 " + System.currentTimeMillis());
	    
	    datastore.put(felipe);
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
	
	logger.info();
    }*/
    
    public void adicionarImagensGrupo() {

	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	Key keyMari = KeyFactory.createKey("UsuarioTeste", "Mari");
	
	Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withDefaults().setXG(true));
	try {
	    
	    Entity imagemFelipe = new Entity("ImagemTeste",keyFelipe);
	    imagemFelipe.setProperty("nomeImagem", "imagem felipe grupo");
	    imagemFelipe.setProperty("contador", 2);
	    datastore.put(imagemFelipe);
	    
	    Entity imagemMari = new Entity("ImagemTeste",keyMari);
	    imagemMari.setProperty("nomeImagem", "imagem mari grupo");
	    imagemMari.setProperty("contador", 2);
	    datastore.put(imagemMari);
	    
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
    }
    
    public void adicionarImagensFelipe() {
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	Key keyFelipe = KeyFactory.createKey("UsuarioTeste", "Felipe");
	
	Transaction txn = datastore.beginTransaction();
	try {
	    
	    Entity imagemFelipe = new Entity("ImagemTeste",keyFelipe);
	    imagemFelipe.setProperty("nomeImagem", "imagem felipe sem grupo");
	    imagemFelipe.setProperty("contador", 2);
	    datastore.put(imagemFelipe);
	    
	    txn.commit();
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
    }
    
   /* public void deletarImagens(String keyAncestor) {

	Key ancestor = KeyFactory.createKey("UsuarioTeste", keyAncestor);
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Transaction txn = datastore.beginTransaction();
	try {
	    
	    Query queryImagens = new Query("ImagemTeste", ancestor);
	    queryImagens.addSort("contador", SortDirection.DESCENDING);
	    List<Entity> asList = datastore.prepare(queryImagens).asList(FetchOptions.Builder.withLimit(1));
	    

	    queryImagens = new Query("ImagemTeste", ancestor);
	    queryImagens.addSort("contador", SortDirection.DESCENDING);
	    List<Entity> asListTotal = DatastoreServiceFactory.getDatastoreService().prepare(queryImagens).asList(FetchOptions.Builder.withDefaults());
	    logger.info("Total correta: "+(asListTotal.size()-1));
	    
	    
	    datastore.delete(asList.get(0).getKey());
	    
	    txn.commit();
	    
	    queryImagens = new Query("ImagemTeste");
	    queryImagens.addSort("contador", SortDirection.DESCENDING);
	    asListTotal = DatastoreServiceFactory.getDatastoreService().prepare(queryImagens).asList(FetchOptions.Builder.withDefaults());
	    logger.info("Total registros sem ancestor1: "+asListTotal.size());
	    
	    queryImagens = new Query("ImagemTeste");
	    queryImagens.addSort("contador", SortDirection.DESCENDING);
	    asListTotal = DatastoreServiceFactory.getDatastoreService().prepare(queryImagens).asList(FetchOptions.Builder.withDefaults());
	    logger.info("Total registros sem ancestor2: "+asListTotal.size());
	    
	    
	    queryImagens = new Query("ImagemTeste", ancestor);
	    queryImagens.addSort("contador", SortDirection.DESCENDING);
	    asListTotal = datastore.prepare(queryImagens).asList(FetchOptions.Builder.withDefaults());
	    logger.info("Total registros com ancestor: "+asListTotal.size());
	    
	    
	} catch (ConcurrentModificationException e) {
	    throw e;
	} finally {
	    if (txn.isActive()) {
		txn.rollback();
	    }
	}
    }*/
    
}






















