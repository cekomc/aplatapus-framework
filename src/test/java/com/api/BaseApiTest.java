package com.api;

import com.api.components.ApiConstants;
import com.thoughtworks.xstream.XStream;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.response.ResponseBody;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import javax.imageio.ImageIO;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidator.reset;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class BaseApiTest {

    private String testEndpoint;
    protected String[] apiParameters;
    protected Map<String, String> pathParams = new HashMap<>();
    protected Map<String, String> queryParams = new HashMap<>();
    protected Map<String, String> postParams = new HashMap<>();
    protected String plainTextBody;
    protected XStream xmlStream = new XStream();

    public static RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
    public static RequestSpecification requestSpec;

    public String[] getApiParameters() {
        Object[][] csvRawData = getTestDataFromCsv("testName");
        return Arrays.copyOf(csvRawData[0], csvRawData[0].length, String[].class);
    }

    public void setupApi(String baseUri, String endpoint, boolean addResponseLog) {
        specInitialSetup(baseUri);
        if (addResponseLog) {
            specAddResponseLog();
        }
        specBuild();
        setTestEndpoint(endpoint);
    }

    public void verifyExactResponse(Response response) {
        assertTrue("ERROR: Response is not as expected.",
                   getFileContent(generateExpectedResponsePath()).replace(" ", "")
                       .equals(response.getBody().asString().replace(" ", "")));
    }

    public void setTestEndpoint(String endpoint) {
        testEndpoint = endpoint;
    }

    public void specAddResponseLog() {
        requestSpecBuilder.addFilter(new ResponseLoggingFilter());
        specBuild();
    }

    public void specBuild() {
        requestSpec = requestSpecBuilder.build();
    }

    public Object[][] getTestDataFromCsv(String testName) {
        String[] fileLine;
        List<String> testDataRow;
        ArrayList<Object[]> dataList = new ArrayList<>();

        try (Scanner scanner = new Scanner(getFile(generateTestDataFilePath()))) {
            while (scanner.hasNextLine()) {
                fileLine = scanner.nextLine().split(";");
                if (fileLine[0].replace("\"", "").equals(testName)) {
                    testDataRow = new ArrayList<>(Arrays.asList(fileLine));
                    testDataRow.remove(0);
                    dataList.add(testDataRow.toArray());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataList.toArray(new Object[dataList.size()][]);
    }

    public void specInitialSetup(String baseUri) {
        requestSpecBuilder.setContentType(ContentType.JSON).
            setBaseUri(getBaseUri(baseUri)).
            addFilter(new RequestLoggingFilter());
        specBuild();
    }

    public void specSetContentType(ContentType contentType) {
        requestSpecBuilder.setContentType(contentType);
        specBuild();
    }

    public String getBaseUri(String baseUri) {
        return ApiConstants.BaseUris.valueOf(baseUri).baseUri();
    }

    public File getFile(String fileLocation) {
        return new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource(fileLocation))
                .getFile());
    }

    public String generateTestDataFilePath() {
        return "api/testdata/" + generateFileName() + "TestData.csv";
    }

    public String generateFileName() {
        char[] endpointArray = getEndpoint().replaceAll("[{=?}]", "").toCharArray();
        String fileName = "";
        for (int i = 0; i < endpointArray.length; i++) {
            if (endpointArray[i] == '/') {
                fileName = fileName + Character.toUpperCase(endpointArray[++i]);
            } else {
                fileName = fileName + endpointArray[i];
            }
        }
        return fileName;

    }

    public String getEndpoint() {
        return ApiConstants.Endpoints.valueOf(testEndpoint).endpoint();
    }

    public Response getRequest() {
        return RestAssured.given().spec(requestSpec).pathParams(pathParams).queryParams(queryParams)
            .when()
            .get(getEndpoint()).then().extract().response();
    }

    public Response postRequest() {
        return RestAssured.given().spec(requestSpec).pathParams(pathParams).queryParams(queryParams)
            .body(postParams).when().post(getEndpoint()).then().extract().response();
    }

    public Response postRequest(String plainTextBody) {
        return RestAssured.given().spec(requestSpec).
            body(plainTextBody).when().post(getEndpoint().replace("post", "")).
            then().extract().response();
    }

    public void verifyStatusCode(Response response, int statusCode) {
        response.then().assertThat().statusCode(statusCode);
    }

    public void verifyJsonSchema(Response response) {
        response.then().assertThat()
            .body(matchesJsonSchemaInClasspath(generateExpectResponsePath()));
    }

    public String generateExpectResponsePath() {
        return "api/expectedresponses/" + generateFileName() + "Response.xml";
    }

    public JsonPath returnJson(Response response) {
        return response.body().jsonPath();
    }


    public void verifyExactXmlResponse(Response response) {
        assertEquals("ERROR: Response is not as expected.",
                     getFileContent(generateExpectResponsePath()),
                     response.getBody().asString().replaceAll("\\r\\n?|\\n", ""));
    }

    public String getFileContent(String filePath) {
        String responseBody = "";
        try (Scanner scanner = new Scanner(getFile(filePath))) {
            while (scanner.hasNextLine()) {
                responseBody = responseBody + scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public String[] getUsableTestData(String testData) {
        return testData.replaceAll("[\\[\\]]", "").split(", ");
    }

    public String generateExpectedResponsePath() {
        return "api/expectedresponses/" + generateFileName() + "Response.json";
    }

    private String generateExpectedResponsePathXML() {
        return "api/expectedresponses/" + generateFileName() + "Response.xml";
    }

    public void verifyResponseContentType(Response response, String contentType) {
        assertTrue("ERROR: Response body content type is not as expected.",
                   response.contentType().contains(contentType));
    }

    public void verifyResponseErrorMessage(Response response, String message) {
        assertTrue("ERROR: Message in response does not contain expected error.",
                   response.asString().contains(message));
    }

    private String generateExpectedResponsePathJpeg() {
        return "src/test/resources/api/expectedresponses/" + generateFileName() + "Response.jpeg";
    }

    private boolean isJpegResponseEqualToExpected(Response response) throws IOException {
        File expected = new File(generateExpectedResponsePathJpeg());
        InputStream actual = response.getBody().asInputStream();
        BufferedImage expectedImage = ImageIO.read(expected);
        BufferedImage actualImage = ImageIO.read(actual);

        if (expectedImage.getWidth() != actualImage.getWidth()
            || expectedImage.getHeight() != actualImage.getHeight()) {
           return false;
        }

        int width = expectedImage.getWidth();
        int height = expectedImage.getHeight();

        for(int y = 0; y < height; y++){
            for (int i = 0; i < height; i++) {
                if(expectedImage.getRGB(i,y) != actualImage.getRGB(i, y)){
                    return false;
                }
            }
        }
        return true;
    }

    public void verifyJpegResponse(Response response) throws IOException {
        assertTrue("Response body jpeg image is not the same as expected jpeg image.",
                   isJpegResponseEqualToExpected(response));
    }
}
