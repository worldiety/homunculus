package org.homunculusframework.jpa.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.homunculusframework.lang.Panic;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * By definition an EntityManager needs not to be thread safe
 */
public class ORMLiteEntityManager implements EntityManager {

    private final ConnectionSource connectionSource;
    private final Map<Class, Dao> managers;
    private final Object lock = new Object();
    private final String jdbcUrl;
    //h2 profits from keeping the connection always open -> we should use a simple pool
    private final Connection keepOpenConnection;

    public ORMLiteEntityManager(String jdbcUrl) throws SQLException {
        this.jdbcUrl = jdbcUrl;
        connectionSource = new JdbcConnectionSource(jdbcUrl);
        managers = new IdentityHashMap<>();
        keepOpenConnection = DriverManager.getConnection(jdbcUrl);
    }

    public <D extends Dao<T, ?>, T> D getDao(Class<T> type) {
        synchronized (lock) {
            Dao dao = managers.get(type);
            if (dao == null) {
                try {
                    dao = DaoManager.createDao(connectionSource, type);
                    managers.put(type, dao);
                } catch (SQLException e) {
                    throw new Panic(e);
                }
            }
            return (D) dao;
        }
    }

    @Override
    public void persist(Object entity) {
        Dao dao = getDao(entity.getClass());
        try {
            dao.create(entity);
        } catch (SQLException e) {
            throw new EntityExistsException(e);
        }
    }

    @Override
    public <T> T merge(T entity) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void remove(Object entity) {
        Dao dao = getDao(entity.getClass());
        try {
            dao.delete(entity);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        Dao dao = getDao(entityClass);
        try {
            return (T) dao.queryForId(primaryKey);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void flush() {
        synchronized (lock) {
            for (Dao dao : managers.values()) {
                dao.notifyChanges();
            }
        }
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new Panic("not yet implemented");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void refresh(Object entity) {
        Dao dao = getDao(entity.getClass());
        try {
            dao.refresh(entity);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void clear() {
        throw new Panic("not yet implemented");
    }

    @Override
    public void detach(Object entity) {
        throw new Panic("not yet implemented");
    }

    @Override
    public boolean contains(Object entity) {
        throw new Panic("not yet implemented");
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Map<String, Object> getProperties() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query createQuery(String qlString) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query createNamedQuery(String name) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        Dao dao = getDao((Class<?>) resultClass);
        return new ORMLiteQuery(sqlString, dao, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        throw new Panic("not yet implemented");
    }

    @Override
    public void joinTransaction() {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Object getDelegate() {
        throw new Panic("not yet implemented");
    }

    @Override
    public void close() {
        throw new Panic("not yet implemented");
    }

    @Override
    public boolean isOpen() {
        throw new Panic("not yet implemented");
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new Panic("not yet implemented");
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        throw new Panic("not yet implemented");
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Metamodel getMetamodel() {
        throw new Panic("not yet implemented");
    }
}
