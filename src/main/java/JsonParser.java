package src.main.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    public Map<String, Object> parse(InputStream json){
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    private List<Object> getTokens(InputStream json) throws IOException {
        List<Object> tokenList = new ArrayList<>();
        while(json.available() > 0){
            char c = (char) json.read();
        }
        return tokenList;
    }
}
