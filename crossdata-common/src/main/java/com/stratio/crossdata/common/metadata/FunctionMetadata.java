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

package com.stratio.crossdata.common.metadata;

import com.stratio.crossdata.common.data.FunctionName;

public class FunctionMetadata implements IMetadata {

    final private FunctionName functionName;
    final private String signature;
    final private String returningType;

    public FunctionMetadata(FunctionName functionName, String signature, String returningType) {
        this.functionName = functionName;
        this.signature = signature;
        this.returningType = returningType;
    }

    public FunctionName getFunctionName() {
        return functionName;
    }

    public String getSignature() {
        return signature;
    }

    public String getReturningType() {
        return returningType;
    }
}
