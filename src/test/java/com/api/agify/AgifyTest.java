package com.api.agify;

import com.api.BaseApiTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.apache.http.HttpStatus.SC_OK;

public class AgifyTest extends BaseApiTest {

    @BeforeClass
    @Parameters({"baseUri"})
    public void getName(@Optional("ENV_ENUM") String baseUri) {
        setupApi(baseUri, "NAME", true);
        apiParameters = getApiParameters();
    }

    @BeforeMethod
    public void setTestData(Object[] testData) {
        String[] usableTestData = getUsableTestData(Arrays.toString((Object[]) testData[0]));
        pathParams.put(apiParameters[0], usableTestData[0]);
        queryParams = Collections.emptyMap();
    }

    @DataProvider(name = "testData")
    public Object[][] testData(Method method) {
        return getTestDataFromCsv(method.getName());
    }

    @Test(priority = 0, dataProvider = "testData")
    public void getName(String[] testData) {
        Response response = getRequest();
        verifyStatusCode(response, SC_OK);
        verifyExactResponse(response);
    }
}
