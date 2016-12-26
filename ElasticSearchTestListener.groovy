package org.wangn6.es_testng_listener

import groovy.json.JsonOutput
import org.testng.ITestContext
import org.testng.ITestListener
import org.testng.ITestResult
import java.text.DateFormat
import java.text.SimpleDateFormat

import org.wangn6.utilities.es.ElasticSearchHelper

/**
 * Created by nwang on 29/11/2016.
 * This is a placeholder for the test listener which will be added into the TestClass, it will
 * 1. Extract the test result of the TestNG tests
 * 2. Store the result and test case information into the remote ElasticSearch instance for further analysis/visualization
 */
class ElasticSearchTestListener implements ITestListener {
    HashMap<String, Object> info = null
    ElasticSearchHelper helper = null
    String indexNamePrefix = "automation_test_results"
    String indexName = ""
    String docType = "result"
    String suiteName = ""

    //The switch to enable the result collection
    boolean enable = (true || System.getenv()["STORE2ES"] != null)

    @Override
    void onTestStart(ITestResult result) {
        if (enable) {

        }
    }

    @Override
    void onTestSuccess(ITestResult result) {
        if (enable) {
            info = getResultInformation(result)
            helper.saveResult2ES(indexName, docType, JsonOutput.toJson(info))
        }
    }

    @Override
    void onTestFailure(ITestResult result) {
        if (enable) {
            info = getResultInformation(result)
            helper.saveResult2ES(indexName, docType, JsonOutput.toJson(info))
        }
    }

    @Override
    void onTestSkipped(ITestResult result) {
        if (enable) {
            info = getResultInformation(result)
            helper.saveResult2ES(indexName, docType, JsonOutput.toJson(info))
        }
    }

    @Override
    void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        if (enable) {
            info = getResultInformation(result)
            helper.saveResult2ES(indexName, docType, JsonOutput.toJson(info))
        }
    }

    @Override
    void onStart(ITestContext context) {
        if (enable) {
            suiteName = context.getSuite().getName()
            def config = getElasticSearchConfig()
            helper = new ElasticSearchHelper(config)
            indexName = getIndexName()
            if(!helper.isIndexExisting(indexName))
            {
                helper.createIndex(indexName, docType)
            }
        }
    }

    @Override
    void onFinish(ITestContext context) {
        if (enable) {
            helper.closeClient()
        }
    }

    //TODO, replace the config with the ElasticSearch instance which is deployed on the production environment
    private HashMap<String, Object> getElasticSearchConfig()
    {
        HashMap<String, Object> config = new HashMap<String, Object>()
        config.host = '10.75.60.76'//Contact Neil.Wang@blackboard.com for the detailed information about this server.
        config.port = 9200
        config.administrator = ""
        config.password = ""
        return config
    }

    //Generate the index name, it should be time based
    //TODO, implement an mechanism to generate the index name dynamically
    private String getIndexName() {
        DateFormat df = new SimpleDateFormat("yyyy.MM");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        return "${indexNamePrefix}-${reportDate}"
    }

    private String getStatusString(int status)
    {
        String statusString = "UNKNOWN"
        switch (status){
            case 1:
                statusString = "SUCCESS"
                break
            case 2:
                statusString = "FAILURE"
                break
            case 3:
                statusString = "SKIP"
                break
            case 4:
                statusString = "SUCCESS_PERCENTAGE_FAILURE"
                break
            case 16:
                statusString = "STARTED"
                break
        }
        return statusString
    }

    private HashMap<String, Object> getResultInformation(ITestResult result) {
        String host = result.getHost()
        String className = result.getTestClass().name
        String methodName = result.getMethod().getMethodName()
        String testName = result.getTestName()
        String name = result.getName()
        def parameters = result.getParameters()
        int status = result.getStatus()
        Throwable cause = result.getThrowable()
        Calendar c = Calendar.getInstance()
        Date startTime = new Date(result.getStartMillis())
        Date endTime = new Date(result.getEndMillis())

        HashMap<String, Object> info = new HashMap<String, Object>()
        info.suite_name = suiteName
        info.class_name = className
        info.method_name = methodName
        info.test_name = testName
        info.parameters = parameters
        info.host = host
        info.status = status
        info.timestamp = startTime
        info.start_time = startTime
        info.end_time = endTime
        info.name = name
        info.cause = cause?.getMessage()
        info.result = getStatusString(status)
        info.duration = result.getEndMillis() - result.getStartMillis()

        return info
    }
}
