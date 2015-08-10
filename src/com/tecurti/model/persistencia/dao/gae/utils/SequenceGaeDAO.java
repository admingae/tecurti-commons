package com.tecurti.model.persistencia.dao.gae.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.tecurti.model.persistencia.dao.gae.CodigoTransacional;
import com.tecurti.model.persistencia.dao.gae.DAOGenericoGAE;
import com.tecurti.model.persistencia.dao.gae.entidades.SequenceGae;

public class SequenceGaeDAO extends DAOGenericoGAE<SequenceGae, SequenceGae.Attr> {

    public static Long nextVal(final String nome) {
	try {
	    final SequenceGaeDAO sequenceGaeDAO = new SequenceGaeDAO();
	    final Key keySequence = SequenceGae.createKey(nome);
	    return UtilsGaeDAO.executarCodigoTransacional(new CodigoTransacional<Long>() {
		public Long executeComRetorno(DatastoreService datastore, Transaction txn) throws Exception {
		    SequenceGae sequence = sequenceGaeDAO.findById(keySequence, datastore, txn);
		    if (sequence == null) {
			sequence = new SequenceGae();
			sequence.setNameId(nome);
		    }
		    sequence.increase();
		    sequenceGaeDAO.update(sequence, datastore, txn);
		    return sequence.getCount();
		}
	    });
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    
    public static Long curVal(final String nome) {
	try {
	    final Key keySequence = SequenceGae.createKey(nome);
	    SequenceGae sequence = new SequenceGaeDAO().findById(keySequence);
	    return sequence == null ? null : sequence.getCount();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    
}
