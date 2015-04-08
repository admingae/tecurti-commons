package com.tecurti.model.persistencia.dao.hibernate;
import org.hibernate.cfg.Configuration;

public interface HibernateConfigurationCreator {

    public Configuration getConfiguration();

}
