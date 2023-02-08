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
package com.manning.javapersistence.ch06;

import com.manning.javapersistence.ch06.model.*;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MappingValuesJPATest {

    @Test
    public void storeLoadEntities() {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("ch06.mapping_value_types");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            City city = new City();
            city.setName("Boston");
            city.setZipcode(new GermanZipcode("12345"));
            city.setCountry("USA");

            User user = new User();
            user.setUsername("username");
            user.setHomeAddress(new Address("Flowers Street", city));

            Item item = new Item();
            item.setName("Some Item");
            item.setMetricWeight(2);
            item.setInitialPrice(new MonetaryAmount(new BigDecimal("1.00"), Currency.getInstance("USD")));
            item.setBuyNowPrice(new MonetaryAmount(BigDecimal.valueOf(1.1), Currency.getInstance("USD")));
            item.setDescription("descriptiondescription");

            em.persist(user);
            em.persist(item);

            em.getTransaction().commit();
            em.refresh(user);
            em.refresh(item);

            em.getTransaction().begin();

            List<User> users =
                    em.createQuery("select u from User u", User.class)
                            .getResultList();

            List<Item> items =
                    em.createQuery("select i from Item i where i.metricWeight = :w", Item.class)
                            .setParameter("w", 2.0)
                            .getResultList();
            em.getTransaction().commit();

            assertAll(
                    () -> assertEquals(1, users.size()),
                    () -> assertEquals("username", users.get(0).getUsername()),
                    () -> assertEquals("Flowers Street", users.get(0).getHomeAddress().getStreet()),
                    () -> assertEquals("Boston", users.get(0).getHomeAddress().getCity().getName()),
                    () -> assertEquals("12345", users.get(0).getHomeAddress().getCity().getZipcode().getValue()),
                    () -> assertEquals("USA", users.get(0).getHomeAddress().getCity().getCountry()),
                    () -> assertEquals(1, items.size()),
                    () -> assertEquals("AUCTION: Some Item", items.get(0).getName()),
                    () -> assertEquals("2.20 USD", items.get(0).getBuyNowPrice().toString()),
                    () -> assertEquals("descriptiondescription", items.get(0).getDescription()),
                    () -> assertEquals(AuctionType.HIGHEST_BID, items.get(0).getAuctionType()),
                    () -> assertEquals("descriptiond...", items.get(0).getShortDescription()),
                    () -> assertEquals(2.0, items.get(0).getMetricWeight()),
                    () -> assertEquals(LocalDate.now(), items.get(0).getCreatedOn()),
                    () -> assertTrue(ChronoUnit.SECONDS.between(LocalDateTime.now(), items.get(0).getLastModified()) < 1),
                    () -> assertEquals("2.00 EUR", items.get(0).getInitialPrice().toString())
            );
        } finally {
            em.close();
            emf.close();
        }
    }

}
