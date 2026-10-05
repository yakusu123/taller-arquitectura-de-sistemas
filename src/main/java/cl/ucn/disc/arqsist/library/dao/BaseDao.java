/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

public abstract class BaseDao<T> {
    protected final Dao<T, Integer> dao;

    protected BaseDao(ConnectionSource connectionSource, Class<T> clazz) {
        try {
            this.dao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> findAll() {
        try {
            return dao.queryForAll();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all entities", e);
        }
    }

    public T findById(Integer id) {
        try {
            return dao.queryForId(id);
        } catch (Exception e) {
            throw new RuntimeException("Error finding entity by ID", e);
        }
    }

    public void create(T entity) {
        try {
            dao.create(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error creating entity", e);
        }
    }

    public void update(T entity) {
        try {
            dao.update(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error updating entity", e);
        }
    }

    public void delete(T entity) {
        try {
            dao.delete(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting entity", e);
        }
    }
}
