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

package com.stratio.crossdata.core.statements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stratio.crossdata.common.exceptions.ParsingException;
import com.stratio.crossdata.common.statements.structures.Relation;
import com.stratio.crossdata.common.utils.StringUtils;
import com.stratio.crossdata.core.structures.Option;
import com.stratio.crossdata.core.utils.ParserUtils;
import com.stratio.crossdata.common.data.CatalogName;
import com.stratio.crossdata.common.data.TableName;
import com.stratio.crossdata.common.statements.structures.Selector;
import com.stratio.crossdata.core.validator.requirements.ValidationTypes;
import com.stratio.crossdata.core.validator.requirements.ValidationRequirements;

/**
 * Class that models an {@code UPDATE} statement from the CROSSDATA language.
 */
public class UpdateTableStatement extends StorageStatement implements ITableStatement {

    /**
     * The name of the table.
     */
    private TableName tableName;

    /**
     * The list of options.
     */
    private List<Option> options;

    /**
     * The list of assignations.
     */
    private List<Relation> assignations;

    /**
     * The list of relations.
     */
    private List<Relation> whereClauses;

    /**
     * Map of conditions.
     */
    private Map<Selector, Selector> conditions;

    /**
     * Class constructor.
     *
     * @param tableName    The name of the table.
     * @param options      The list of options.
     * @param assignations The list of assignations.
     * @param whereClauses The list of relations.
     * @param conditions   The map of conditions.
     */
    public UpdateTableStatement(TableName tableName, List<Option> options,
            List<Relation> assignations, List<Relation> whereClauses,
            Map<Selector, Selector> conditions) throws ParsingException {
        this.command = false;

        if(tableName.getName().isEmpty()){
            throw new ParsingException("Table name cannot be empty");
        }
        this.tableName = tableName;

        if(options == null){
            this.options = new ArrayList<>();
        } else {
            this.options = options;
        }

        if(assignations == null){
            this.assignations = new ArrayList<>();
        } else {
            this.assignations = assignations;
        }

        if(whereClauses == null){
            this.whereClauses = new ArrayList<>();
        } else {
            this.whereClauses = whereClauses;
        }

        if(conditions == null){
            this.conditions = new HashMap<>();
        } else {
            this.conditions = conditions;
        }

    }

    /**
     * Class constructor.
     *
     * @param tableName    The name of the table.
     * @param assignations The list of assignations.
     * @param whereClauses The list of relations.
     * @param conditions   The map of conditions.
     */
    public UpdateTableStatement(TableName tableName, List<Relation> assignations,
            List<Relation> whereClauses, Map<Selector, Selector> conditions) throws ParsingException {
        this(tableName, null, assignations, whereClauses, conditions);
    }

    /**
     * Class constructor.
     *
     * @param tableName    The name of the table.
     * @param options      The list of options.
     * @param assignations The list of assignations.
     * @param whereClauses The list of relations.
     */
    public UpdateTableStatement(TableName tableName, List<Option> options,
            List<Relation> assignations, List<Relation> whereClauses) throws ParsingException {
        this(tableName, options, assignations, whereClauses, null);
    }

    /**
     * Class constructor.
     *
     * @param tableName    The name of the table.
     * @param assignations The list of assignations.
     * @param whereClauses The list of relations.
     */
    public UpdateTableStatement(TableName tableName, List<Relation> assignations,
            List<Relation> whereClauses) throws ParsingException {
        this(tableName, null, assignations, whereClauses, null);
    }

    public List<Relation> getAssignations() {
        return assignations;
    }

    public List<Relation> getWhereClauses() {
        return whereClauses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(tableName.getQualifiedName());
        if ((options != null) && (!options.isEmpty())) {
            sb.append(" ").append("USING ");
            sb.append(StringUtils.stringList(options, " AND "));
        }
        sb.append(" ").append("SET ");
        sb.append(StringUtils.stringList(assignations, ", "));
        if ((whereClauses != null) && (!whereClauses.isEmpty())) {
            sb.append(" ").append("WHERE ");
            sb.append(StringUtils.stringList(whereClauses, " AND "));
        }
        if ((conditions != null) && (!conditions.isEmpty())) {
            sb.append(" ").append("IF ");
            sb.append(ParserUtils.stringMap(conditions, " = ", " AND "));
        }
        return sb.toString();
    }

    @Override
    public ValidationRequirements getValidationRequirements() {
        return new ValidationRequirements().add(ValidationTypes.MUST_EXIST_CATALOG).add(ValidationTypes.MUST_EXIST_TABLE)
                .add(ValidationTypes.MUST_EXIST_COLUMN);
    }

    public TableName getTableName() {
        return tableName;
    }

    public void setTableName(TableName tableName) {
        this.tableName = tableName;
    }

    @Override
    public CatalogName getEffectiveCatalog() {
        CatalogName effective;
        if (tableName != null) {
            effective = tableName.getCatalogName();
        } else {
            effective = catalog;
        }
        if (sessionCatalog != null) {
            effective = sessionCatalog;
        }
        return effective;
    }

}
