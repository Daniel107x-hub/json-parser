package src.main.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private static final char OPENING_CURLY_BRACE = '{';
    private static final char CLOSING_CURLY_BRACE = '}';
    private static final char OPENING_BRACKET = '[';
    private static final char CLOSING_BRACKET = ']';
    private static final char DOUBLE_QUOTES = '"';
    private static final char SEMICOLON = ':';
    private static final char COMMA = ',';

    public int validate(InputStream json) {
        Map<String, Object> parsedJson;
        try {
            parsedJson = this.parse(json);
        }catch(IOException exception){
            parsedJson = null;
            System.out.println("Exception when reading from IO");
        }
        if(parsedJson != null) return 0;
        return 1;
    }

    public Map<String, Object> parse(InputStream json) throws IOException {
        if(json.available() == 0) return null;
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
