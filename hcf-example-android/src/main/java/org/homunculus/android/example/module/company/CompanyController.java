package org.homunculus.android.example.module.company;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Singleton
public class CompanyController {

    @Inject
    private EntityManager em;

    public void testLoad() {
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
    }
}
