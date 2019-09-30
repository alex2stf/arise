package com.arise.jentil;

import com.arise.jentil.storage.entities.FileEntity;
import com.arise.jentil.storage.entities.FileTag;
import com.arise.jentil.storage.entities.FolderEntity;
import com.arise.jentil.storage.entities.FolderTag;
import com.arise.jentil.storage.entities.Tag;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.util.config.ConfigurationException;

public class TestDB {


    private static FolderEntity createTestFolder(){
        FolderEntity root = new FolderEntity();
        root.setStatus("ACTIVE");
        root.setCreationDate(new Date());




//        root.getProperties().add(new FolderTag());

        return root;
    }


    public static void main(String[] args) {

        Configuration configuration = new Configuration();





        Properties properties = new Properties();
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.DRIVER, "org.postgresql.Driver");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.URL, "jdbc:postgresql://localhost/jentil");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "alex");
        properties.put(Environment.AUTOCOMMIT, "true");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

        configuration.setProperties(properties);

        configuration.addAnnotatedClass(FileEntity.class);
        configuration.addAnnotatedClass(FolderEntity.class);
        configuration.addAnnotatedClass(Tag.class);
        configuration.addAnnotatedClass(FileTag.class);
        configuration.addAnnotatedClass(FolderTag.class);


        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        Tag tag = new Tag();
        tag.setValue("xxx");


        FolderTag fd = new FolderTag();


        FolderEntity root = createTestFolder();



        Transaction transaction = session.beginTransaction();

        session.save(tag);
        fd.setTag(tag);
        session.save(fd);


        session.save(root);
        transaction.commit();
        session.close();

        sessionFactory.close();


    }
}
