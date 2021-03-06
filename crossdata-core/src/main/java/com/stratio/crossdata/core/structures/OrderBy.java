/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.crossdata.core.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratio.crossdata.common.statements.structures.Selector;

/**
 * OrderBy Class.
 */
public class OrderBy implements Serializable {
    private List<Selector> selectorList = new ArrayList<>();
    private OrderDirection direction = OrderDirection.ASC;

    /**
     * Constructor Class.
     */
    public OrderBy() {
    }

    /**
     * Constructor Class.
     * @param direction The order direction
     * @param selectorList A list of Selectors
     */
    public OrderBy(OrderDirection direction,
            List<Selector> selectorList) {
        this.direction = direction;
        this.selectorList = selectorList;
    }

    /**
     * Constructor Class.
     * @param selectorList A list of Selectors
     */
    public OrderBy(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    /**
     * Get a list of Selectors of the Group By.
     * @return List of Selectors of the Group By.
     */
    public List<Selector> getSelectorList() {
        return selectorList;
    }

    /**
     * Set the selector List of the Group By.
     * @param selectorList The selector list
     */
    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    /**
     * Get the direction of the Group By.
     * @return Direction of the Group By.
     */
    public OrderDirection getDirection() {
        return direction;
    }

    /**
     * Set the direction of the GroupBy.
     * @param direction
     */
    public void setDirection(OrderDirection direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Selector> iter = selectorList.iterator();

        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        if (direction == OrderDirection.DESC) {
            sb.append(" DESC");
        }

        return sb.toString();
    }
}
