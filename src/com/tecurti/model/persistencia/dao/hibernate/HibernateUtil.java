package com.tecurti.model.persistencia.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;


public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static void generateTables() {
	SchemaExport export = new SchemaExport(new Configuration().configure());
	export.create(true, false);
    }
    
    public static void generateTables(boolean script, boolean doUpdate)  {
	SchemaExport export = new SchemaExport(new Configuration().configure());
	export.create(script, doUpdate);
    }

    public static void updateTables() {
	SchemaUpdate update = new SchemaUpdate(new Configuration().configure());
	update.execute(true, false);
    }
    
    public static void updateTables(boolean script, boolean doUpdate)  {
	SchemaUpdate update = new SchemaUpdate(new Configuration().configure());
	update.execute(script, doUpdate);
    }

    public static Session getSession() {
	return getSessionFactory().openSession();
    }

    public static SessionFactory getSessionFactory() {

	if (sessionFactory == null) {
	    initSessionFactory();
	}

	return sessionFactory;
    }

    public static void initSessionFactory() {
	Configuration configuration = new Configuration().configure();
	ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
	sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

}












