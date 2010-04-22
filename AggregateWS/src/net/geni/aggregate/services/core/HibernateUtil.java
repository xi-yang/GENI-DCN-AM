/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;

/**
 *
 * @author Xi Yang
 */

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static void initSessionFactory() {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml)
            // config file.
            Properties props = new Properties();
            props.put("hibernate.connection.username", AggregateState.getDbUser());
            props.put("hibernate.connection.password", AggregateState.getDbPwd());
            props.put("hibernate.connection.autoreconnect", "true");
            Configuration cfg = new Configuration();
            cfg.setProperties(props);
            sessionFactory = cfg.configure().buildSessionFactory();
        } catch (Throwable e) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + e);
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

