package org.homunculus.android.example;

import android.app.Application;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.module.cart.*;
import org.homunculus.android.example.module.company.Company;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.flavor.ee.EEFlavor;
import org.homunculusframework.factory.flavor.spring.SpringFlavor;
import org.homunculusframework.jpa.ormlite.ORMLiteEntityManager;
import org.homunculusframework.lang.Panic;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

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


        configureH2();

        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();
    }

    private String script(String assetName) {
        try {
            InputStream in = getResources().getAssets().open(assetName);
            try {
                InputStreamReader ir = new InputStreamReader(in);
                BufferedReader r = new BufferedReader(ir);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new Panic(e);
        }
    }

    private void clearH2(File base) {
        for (File file : base.getParentFile().listFiles()) {
            if (file.getName().startsWith(base.getName())) {
                file.delete();
            }
        }
    }

    public void configureH2() {
        try {
            File fname = new File(getFilesDir(), "mydb");
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:" + fname + ";FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;MODE=MySQL";
            clearH2(fname);
            LoggerFactory.getLogger(getClass()).info("open h2: {}", url);
            Connection conn = DriverManager.getConnection(url);

            RunScript.execute(conn, new StringReader(script("V1__init.sql")));
            LoggerFactory.getLogger(getClass()).info("db is up");

            ORMLiteEntityManager em = new ORMLiteEntityManager(url);
            Company cmp = em.find(Company.class, "de.worldiety");
            LoggerFactory.getLogger(getClass()).info("{}", cmp);
            em.remove(cmp);
            cmp = em.find(Company.class, "de.worldiety");
            LoggerFactory.getLogger(getClass()).info("{}", cmp);

            cmp = new Company();
            cmp.setId("abc");
            cmp.setTitleColor("#00000");
            cmp.setTitleName("hello world");
            em.persist(cmp);
            cmp.setId("abc2");
            em.persist(cmp);

            cmp = em.find(Company.class, "abc");
            LoggerFactory.getLogger(getClass()).info("{}", cmp);

            Query query = em.createNativeQuery("select id,title_name,title_color from company", Company.class);
            List<Company> companyList = (List<Company>) query.getResultList();
            for (Company c : companyList) {
                LoggerFactory.getLogger(getClass()).info("{}", c);
            }

        } catch (Exception e) {
            throw new Panic(e);
        }
    }
}
