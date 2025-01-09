//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package com.quatro.dao.security;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.oscarehr.util.MiscUtils;
import org.springframework.stereotype.Repository;

import com.quatro.model.security.SecProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * @author JZhang
 */

@Repository
public class SecProviderDaoImpl implements SecProviderDao {
    private static final Logger logger = MiscUtils.getLogger();
    
    @PersistenceContext
    private EntityManager entityManager;
    // property constants

    @Override
    public void save(SecProvider transientInstance) {
        logger.debug("saving Provider instance");
        try {
            entityManager.persist(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            logger.error("save failed", re);
            throw re;
        }
    }

    @Override
    public void saveOrUpdate(SecProvider transientInstance) {
        logger.debug("saving or updating Provider instance");
        try {
            entityManager.merge(transientInstance);
            logger.debug("save or update successful");
        } catch (RuntimeException re) {
            logger.error("save or update failed", re);
            throw re;
        }
    }

    @Override
    public void delete(SecProvider persistentInstance) {
        logger.debug("deleting Provider instance");
        try {
            entityManager.remove(entityManager.contains(persistentInstance) ? persistentInstance : entityManager.merge(persistentInstance));
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    @Override
    public SecProvider findById(java.lang.String id) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            return entityManager.find(SecProvider.class, id);
        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    @Override
    public SecProvider findById(java.lang.String id, String status) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            String sql = "from SecProvider where id=?0 and status=?1";
            List lst = this.getHibernateTemplate().find(sql, new Object[]{id, status});
            if (lst.size() == 0)
                return null;
            else
                return (SecProvider) lst.get(0);

        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    @Override
    public List<SecProvider> findByExample(SecProvider instance) {
        logger.debug("finding Provider instance by example");
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<SecProvider> cq = cb.createQuery(SecProvider.class);
            Root<SecProvider> root = cq.from(SecProvider.class);
            
            // Add criteria based on non-null properties of the example instance
            // This is a simplified version and may need to be expanded based on your needs
            if (instance.getLastName() != null) {
                cq.where(cb.equal(root.get("lastName"), instance.getLastName()));
            }
            // Add more criteria as needed
            
            TypedQuery<SecProvider> query = entityManager.createQuery(cq);
            List<SecProvider> results = query.getResultList();
            logger.debug("find by example successful, result size: " + results.size());
            return results;
        } catch (RuntimeException re) {
            logger.error("find by example failed", re);
            throw re;
        }
    }

    @Override
    public List<SecProvider> findByProperty(String propertyName, Object value) {
        logger.debug("finding Provider instance with property: " + propertyName + ", value: " + value);
        try {
            String queryString = "SELECT model FROM SecProvider model WHERE model." + propertyName + " = :value";
            TypedQuery<SecProvider> query = entityManager.createQuery(queryString, SecProvider.class);
            query.setParameter("value", value);
            return query.getResultList();
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
    }

    @Override
    public List findByLastName(Object lastName) {
        return findByProperty(LAST_NAME, lastName);
    }

    @Override
    public List findByFirstName(Object firstName) {
        return findByProperty(FIRST_NAME, firstName);
    }

    @Override
    public List findByProviderType(Object providerType) {
        return findByProperty(PROVIDER_TYPE, providerType);
    }

    @Override
    public List findBySpecialty(Object specialty) {
        return findByProperty(SPECIALTY, specialty);
    }

    @Override
    public List findByTeam(Object team) {
        return findByProperty(TEAM, team);
    }

    @Override
    public List findBySex(Object sex) {
        return findByProperty(SEX, sex);
    }

    @Override
    public List findByAddress(Object address) {
        return findByProperty(ADDRESS, address);
    }

    @Override
    public List findByPhone(Object phone) {
        return findByProperty(PHONE, phone);
    }

    @Override
    public List findByWorkPhone(Object workPhone) {
        return findByProperty(WORK_PHONE, workPhone);
    }

    @Override
    public List findByOhipNo(Object ohipNo) {
        return findByProperty(OHIP_NO, ohipNo);
    }

    @Override
    public List findByRmaNo(Object rmaNo) {
        return findByProperty(RMA_NO, rmaNo);
    }

    @Override
    public List findByBillingNo(Object billingNo) {
        return findByProperty(BILLING_NO, billingNo);
    }

    @Override
    public List findByHsoNo(Object hsoNo) {
        return findByProperty(HSO_NO, hsoNo);
    }

    @Override
    public List findByStatus(Object status) {
        return findByProperty(STATUS, status);
    }

    @Override
    public List findByComments(Object comments) {
        return findByProperty(COMMENTS, comments);
    }

    @Override
    public List findByProviderActivity(Object providerActivity) {
        return findByProperty(PROVIDER_ACTIVITY, providerActivity);
    }

    @Override
    public List<SecProvider> findAll() {
        logger.debug("finding all Provider instances");
        try {
            String queryString = "SELECT p FROM SecProvider p";
            TypedQuery<SecProvider> query = entityManager.createQuery(queryString, SecProvider.class);
            return query.getResultList();
        } catch (RuntimeException re) {
            logger.error("find all failed", re);
            throw re;
        }
    }

    @Override
    public SecProvider merge(SecProvider detachedInstance) {
        logger.debug("merging Provider instance");
        try {
            SecProvider result = entityManager.merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    @Override
    public void attachDirty(SecProvider instance) {
        logger.debug("attaching dirty Provider instance");
        try {
            entityManager.merge(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    @Override
    public void attachClean(SecProvider instance) {
        // This method is not directly applicable in JPA
        // You might want to consider removing it or implementing a different approach
        logger.warn("attachClean is not directly supported in JPA");
    }
}
