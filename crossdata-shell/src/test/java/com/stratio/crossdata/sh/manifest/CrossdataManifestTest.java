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

package com.stratio.crossdata.sh.manifest;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.stratio.crossdata.common.manifest.CrossdataManifest;
import com.stratio.crossdata.common.exceptions.ManifestException;
import com.stratio.crossdata.sh.utils.ConsoleUtils;
import com.stratio.crossdata.common.manifest.ManifestHelper;

public class CrossdataManifestTest {

    @Test
    public void testDataStoreManifest() {

        CrossdataManifest manifest = null;
        try {
            manifest = ConsoleUtils.parseFromXmlToManifest(CrossdataManifest.TYPE_DATASTORE,
                    getClass().getResourceAsStream("/com/stratio/crossdata/connector/DataStoreDefinition.xml"));
        } catch (ManifestException e) {
            fail("CrossdataManifest validation failed", e);
        }

        String parsedManifest = ManifestHelper.manifestToString(manifest);

        StringBuilder sb = new StringBuilder("DATASTORE");
        sb.append(System.lineSeparator());

        // NAME
        sb.append("Name: ").append("Cassandra").append(System.lineSeparator());

        // VERSION
        sb.append("Version: ").append("2.0.0").append(System.lineSeparator());

        // REQUIRED PARAMETERS
        sb.append("Required properties: ").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Host").append(System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Cassandra Host").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Port").append(System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Cassandra Port").append(System.lineSeparator());

        // OPTIONAL PROPERTIES
        sb.append("Optional properties: ").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Limit").append(System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Default limit").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Level").append(System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Warning level").append(System.lineSeparator());

        // BEHAVIORS
        sb.append("Behaviors: ").append(System.lineSeparator());
        sb.append("\t").append("Behavior: ").append("UPSERT_ON_INSERT").append(System.lineSeparator());
        sb.append("\t").append("Behavior: ").append("FAKE_BEHAVIOR").append(System.lineSeparator());

        // ERROR MESSAGE
        StringBuilder sbError = new StringBuilder(System.lineSeparator());
        sbError.append("Expecting:  ").append(sb.toString()).append(System.lineSeparator());
        sbError.append("Instead of: ").append(parsedManifest);

        assertTrue(parsedManifest.equalsIgnoreCase(sb.toString()), sbError.toString());
    }

    @Test
    public void testConnectorManifest() {

        CrossdataManifest manifest = null;
        try {
            manifest = ConsoleUtils.parseFromXmlToManifest(CrossdataManifest.TYPE_CONNECTOR,
                    getClass().getResourceAsStream("/com/stratio/crossdata/connector/ConnectorDefinition.xml"));
        } catch (ManifestException e) {
            fail("CrossdataManifest validation failed", e);
        }

        String parsedManifest = ManifestHelper.manifestToString(manifest);

        StringBuilder sb = new StringBuilder("CONNECTOR");
        sb.append(System.lineSeparator());

        // CONNECTOR NAME
        sb.append("ConnectorName: ").append("cassandra_native_connector").append(System.lineSeparator());

        // DATA STORES NAME
        sb.append("DataStores: ").append(System.lineSeparator());
        sb.append("\t").append("DataStoreName: ").append("cassandra").append(System.lineSeparator());

        // VERSION
        sb.append("Version: ").append("0.1.1").append(System.lineSeparator());

        // REQUIRED PROPERTIES
        sb.append("Required properties: ").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Host").append(
                System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Cassandra Host").append(
                System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Port").append(
                System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Cassandra Port").append(
                System.lineSeparator());

        // OPTIONAL PROPERTIES
        sb.append("Optional properties: ").append(System.lineSeparator());
        sb.append("\t").append("Property: ").append(System.lineSeparator());
        sb.append("\t").append("\t").append("PropertyName: ").append("Limit").append(
                System.lineSeparator());
        sb.append("\t").append("\t").append("Description: ").append("Default limit").append(
                System.lineSeparator());

        // SUPPORTED OPERATIONS
        sb.append("Supported operations: ").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("CREATE_CATALOG").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("DROP_CATALOG").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("CREATE_TABLE").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("DROP_TABLE").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("INSERT").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("DELETE").append(System.lineSeparator());
        sb.append("\t").append("Operation: ").append("PROJECT").append(System.lineSeparator());

        // ERROR MESSAGE
        StringBuilder sbError = new StringBuilder(System.lineSeparator());
        sbError.append("Expecting:  ").append(sb.toString()).append(System.lineSeparator());
        sbError.append("Instead of: ").append(parsedManifest);

        assertTrue(parsedManifest.equalsIgnoreCase(sb.toString()), sbError.toString());
    }

}
