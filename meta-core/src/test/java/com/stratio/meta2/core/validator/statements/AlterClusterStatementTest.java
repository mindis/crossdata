package com.stratio.meta2.core.validator.statements;

import com.stratio.meta.common.exceptions.IgnoreQueryException;
import com.stratio.meta.common.exceptions.ValidationException;
import com.stratio.meta2.common.data.CatalogName;
import com.stratio.meta2.core.query.BaseQuery;
import com.stratio.meta2.core.query.MetadataParsedQuery;
import com.stratio.meta2.core.query.ParsedQuery;
import com.stratio.meta2.core.statements.AlterClusterStatement;
import com.stratio.meta2.core.validator.Validator;
import org.testng.Assert;
import org.testng.annotations.Test;


public class AlterClusterStatementTest {
    @Test
    public void alterCluster() {
        String query = "ALTER CLUSTER IF EXISTS Cassandra WITH OPTIONS {'comment':'my coments'}";


        AlterClusterStatement alterClusterStatement=new AlterClusterStatement("Cassandra",true, "{'comment':'my coments'}");
        Validator validator=new Validator();

        BaseQuery baseQuery=new BaseQuery("alterClusterId",query, new CatalogName("system"));

        ParsedQuery parsedQuery=new MetadataParsedQuery(baseQuery,alterClusterStatement);
        try {
            validator.validate(parsedQuery);
            Assert.assertTrue(true);
        } catch (ValidationException e) {
            Assert.fail(e.getMessage());
        } catch (IgnoreQueryException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void alterIfExistsClusterNotExists() {
        String query = "ALTER CLUSTER IF EXISTS unknown WITH OPTIONS {'comment':'my coments'}";


        AlterClusterStatement alterClusterStatement=new AlterClusterStatement("unknown",true, "{'comment':'my coments'}");
        Validator validator=new Validator();

        BaseQuery baseQuery=new BaseQuery("alterClusterId",query, new CatalogName("system"));

        ParsedQuery parsedQuery=new MetadataParsedQuery(baseQuery,alterClusterStatement);
        try {
            validator.validate(parsedQuery);
            Assert.assertTrue(true);
        } catch (ValidationException e) {
            Assert.fail(e.getMessage());
        } catch (IgnoreQueryException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void alterClusterEmptyOptions() {
        String query = "ALTER CLUSTER demo WITH OPTIONS {}";


        AlterClusterStatement alterClusterStatement=new AlterClusterStatement("demo",false, "{}");
        Validator validator=new Validator();

        BaseQuery baseQuery=new BaseQuery("alterClusterId",query, new CatalogName("system"));

        ParsedQuery parsedQuery=new MetadataParsedQuery(baseQuery,alterClusterStatement);
        try {
            validator.validate(parsedQuery);
            Assert.fail("Cluster options must exists");
        } catch (ValidationException e) {
            Assert.assertTrue(true);
        } catch (IgnoreQueryException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void alterNotExistCluster() {
        String query = "ALTER CLUSTER unknown";


        AlterClusterStatement alterClusterStatement=new AlterClusterStatement("unknown",false, "{}");
        Validator validator=new Validator();

        BaseQuery baseQuery=new BaseQuery("alterClusterId",query, new CatalogName("system"));

        ParsedQuery parsedQuery=new MetadataParsedQuery(baseQuery,alterClusterStatement);
        try {
            validator.validate(parsedQuery);
            Assert.fail("Cluster must exists");
        } catch (ValidationException e) {
            Assert.assertTrue(true);
        } catch (IgnoreQueryException e) {
            Assert.assertTrue(true);
        }
    }

}
