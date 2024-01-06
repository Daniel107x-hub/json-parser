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
}
