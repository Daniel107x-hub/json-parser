package src.test.java;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import src.main.java.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class JsonParserTests{
    private JsonParser jsonParser;

    @Before
    public void initializeClass(){
        jsonParser = new JsonParser();
    }

    @Test
    public void failValidationOnInvalidJson(){
        String jsonString = "";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        int result = jsonParser.validate(inputStream);
        Assert.assertEquals(1, result);
    }

    @Test
    public void passValidationOnValidJson(){
        String jsonString = "{}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        int result = jsonParser.validate(inputStream);
        Assert.assertEquals(0, result);
    }

    @Test
    public void parsesBasicJsonStringCorrectly() throws IOException {
        String jsonString = "{}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        Assert.assertNotNull(parsedJson);
        Assert.assertEquals(0, parsedJson.size());
    }

    @Test
    public void unableToParseInvalidJson() throws IOException {
        String jsonString = "";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        Assert.assertNull(parsedJson);
    }

    @Test
    public void parseStringSingleKeyValueJson() throws IOException {
        String jsonString = "{\"key\":\"value\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        Assert.assertNotNull(parsedJson);
        Assert.assertEquals(1, parsedJson.size());
        Object value = parsedJson.get("key");
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof String);
        Assert.assertEquals("value", value);
    }

    @Test
    public void parseStringMultiKeyValueJson() throws IOException {
        String jsonString = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        Assert.assertNotNull(parsedJson);
        Assert.assertEquals(2, parsedJson.size());
        Object value1 = parsedJson.get("key1");
        Assert.assertNotNull(value1);
        Assert.assertTrue(value1 instanceof String);
        Assert.assertEquals("value1", value1);
        Object value2 = parsedJson.get("key2");
        Assert.assertNotNull(value2);
        Assert.assertTrue(value2 instanceof String);
        Assert.assertEquals("value2", value2);
    }
}
