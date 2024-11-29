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
import com.quatro.model.security.SecProvider;

/**
 * @author JZhang
 */
public interface SecProviderDao {

    // Constants (unchanged)
    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String PROVIDER_TYPE = "providerType";
    public static final String SPECIALTY = "specialty";
    public static final String TEAM = "team";
    public static final String SEX = "sex";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";
    public static final String WORK_PHONE = "workPhone";
    public static final String OHIP_NO = "ohipNo";
    public static final String RMA_NO = "rmaNo";
    public static final String BILLING_NO = "billingNo";
    public static final String HSO_NO = "hsoNo";
    public static final String STATUS = "status";
    public static final String COMMENTS = "comments";
    public static final String PROVIDER_ACTIVITY = "providerActivity";

    // CRUD operations
    void save(SecProvider transientInstance);
    void saveOrUpdate(SecProvider transientInstance);
    void delete(SecProvider persistentInstance);
    SecProvider findById(String id);
    SecProvider findById(String id, String status);
    List<SecProvider> findByExample(SecProvider instance);
    List<SecProvider> findByProperty(String propertyName, Object value);
    List<SecProvider> findAll();
    SecProvider merge(SecProvider detachedInstance);

    // Specific finder methods
    List<SecProvider> findByLastName(Object lastName);
    List<SecProvider> findByFirstName(Object firstName);
    List<SecProvider> findByProviderType(Object providerType);
    List<SecProvider> findBySpecialty(Object specialty);
    List<SecProvider> findByTeam(Object team);
    List<SecProvider> findBySex(Object sex);
    List<SecProvider> findByAddress(Object address);
    List<SecProvider> findByPhone(Object phone);
    List<SecProvider> findByWorkPhone(Object workPhone);
    List<SecProvider> findByOhipNo(Object ohipNo);
    List<SecProvider> findByRmaNo(Object rmaNo);
    List<SecProvider> findByBillingNo(Object billingNo);
    List<SecProvider> findByHsoNo(Object hsoNo);
    List<SecProvider> findByStatus(Object status);
    List<SecProvider> findByComments(Object comments);
    List<SecProvider> findByProviderActivity(Object providerActivity);

    // Attachment methods (consider if these are still necessary)
    void attachDirty(SecProvider instance);
    void attachClean(SecProvider instance);
}
