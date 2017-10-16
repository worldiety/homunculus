package org.homunculusframework.jpa.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.lang.Classname;
import org.homunculusframework.lang.Panic;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ORMLiteQuery<T> implements Query {

    private final Class<T> type;
    private final Map<String, String> parameter;
    private final Dao<T, ?> dao;
    private final String query;

    public ORMLiteQuery(String query, Dao<T, ?> dao, Class<T> type) {
        this.parameter = new TreeMap<>();
        this.query = query;
        this.type = type;
        this.dao = dao;
    }

    @Override
    public List getResultList() {
        try {
            List res = new ArrayList();
            RawRowMapper<T> mapper = dao.getRawRowMapper();
            GenericRawResults<T> r = dao.queryRaw(query, mapper, parameter.values().toArray(new String[parameter.size()]));
            try {
                for (T t : r) {
                    res.add(t);
                }
            } finally {
                r.close();
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getSingleResult() {
        throw new Panic("not yet implemented");
    }

    @Override
    public int executeUpdate() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setMaxResults(int maxResult) {
        throw new Panic("not yet implemented");
    }

    @Override
    public int getMaxResults() {
        return getResultList().size();
    }

    @Override
    public Query setFirstResult(int startPosition) {
        throw new Panic("not yet implemented");
    }

    @Override
    public int getFirstResult() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setHint(String hintName, Object value) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Map<String, Object> getHints() {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> Query setParameter(Parameter<T> param, T value) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(String name, Object value) {
        parameter.put(name, value.toString());
        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(int position, Object value) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Parameter<?> getParameter(String name) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Parameter<?> getParameter(int position) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        throw new Panic("not yet implemented");
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Object getParameterValue(String name) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Object getParameterValue(int position) {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new Panic("not yet implemented");
    }

    @Override
    public Query setLockMode(LockModeType lockMode) {
        throw new Panic("not yet implemented");
    }

    @Override
    public LockModeType getLockMode() {
        throw new Panic("not yet implemented");
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new Panic("not yet implemented");
    }
}
