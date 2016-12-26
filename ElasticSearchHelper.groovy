package org.wangn6.utilities.es

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.elasticsearch.client.*
import org.apache.http.HttpHost
import org.apache.http.HttpEntity
import org.apache.http.nio.entity.NStringEntity
import org.apache.http.entity.ContentType

/**
 * Created by nwang on 29/11/2016.
 * This is to interact with the remote elasticsearch server using the ES Rest Client
 */
class ElasticSearchHelper {
    static String host
    static int port
    static String administrator
    static String password
    static RestClient _restClient = null


    ElasticSearchHelper(config) {
        host = config.host
        port = config.port
        administrator = config?.administrator
        password = config?.password
    }

    public static synchronized esRestClient() {
        if (_restClient == null) {
            _restClient = RestClient.builder(new HttpHost(host, port, "http")).build()
        }
        return _restClient
    }

    //save the test result to ElasticSearch
    void saveResult2ES(String indexName, String docType, String jsonString) {
        try{
            HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON)
            String docId = UUID.randomUUID().toString()
            Response response = esRestClient().performRequest('PUT', "/${indexName}/${docType}/${docId}", Collections.<String, String> emptyMap(), entity)
        }
        catch (Exception ex)
        {
            println(ex)
        }

    }

    //Create the index in ElasticSearch, TODO, need to detail the schema of the index doc type
    void createIndex(indexName, docType) {
        try{
            String path = "/${indexName}"
            JsonBuilder builder = new JsonBuilder()
            builder {
                mappings {
                    "${docType}" {
                        "properties" {
                            "name" {
                                type "string"
                                index "not_analyzed"
                            }
                            "result" {
                                type "string"
                                index "not_analyzed"
                            }
                        }
                    }
                }
            }
            String body = builder.toString()
            HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON)
            Map<String, String> params = Collections.<String, String> emptyMap()
            Response response = esRestClient().performRequest('PUT', path, params, entity)
        }
        catch (Exception ex)
        {
            println(ex)
        }

    }

    //Delete the index from ElasticSearch
    void deleteIndex(indexName) {
        String path = "/${indexName}"
        Response response = esRestClient().performRequest("DELETE", path)
    }

    //Check whether the index exists or not on ElasticSearch
    boolean isIndexExisting(indexName) {
        String path = "/${indexName}"
        try{
            Response response = esRestClient().performRequest("HEAD", path)
            if (response.getStatusLine().statusCode == 200) {
                return true
            } else {
                return false
            }
        }
        catch (ResponseException ex)
        {
            println(ex.message)
            return false
        }

    }

    static synchronized void closeClient()
    {
        if(_restClient != null)
        {
            esRestClient().close()
            _restClient = null
        }
    }

    //The sample usage of the helper class
    public static void main(String[] args) {
        String indexName = "test_index01"
        String docType = "sample"
        ElasticSearchHelper helper = new ElasticSearchHelper(host: "nwang.local", port: 9200)
        if (helper.isIndexExisting(indexName)) {
            helper.deleteIndex(indexName)
        }
        helper.createIndex(indexName, docType)
        JsonBuilder builder = new JsonBuilder()
        builder {
            name "Neil"
            age 20
            description "Hello World"
        }
        helper.saveResult2ES(indexName, docType, builder.toString())
        helper.closeClient()
    }
}