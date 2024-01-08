package src.main.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JsonParser {
    private static final char OPENING_CURLY_BRACE = '{';
    private static final char CLOSING_CURLY_BRACE = '}';
    private static final char OPENING_BRACKET = '[';
    private static final char CLOSING_BRACKET = ']';
    private static final char DOUBLE_QUOTES = '"';
    private static final char SEMICOLON = ':';
    private static final char COMMA = ',';
    private static final Set<Character> SPECIAL_CHARACTERS = new HashSet<>(Arrays.asList(
            OPENING_CURLY_BRACE,
            CLOSING_CURLY_BRACE,
            OPENING_BRACKET,
            CLOSING_BRACKET,
            SEMICOLON,
            COMMA
    ));

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
        Map<String, Object> map;
        List<Object> tokens = getTokens(json);
        if(tokens == null) return null;
        if(!tokens.get(0).equals(OPENING_CURLY_BRACE)) return null;
        map = readJsonTokens(tokens, 1);
        return map;
    }

    private Map<String, Object> readJsonTokens(List<Object> tokens, Integer startIndex){
        Map<String, Object> map = new HashMap<>();
        for(Integer index = startIndex ; index < tokens.size() ; index++){
            Object token = tokens.get(index);
            if(token instanceof Character && SPECIAL_CHARACTERS.contains((char) token)){
                char charToken = (char) token;
                if(charToken == SEMICOLON){
                    Object prevToken = tokens.get(index - 1);
                    if(prevToken == null) return null;
                    if(!(prevToken instanceof String)) return null;
                    String key = (String) prevToken;

                }
            }
        }
        return map;
    }

    private List<Object> getTokens(InputStream json) throws IOException {
        List<Object> tokenList = new ArrayList<>();
        while(json.available() > 0){
            char c = (char) json.read();
            if(SPECIAL_CHARACTERS.contains(c)){
                tokenList.add(c);
                continue;
            }
            if(DOUBLE_QUOTES == c){
                String stringValue = readString(json);
                if(stringValue == null) return null; // If string is not closed, then return null in tokens
                tokenList.add(stringValue);
            }
        }
        return tokenList;
    }

    private String readString(InputStream stream) throws IOException {
        if(stream.available() <= 0) return null;
        StringBuilder stringBuilder = new StringBuilder();
        char currentChar = (char) stream.read();
        while(stream.available() > 0 && currentChar != DOUBLE_QUOTES){
            stringBuilder.append(currentChar);
            currentChar = (char) stream.read();
        }
        if(currentChar != DOUBLE_QUOTES) return null;
        return stringBuilder.toString();
    }
}
