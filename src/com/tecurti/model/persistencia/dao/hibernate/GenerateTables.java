package com.tecurti.model.persistencia.dao.hibernate;


public class GenerateTables {

    public static void main(String[] args) {
	HibernateUtil.generateTables(true, true);
    }
}
