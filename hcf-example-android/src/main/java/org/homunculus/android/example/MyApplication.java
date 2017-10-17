package org.homunculus.android.example;

import android.app.Application;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.h2.tools.RunScript;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.module.cart.*;
import org.homunculus.android.example.module.company.CompanyController;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.flavor.ee.EEFlavor;
import org.homunculusframework.jpa.ormlite.ORMLiteEntityManager;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //configure HCF for Android
        Configuration cfg = Android.getConfiguration(this);
        new EEFlavor().apply(cfg);

        //add each module (== controllers + views), order is unimportant
        cfg.add(CartController.class);
        cfg.add(CartControllerConnection.class);
        cfg.add(CartRepository.class);
        cfg.add(CartView.class);
        cfg.add(CartUIS.class);
        cfg.add(CompanyController.class);

        //setup the entity manager
        setupDB(cfg.getRootScope());

        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();
    }

    private void setupDB(Scope scope) {
        File fname = new File(getFilesDir(), "mydb");
        String url = "jdbc:h2:" + fname + ";FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;MODE=MySQL";


        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, "", "");
        flyway.migrate();


        EntityManager em = new ORMLiteEntityManager(url);
        scope.putNamedValue("entityManager", em);
    }
}
