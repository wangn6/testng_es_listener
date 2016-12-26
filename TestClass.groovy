package org.wangn6.es_testng_listener.sample

import org.wangn6.es_testng_listener.ElasticSearchTestListener

import groovy.util.logging.Slf4j
import org.testng.Assert
import org.testng.ITestContext
import org.testng.annotations.*
import org.testng.asserts.SoftAssert

import java.lang.reflect.Method
import java.nio.file.Paths

@Slf4j
@Listeners([ElasticSearchTestListener.class])//This is to monitor the execution result of our test and the result will be saved to a central ES instance for future analysis
class TestClass {

    @BeforeClass
    public void initClass() {
        
    }

    def TestClass() {
    }

    //server type should be stage or prod

    @DataProvider(name = "xmlData", parallel = true)
    public Object[][] getTestData(Method method, ITestContext context) 
    }


    @BeforeMethod
    public void initMethod() {
        
    }

    @AfterMethod
    public void cleanMethod() {
        
    } 
}
