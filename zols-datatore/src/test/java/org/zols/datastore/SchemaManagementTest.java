/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zols.datastore;

import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.zols.datastore.jsonschema.util.JsonSchemaTestUtil.sampleJsonSchema;
import static org.zols.datastore.jsonschema.util.JsonSchemaTestUtil.sampleJsonSchemaText;
import org.zols.datastore.util.JsonUtil;
import static org.zols.datastore.util.TestUtil.testDataStore;
import org.zols.datatore.exception.DataStoreException;

/**
 *
 * This class will test Schema Management functionalities provided by Data Store
 */
public class SchemaManagementTest {

    private final DataStore dataStore;

    public SchemaManagementTest() {
        dataStore = testDataStore();
    }

    @Before
    public void beforeTest() throws DataStoreException {
        dataStore.createSchema(sampleJsonSchemaText("address"));
        dataStore.createSchema(sampleJsonSchemaText("person"));
    }

    @After
    public void afterTest() throws DataStoreException {
        dataStore.delete("address");
        dataStore.deleteSchema(sampleJsonSchemaText("address"));
        dataStore.delete("person");
        dataStore.deleteSchema(sampleJsonSchemaText("person"));
    }

    @Test
    public void testGetRawJsonSchema() throws DataStoreException {
        Map<String,Object> rawJsonSchema = dataStore.getRawJsonSchema(sampleJsonSchema("teacher"));
        System.out.println(JsonUtil.asString(rawJsonSchema));
        System.out.println("rawJsonSchema");
    }

}
