/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculusframework.jpa.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import org.homunculusframework.lang.Panic;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * By definition an EntityManager needs not to be thread safe, however in a container it looks like that and
 * to simplify things (and our container is simple), this one is thread safe.
 * <p>
 * Most stuff is not implemented. Implemented methods are:
 * <ul>
 * <li>{@link #getDao(Class)}</li>
 * <li>{@link #persist(Object)}</li>
 * <li>{@link #remove(Object)}</li>
 * <li>{@link #find(Class, Object)}</li>
 * <li>{@link #flush()}</li>
 * <li>{@link #refresh(Object)}</li>
 * <li>{@link #createNativeQuery(String, Class)}</li>
 * <li>{@link ORMLiteQuery#setParameter(String, Object)} (probably only string support)</li>
 * <li>{@link ORMLiteQuery#getMaxResults()} (slow)</li>
 * <li>{@link ORMLiteQuery#getResultList()} (the projection has to be exact, no extra or missing columns)</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ORMLiteEntityManager implements EntityManager {

    private final JdbcConnectionSource connectionSource;
    private final Map<Class, Dao> managers;
    private final Object lock = new Object();
    private final String jdbcUrl;
    //h2 profits from keeping the connection always open -> we should use a simple pool
    private final Connection keepOpenConnection;

    public ORMLiteEntityManager(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:h2:")) {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new Panic("h2 driver required but not in classpath", e);
            }
        }

        try {
            this.jdbcUrl = jdbcUrl;
            connectionSource = new JdbcConnectionSource(jdbcUrl);
            managers = new IdentityHashMap<>();
            keepOpenConnection = DriverManager.getConnection(jdbcUrl);

        } catch (SQLException e) {
            throw new Panic(e);
        }
    }

    /**
     * Returns the backing connection source.
     *
     * @return the source
     */
    public JdbcConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * See {@link DatabaseConnection#setSavePoint(String)}
     */
    public Savepoint setSavePoint(Class<?> type, String name) {
        try {
            String tableName = getDao(type);
            return getConnectionSource().getReadWriteConnection(tableName).setSavePoint(name);
        } catch (SQLException e) {
            throw new Panic(e);
        }
    }

    /**
     * See {@link DatabaseConnection#commit(Savepoint)}
     */
    public void commit(Class<?> type, Savepoint savepoint) {
        try {
            String tableName = getDao(type);
            getConnectionSource().getReadWriteConnection(tableName).commit(savepoint);
        } catch (SQLException e) {
            throw new Panic(e);
        }
    }


    /**
     * See {@link DatabaseConnection#rollback(Savepoint)}
     */
    public void rollback(Class<?> type, Savepoint savepoint) {
        try {
            String tableName = getDao(type);
            getConnectionSource().getReadWriteConnection(tableName).rollback(savepoint);
        } catch (SQLException e) {
            throw new Panic(e);
        }
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
