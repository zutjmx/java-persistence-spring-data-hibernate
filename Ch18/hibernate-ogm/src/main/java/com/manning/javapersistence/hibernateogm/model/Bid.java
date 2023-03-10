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
package com.manning.javapersistence.hibernateogm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    @GenericGenerator(name = "ID_GENERATOR", strategy = "uuid2")
    @Getter
    private String id;

    @Getter
    @Setter
    private BigDecimal amount;

    @ManyToOne
    @Getter
    @Setter
    private User user;

    @ManyToOne
    @Getter
    @Setter
    private Item item;

    public Bid(BigDecimal amount) {
        this.amount = amount;
    }
}
