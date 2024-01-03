package src.test.java;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Spy;
import src.main.java.JsonParser;

import java.util.Map;

public class JsonParserTests{
    @Spy private JsonParser jsonParser;

    @Test
    public void parsesBasicJsonStringCorrectly(){
        String jsonString = "{}";
        Map<String, Object> parsedJson = jsonParser.parse(jsonString);
        Assert.assertNotNull(parsedJson);
        Assert.assertEquals(0, parsedJson.size());
    }
}
