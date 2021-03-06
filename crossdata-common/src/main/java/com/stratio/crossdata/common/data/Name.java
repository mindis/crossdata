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

package com.stratio.crossdata.common.data;

import java.io.Serializable;

public abstract class Name implements Serializable {

    private static final long serialVersionUID = -3032254998929033117L;
    static final String UNKNOWN_NAME = "<UNKNOWN_NAME>";

    /**
     *
     * @return boolean .
     */
    public abstract boolean isCompletedName();

    /**
     * returns qualified name.
     *
     * @return String.
     */
    public abstract String getQualifiedName();

    /**
     * returns NameType.
     *
     * @return NameType.
     */
    public abstract NameType getType();

    /**
     *
     * @return String representation of the object.
     */
    @Override
    public String toString() {
        return this.getQualifiedName();
    }

    /**
     *
     * @return object's hash code.
     */
    @Override
    public int hashCode() {
        String code = getType() + getQualifiedName();
        return code.hashCode();
    }

    /**
     * Method equals
     * @param o Object to be compared
     * @return whether Object o is equal to this object
     */
    @Override
    public boolean equals(Object o) {
        return (this == o) || ((o instanceof Name) && (this.hashCode() == o.hashCode()));
    }
}
