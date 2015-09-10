/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zols.datastore.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import static org.slf4j.LoggerFactory.getLogger;
import org.zols.datastore.DataStore;
import org.zols.datastore.jsonschema.JSONSchema;
import org.zols.datatore.exception.DataStoreException;

/**
 *
 * Elastic Search Implementation of DataStore
 *
 * @author Raji
 */
public class ElasticSearchDataStore extends DataStore {

    private static final org.slf4j.Logger LOGGER = getLogger(ElasticSearchDataStore.class);

    private final Node node;

    private final String indexName;

    public ElasticSearchDataStore() {
        this.indexName = "zols";
        Settings settings = ImmutableSettings.settingsBuilder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0).build();
        node = nodeBuilder().settings(settings).local(true).build().start();        
        createIndexIfNotExists();
    }

    public ElasticSearchDataStore(String indexName) {
        this.indexName = indexName;
        Settings settings = ImmutableSettings.settingsBuilder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0).build();
        node = nodeBuilder().settings(settings).local(true).build().start();        
        createIndexIfNotExists();
    }

    private void patchDelayInRefresh() {
//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ElasticSearchDataStore.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void createIndexIfNotExists() {
        IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest(indexName);
        if (!node.client().admin().indices().exists(indicesExistsRequest).actionGet().isExists()) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            node.client().admin().indices().create(createIndexRequest).actionGet();
            LOGGER.debug("New index [{}] created",indexName);
        }
    }

    @Override
    protected Map<String, Object> create(JSONSchema jsonSchema, Map<String, Object> validatedDataObject) {
        LOGGER.debug("Create Data for ",jsonSchema.id());
        Object idValue = validatedDataObject.get(jsonSchema.idField());
        IndexRequestBuilder indexRequestBuilder;
        if (idValue == null) {
            indexRequestBuilder = node.client().prepareIndex(indexName, jsonSchema.id()).setRefresh(true);
        } else {
            indexRequestBuilder = node.client().prepareIndex(indexName, jsonSchema.id(), idValue.toString()).setRefresh(true);
        }

        IndexResponse response = indexRequestBuilder
                .setSource(validatedDataObject)
                .setOperationThreaded(false)
                .execute()
                .actionGet();

        Map<String, Object> createdData = null;

        if (response.isCreated()) {
            createdData = read(jsonSchema, response.getId());
            //If autogenerated if then update idField
            if (idValue == null) {
                createdData.put(jsonSchema.idField(), response.getId());
                update(jsonSchema, createdData);
            }
        }

        patchDelayInRefresh();
        return createdData;

    }

    @Override
    protected Map<String, Object> read(JSONSchema jsonSchema, String idValue) {
        GetResponse getResponse = node.client()
                .prepareGet(indexName, jsonSchema.id(), idValue)
                .setOperationThreaded(false)
                .execute()
                .actionGet();
        patchDelayInRefresh();
        return getResponse.getSource();
    }

    @Override
    protected boolean delete(JSONSchema jsonSchema, String idValue) {
        DeleteResponse response = node.client()
                .prepareDelete(indexName, jsonSchema.id(), idValue).setRefresh(true)
                .setOperationThreaded(false)
                .execute()
                .actionGet();
        node.client().admin().indices().refresh(new RefreshRequest(indexName));
        patchDelayInRefresh();
        return response.isFound();
    }

    @Override
    protected boolean update(JSONSchema jsonSchema, Map<String, Object> validatedDataObject) {
        String idValue = validatedDataObject.get(jsonSchema.idField()).toString();
        IndexResponse response = node.client().prepareIndex(indexName, jsonSchema.id(), idValue).setRefresh(true)
                .setOperationThreaded(false)
                .setSource(validatedDataObject)
                .execute()
                .actionGet();
        node.client().admin().indices().refresh(new RefreshRequest(indexName));
        patchDelayInRefresh();
        return response.isCreated();
    }

    @Override
    protected List<Map<String, Object>> list(JSONSchema jsonSchema) {
        List<Map<String, Object>> list = null;
        SearchResponse response = node.client()
                .prepareSearch()
                .setIndices(indexName)
                .setTypes(jsonSchema.id())
                .execute().actionGet();
        SearchHits hits = response.getHits();
        if (hits != null) {
            SearchHit[] searchHits = hits.getHits();
            if (searchHits != null) {
                list = new ArrayList<>(searchHits.length);
                for (SearchHit searchHit : searchHits) {
                    list.add(searchHit.getSource());
                }
            }
        }
        return list;
    }

    @Override
    protected void drop() throws DataStoreException {
        node.client().admin().indices().delete(new DeleteIndexRequest(indexName));
    }

}
