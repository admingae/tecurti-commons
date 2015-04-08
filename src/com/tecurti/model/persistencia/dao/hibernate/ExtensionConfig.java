package com.tecurti.model.persistencia.dao.hibernate;

import org.hibernate.cfg.Configuration;

import com.tecurti.model.utils.ModelUtils;

public class ExtensionConfig {

    public static Configuration createHibernateConfiguration() {

	try {
	    String HibernateConfigurationCreator = ModelUtils.getPropertieFromResourceAsStream("/hibernate-dao-generico.properties", "HibernateConfigurationCreator");
	    HibernateConfigurationCreator creator = (HibernateConfigurationCreator) Class.forName(HibernateConfigurationCreator).newInstance();
	    return creator.getConfiguration();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }
}