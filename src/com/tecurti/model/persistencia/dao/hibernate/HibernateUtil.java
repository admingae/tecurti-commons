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

    public static void executeGenerateTables() {
	SchemaExport export = new SchemaExport(ExtensionConfig.createHibernateConfiguration());
	export.create(true, true);
    }
    
    public static void showLogGenerateTables(boolean script, boolean doUpdate)  {
	SchemaExport export = new SchemaExport(ExtensionConfig.createHibernateConfiguration());
	export.create(script, doUpdate);
    }

    public static void executeUpdateTables() {
	SchemaUpdate update = new SchemaUpdate(ExtensionConfig.createHibernateConfiguration());
	update.execute(true, true);
    }
    
    public static void showLogUpdateTables(boolean script, boolean doUpdate)  {
	SchemaUpdate update = new SchemaUpdate(ExtensionConfig.createHibernateConfiguration());
	update.execute(script, doUpdate);
    }

    public static Session getSession() {
	return getSessionFactory().openSession();
    }

    public static SessionFactory getSessionFactory() {

	/*if (sessionFactory == null) {
	    initSessionFactory();
	}*/
	initSessionFactory();

	return sessionFactory;
    }

    public static void initSessionFactory() {
	Configuration configuration = ExtensionConfig.createHibernateConfiguration();
	ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
	sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

}












