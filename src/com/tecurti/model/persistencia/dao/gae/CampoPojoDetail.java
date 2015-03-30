package com.tecurti.model.persistencia.dao.gae;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.tecurti.model.persistencia.dao.gae.ColunaGAE.Tipo;
import com.tecurti.model.persistencia.dao.gae.utils.UtilsGaeDAO;

public class CampoPojoDetail {
    
    private static Logger logger = Logger.getLogger(CampoPojoDetail.class.getName());
    
    Field field;
    
    public CampoPojoDetail(Field field) {
	this.field = field;
	this.field.setAccessible(true);
    }

    public String getName() {
	return field.getName();
    }
    
    public void setValorJavaNoObjetoJava(Object objeto, Object valorJava) throws Exception {
	field.set(objeto, valorJava);
    }
    
    public void setValorNoObjetoFromEntity(Object objeto, Entity entity) throws Exception {
	
	Object valorDaEntityFormatoJava = getValorFromEntityConvertendoNoPadraoJava(entity);
	setValorJavaNoObjetoJava(objeto, valorDaEntityFormatoJava);
	
	/*entity.setProperty(getName(), valorjava);
	
	Object value = entity.getProperty(getName());
	setValorGaeNoObjetoJava(objeto, value);*/
    }
    
    public void setValorNaEntityFromObjeto(Entity entity, Object object) throws Exception {
	
	Object valorjava = field.get(object);
	
	// ------------------
	if (valorjava == null) {
	    entity.setProperty(getName(), valorjava);
	    return;
	}
	
	// ------------------
	Object valorGae;
	if (valorjava.getClass().equals(byte[].class)) {
	    
	    valorGae = new Blob((byte[]) valorjava);
	} else if (List.class.isAssignableFrom(valorjava.getClass())) {
	    
	    valorGae = UtilsGaeDAO.transformarObjectEmBlob(valorjava);
	} else if (Calendar.class.isAssignableFrom(valorjava.getClass())) {
	    valorGae = ((Calendar) valorjava).getTime();
	} else if (valorjava.getClass().isEnum()) {
	    valorGae = valorjava.toString();
	} else if (valorjava.getClass().equals(String.class)) {
	    
	    ColunaGAE colunaGae = field.getAnnotation(ColunaGAE.class);
	    if (colunaGae != null) {
		if (colunaGae.tipoGae() == Tipo.TEXTO_MUITOS_CARACTERES) {
		    valorGae = new Text(valorjava.toString());
		} else {
		    valorGae = valorjava.toString();
		}
	    } else {
		valorGae = valorjava.toString();
	    }
	    
	} else {
	    valorGae = valorjava;
	}
	
	entity.setProperty(getName(), valorGae);
    }
    
    public Object getValorFromEntityConvertendoNoPadraoJava(Entity entity) {
	
	try {
	
	    Object valorFormatoGae = entity.getProperty(getName());
	    if (valorFormatoGae == null) {
		return null;
	    } else {
		
		Object valorFormatoJava = null;
		if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
		    valorFormatoJava = Integer.parseInt(valorFormatoGae.toString());
		    
		} else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
		    valorFormatoJava = Long.parseLong(valorFormatoGae.toString());
		    
		} else if (valorFormatoGae.getClass().equals(Blob.class)) {
		    
		    if (field.getType().equals(byte[].class)) {
			valorFormatoJava = ((Blob)valorFormatoGae).getBytes();
		    } else if (field.getType().equals(List.class)) {
			valorFormatoJava = UtilsGaeDAO.transformarBlobEmObject((Blob)valorFormatoGae);
		    } 
		}  else if (valorFormatoGae.getClass().equals(Text.class)) {
		    
		    valorFormatoJava = ((Text)valorFormatoGae).getValue();
		} else if (field.getType().isEnum()) {
		    
		    for (Object enumConstant : field.getType().getEnumConstants()) {
			if (enumConstant.toString().equals(valorFormatoGae.toString())) {
			    valorFormatoJava = enumConstant;
			    break;
			}
		    }
		} else if (Calendar.class.isAssignableFrom(field.getType())) {
		    
		    Calendar gregorianCalendar = new GregorianCalendar();
		    gregorianCalendar.setTime((Date) valorFormatoGae);
		    valorFormatoJava = gregorianCalendar;
		} else {
		    
		    valorFormatoJava = valorFormatoGae;
		}
		
		return valorFormatoJava;
	    }
	    
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
    
    /*public void setValorGaeNoObjetoJava(Object objeto, Object valor) {

	try {
	    
	    if (valor == null) {
		field.set(objeto, valor);
	    } else {
		
		Object valorReal = null;
		if (field.getType().equals(Integer.class)) {
		    
		    valorReal = valor.getClass().equals(Integer.class) ? valor : Integer.parseInt(valor.toString());
		} else if (valor.getClass().equals(Blob.class)) {
		    
		    if (field.getType().equals(byte[].class)) {
			valorReal = ((Blob)valor).getBytes();
		    } else if (field.getType().equals(List.class)) {
			valorReal = UtilsGaeDAO.transformarBlobEmObject((Blob)valor);
		    } 
		}  else if (valor.getClass().equals(Text.class)) {
		    
		    valorReal = ((Text)valor).getValue();
		} else if (field.getType().isEnum()) {
		    
		    for (Object enumConstant : field.getType().getEnumConstants()) {
			if (enumConstant.toString().equals(valor.toString())) {
			    valorReal = enumConstant;
			    break;
			}
		    }
		} else if (Calendar.class.isAssignableFrom(field.getType())) {
		    
		    Calendar gregorianCalendar = new GregorianCalendar();
		    gregorianCalendar.setTime((Date) valor);
		    valorReal = gregorianCalendar;
		} else {
		    
		    valorReal = valor;
		}
		
		field.set(objeto, valorReal);
	    }
	    
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "", e);
	    // e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }*/
    
    public Class<?> getFieldClassConvertendoParaTipoGae() {
	return UtilsGaeDAO.converterClassJavaParaClassGae(field.getType());
    }

    public Class<?> getFieldClass() {
	return field.getType();
    }

    @Override
    public String toString() {
	return "CampoPojoDetail [getName()=" + getName() + ", getFieldClass()=" + getFieldClass() + ", getFieldClassConvertendoParaTipoGae()= "+getFieldClassConvertendoParaTipoGae()+"]";
    }

    public boolean isId() {
	return getName().equals("id");
    }
    
}









