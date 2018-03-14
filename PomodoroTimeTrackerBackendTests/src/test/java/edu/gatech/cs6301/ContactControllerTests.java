package edu.gatech.cs6301;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.skyscreamer.jsonassert.JSONAssert;

public class ContactControllerTests {

    private String baseUrl;
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpclient;
    private boolean setupdone;
    private String usernamefile = "username.txt";

    @Before
    public void runBefore() {
	if (!setupdone) {
	    System.out.println("*** SETTING UP TESTS ***");
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(usernamefile)));
        } catch (IOException e) {
	    content = "";
        }
        String basedir = content.trim();
	cm.setMaxTotal(100);
	cm.setDefaultMaxPerRoute(10);
	HttpHost host = new HttpHost("gazelle.cc.gatech.edu", 8888);
	cm.setMaxPerRoute(new HttpRoute(host), 10);
	httpclient = HttpClients.custom().setConnectionManager(cm).build();
	baseUrl = host.toURI() + "/" + basedir;
	if (basedir.length() == 0) baseUrl = baseUrl + "ptt";
	else baseUrl = baseUrl + "/ptt";
        System.out.println("BaseURL: " + baseUrl);
	    setupdone = true;
	}
        System.out.println("*** STARTING TEST ***");
    }

    @After
    public void runAfter() {
        System.out.println("*** ENDING TEST ***");
    }

    // *** YOU SHOULD NOT NEED TO CHANGE ANYTHING ABOVE THIS LINE ***
    
    @Test
    public void createContactTest() throws Exception {
	deleteContacts();

        try {
            CloseableHttpResponse response =
		createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"1\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
            // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateContactTest() throws Exception {
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            response.close();

            response = updateContact("1", "Tom", "Doe", "(123)-456-7890" , "tom@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"1\",\"firstname\":\"Tom\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"tom@doe.org\"}";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getContact("1");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"1\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllContactsTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            EntityUtils.consume(response.getEntity());
            response.close();
            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllContacts();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[{\"id\":\"1\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"},"
                    + "{\"id\":\"2\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(9876)-543-210\",\"email\":\"jane@wall.com\"}]";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void DeleteContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            response.close();
            deleteContact("1");
            response = getAllContacts();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[]";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleDeleteOneContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            response.close();
            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            response.close();

            deleteContact("1");
            response = getAllContacts();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[{\"id\":\"2\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(9876)-543-210\",\"email\":\"jane@wall.com\"}]";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleUpdateOneContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            EntityUtils.consume(response.getEntity());
            response.close();
            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            EntityUtils.consume(response.getEntity());
            response.close();

            updateContact("2", "Jane", "Wall", "(6789)-210-534" , "jane@wall.com");
            response = getContact("2");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"2\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(6789)-210-534\",\"email\":\"jane@wall.com\"}";
	    // Assert.assertEquals(expectedJson,strResponse);
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse createContact(String firstname, String familyname, String phonenumber, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"familyname\":\"" + familyname + "\"," +
                "\"phonenumber\":\"" + phonenumber + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateContact(String id, String firstname, String familyname, String phonenumber, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"familyname\":\"" + familyname + "\"," +
                "\"phonenumber\":\"" + phonenumber + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getContact(String id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/" + id);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getAllContacts() throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private void deleteContact(String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        EntityUtils.consume(response.getEntity());
        response.close();
    }

    private void deleteContacts() throws IOException {
	HttpDelete httpDelete = new HttpDelete(baseUrl);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        EntityUtils.consume(response.getEntity());
        response.close();
    }
}
