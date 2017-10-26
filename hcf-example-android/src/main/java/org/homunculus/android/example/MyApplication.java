package org.homunculus.android.example;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;
import org.homunculus.android.compat.CompatApplication;
import org.homunculus.android.compat.UnbreakableCrashHandler;
import org.homunculus.android.example.module.benchmark.Register;
import org.homunculus.android.example.module.cart.CartController;
import org.homunculus.android.example.module.cart.CartControllerConnection;
import org.homunculus.android.example.module.cart.CartUIS;
import org.homunculus.android.example.module.cart.CartView;
import org.homunculus.android.example.module.company.CompanyController;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.jpa.ormlite.ORMLiteEntityManager;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;

public class MyApplication extends CompatApplication {

    private Scope mAppScope;


    @Override
    public void onCreate() {
        super.onCreate();

        //configure HCF for Android
        long start = System.currentTimeMillis();
        Configuration cfg = createConfiguration();

        //add each module (== controllers + views), order is unimportant
        cfg.add(CartController.class);
        cfg.add(CartControllerConnection.class);
        cfg.add(CartView.class);
        cfg.add(CartUIS.class);
        cfg.add(CompanyController.class);

        //try performance on real device
        Register.register(cfg);
        LoggerFactory.getLogger(getClass()).info("configuration time {}ms", System.currentTimeMillis() - start);

        //setup the entity manager
        setupDB(cfg.getRootScope());

        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();

        /*
         * Performance metrics of 100 controllers with 5 injection fields and 5 exported methods (17. October 2017):
         *   PixelXL:   333ms | 340ms | 345ms  (Android 8)
         *   S3:        650ms | 693ms | 1400ms (Android 4.4.4, Custom Rom)
         *   S4 Mini:   917ms | 815ms | 1023ms (Android 4.4.2)
         *   S5 Neo:    776ms | 760ms | 883ms  (Android 6.0.1)
         *   XperiaXZ   263ms | 253ms | 566ms  (Android 7.1.1)
         *   Dell V8    626ms | 721ms | 589ms  (Android 4.4.2)
         */

        new UnbreakableCrashHandler().install(this);
    }


    /**
     * load db, migrate db and provide the JPA entity manager API into the given scope
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
