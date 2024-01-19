package src.test.java;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import src.main.java.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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
    public void parseStringMultiKeyValueJson() throws IOException {
        Random random = new Random();
        int keyValuePairs = random.nextInt(20);
        Map<String, String> json = generateRandomJsonWithNStringKeyValues(keyValuePairs);
        String jsonString = new Gson().toJson(json);
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        Assert.assertNotNull(parsedJson);
        Assert.assertEquals(keyValuePairs, parsedJson.size());
        for(String key : json.keySet()){
            Object value = parsedJson.get(key);
            Assert.assertNotNull(value);
            Assert.assertTrue(value instanceof String);
            Assert.assertEquals(json.get(key), value);
        }
    }

    @Test
    public void failToParseInvalidJsonString() throws IOException {
        String jsonString = "{\"key\":\"value\",}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        Map<String, Object> parsedJson = jsonParser.parse(inputStream);
        System.out.println(" Hello");
    }

    private Map<String, String> generateRandomJsonWithNStringKeyValues(int pairs){
        Map<String, String> json = new HashMap<>();
        for(int i = 0 ; i < pairs ; i++){
            String randomKey;
            do
            {
                randomKey = UUID.randomUUID().toString();
            }while(json.containsKey(randomKey));
            String randomValue = UUID.randomUUID().toString();
            json.put(randomKey, randomValue);
        }
        return json;
    }
}
