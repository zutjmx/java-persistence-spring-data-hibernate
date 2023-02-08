/*
 * ========================================================================
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package com.manning.javapersistence.ch10;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ReadOnly {

    private static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("ch10");


    private FetchTestData storeTestData() {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Long[] itemIds = new Long[3];
        Long[] userIds = new Long[3];

        User johndoe = new User("johndoe");
        em.persist(johndoe);
        userIds[0] = johndoe.getId();

        User janeroe = new User("janeroe");
        em.persist(janeroe);
        userIds[1] = janeroe.getId();

        User robertdoe = new User("robertdoe");
        em.persist(robertdoe);
        userIds[2] = robertdoe.getId();

        Item item = new Item("Item One", LocalDate.now().plusDays(1), johndoe);
        em.persist(item);
        itemIds[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid(item, robertdoe, new BigDecimal(9 + i));
            item.addBid(bid);
            em.persist(bid);
        }

        item = new Item("Item Two", LocalDate.now().plusDays(1), johndoe);
        em.persist(item);
        itemIds[1] = item.getId();
        for (int i = 1; i <= 1; i++) {
            Bid bid = new Bid(item, janeroe, new BigDecimal(2 + i));
            item.addBid(bid);
            em.persist(bid);
        }

        item = new Item("Item Three", LocalDate.now().plusDays(2), janeroe);
        em.persist(item);
        itemIds[2] = item.getId();

        em.getTransaction().commit();
        em.close();

        FetchTestData testData = new FetchTestData();
        testData.items = new TestData(itemIds);
        testData.users = new TestData(userIds);
        return testData;
    }

    @Test
    public void immutableEntity() {
        EntityManager em = emf.createEntityManager();
        FetchTestData testData = storeTestData();
        em.getTransaction().begin();

        Long ITEM_ID = testData.items.getFirstId();

        Item item = em.find(Item.class, ITEM_ID);

        for (Bid bid : item.getBids()) {
            bid.setAmount(new BigDecimal("99.99")); // This has no effect
        }
        em.flush();
        em.clear();

        item = em.find(Item.class, ITEM_ID);
        for (Bid bid : item.getBids()) {
            assertNotEquals("99.99", bid.getAmount().toString());
        }

        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void selectiveReadOnly() throws Exception {
        EntityManager em = emf.createEntityManager();
        FetchTestData testData = storeTestData();
        em.getTransaction().begin();

        Long ITEM_ID = testData.items.getFirstId();

        {
            em.unwrap(Session.class).setDefaultReadOnly(true);

            Item item = em.find(Item.class, ITEM_ID);
            item.setName("New Name");

            em.flush(); // No UPDATE
        }
        {
            em.clear();
            Item item = em.find(Item.class, ITEM_ID);
            assertNotEquals("New Name", item.getName());
        }
        {
            Item item = em.find(Item.class, ITEM_ID);

            em.unwrap(Session.class).setReadOnly(item, true);

            item.setName("New Name");

            em.flush(); // No UPDATE
        }
        {
            em.clear();
            Item item = em.find(Item.class, ITEM_ID);
            assertNotEquals("New Name", item.getName());
        }
        {
            org.hibernate.query.Query<Item> query = em.unwrap(Session.class)
                    .createQuery("select i from Item i", Item.class);

            query.setReadOnly(true).list();

            List<Item> result = query.list();

            for (Item item : result)
                item.setName("New Name");

            em.flush(); // No UPDATE
        }
        {
            List<Item> items = em.createQuery("select i from Item i", Item.class)
                    .setHint(
                            org.hibernate.annotations.QueryHints.READ_ONLY,
                            Boolean.TRUE
                    ).getResultList();

            for (Item item : items)
                item.setName("New Name");
            em.flush(); // No UPDATE
        }
        {
            em.clear();
            Item item = em.find(Item.class, ITEM_ID);
            assertNotEquals("New Name", item.getName());
        }

        em.getTransaction().commit();
        em.close();
    }
}
