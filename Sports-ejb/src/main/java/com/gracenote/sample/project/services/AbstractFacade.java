/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Gbenga
 */
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    public List<T> findWithNamedQuery(String namedQueryName, Map<String, Object> parameters) {
        return findWithNamedQuery(namedQueryName, parameters, 0);
    }

    public List<T> findWithNamedQuery(String namedQueryName, Map<String, Object> params, int offset, int limit) {
        Query query = getEntityManager().createNamedQuery(namedQueryName);
        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }
        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public long countWithNamedQuery(String namedCountQuery, Map<String, Object> params) {
        Query query = getEntityManager().createNamedQuery(namedCountQuery);
        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }
        return ((Long) query.getSingleResult());
    }

    public List<T> findWithNamedQuery(String namedQueryName, int offset) {
        return getEntityManager().createNamedQuery(namedQueryName).setFirstResult(offset).getResultList();
    }

    public List<T> findWithNamedQuery(String namedQuery, int offset, int limit) {
        return getEntityManager().createNamedQuery(namedQuery)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public List<T> findWithNamedQuery(String namedQueryName, Map<String, Object> parameters, int resultLimit) {
        Query query = getEntityManager().createNamedQuery(namedQueryName);
        if (resultLimit > 0) {
            query.setMaxResults(resultLimit);
        }
        Set<Map.Entry<String, Object>> params = parameters.entrySet();

        for (Map.Entry<String, Object> entry : params) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }
    
}
