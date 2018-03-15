package org.homunculus.android.example;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.homunculus.android.component.HomunculusApplication;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.jpa.ormlite.ORMLiteEntityManager;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.persistence.EntityManager;

public class MyApplication extends HomunculusApplication {


    @Override
    protected void onConfigure(Configuration cfg) {
        //configure HCF for Android
        long start = System.currentTimeMillis();

        super.onConfigure(cfg);

        //try performance on real device
//        Register.register(cfg);
        LoggerFactory.getLogger(getClass()).info("configuration time {}ms", System.currentTimeMillis() - start);

        //setup the entity manager
        setupDB(cfg.getRootScope());

         /*
         * Performance metrics of 100 controllers with 5 injection fields and 5 exported methods (17. October 2017):
         *   PixelXL:   333ms | 340ms | 345ms  (Android 8)
         *   S3:        650ms | 693ms | 1400ms (Android 4.4.4, Custom Rom)
         *   S4 Mini:   917ms | 815ms | 1023ms (Android 4.4.2)
         *   S5 Neo:    776ms | 760ms | 883ms  (Android 6.0.1)
         *   XperiaXZ   263ms | 253ms | 566ms  (Android 7.1.1)
         *   Dell V8    626ms | 721ms | 589ms  (Android 4.4.2)
         */
    }


    /**
     * load db, migrate db and onProvide the JPA entity manager API into the given scope
     */
    private void setupDB(Scope scope) {
        File fname = new File(getFilesDir(), "mydb");
        String url = "jdbc:h2:" + fname + ";FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;MODE=MySQL";


        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, "", "");
        flyway.migrate();


        EntityManager em = new ORMLiteEntityManager(url);
        scope.put("entityManager", em);
    }

}
