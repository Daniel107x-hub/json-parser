package src.main;

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
    private static final char WHITESPACE = ' ';
    private static final char POINT = '.';
    private static final char CARRIAGE_RETURN = '\r';
    private static final char NEW_LINE = '\n';
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
        map = readJsonTokens(tokens, 0);
        return map;
    }

    private Map<String, Object> readJsonTokens(List<Object> tokens, Integer startIndex){
        Map<String, Object> map = new HashMap<>();
        for(Integer index = startIndex ; index < tokens.size() ; index++){
            Object token = tokens.get(index);
            if(token instanceof Character && SPECIAL_CHARACTERS.contains((char) token)){
                char charToken = (char) token;
                if(charToken == CLOSING_CURLY_BRACE) return map;
                if(charToken == COMMA){
                    Object nextToken = tokens.get(index + 1);
                    if(!(nextToken instanceof String)) return null;
                }
                if(charToken == SEMICOLON){
                    Object key = tokens.get(index - 1);
                    if(!(key instanceof String)) return null;
                    Object value = readValue(tokens, index + 1);
                    map.put((String) key, value);
                  }
            }
        }
        return map;
    }

    private Object readValue(List<Object> tokens, Integer index){
        Object token = tokens.get(index);
        if(token instanceof Character && SPECIAL_CHARACTERS.contains((char) token)){
            char charToken = (char) token;
            if(charToken == OPENING_CURLY_BRACE){
                return readJsonTokens(tokens, index + 1);
            }
        }
        if(token instanceof String) return token;
        if(token instanceof Integer) return token;
        if(token instanceof Boolean) return token;
        return null;
    }

    private List<Object> getTokens(InputStream json) throws IOException {
        List<Object> tokenList = new ArrayList<>();
        while(json.available() > 0){
            json.mark(Integer.MAX_VALUE);
            char c = (char) json.read();
            if(WHITESPACE == c || CARRIAGE_RETURN == c || NEW_LINE == c) continue;
            if(SPECIAL_CHARACTERS.contains(c)){
                tokenList.add(c);
                continue;
            }
            json.reset();
            json.mark(Integer.MAX_VALUE);
            String stringValue = readString(json);
            if(stringValue != null){
                tokenList.add(stringValue);
                continue;
            }
            json.reset();
            json.mark(Integer.MAX_VALUE);
            Integer numericValue = readNumeric(json);
            if(numericValue != null){
                tokenList.add(numericValue);
                continue;
            }
            json.reset();
            json.mark(Integer.MAX_VALUE);
            Boolean booleanValue = readBoolean(json);
            if(booleanValue != null){
                tokenList.add(booleanValue);
                continue;
            }
            json.reset();
            json.mark(Integer.MAX_VALUE);
            boolean isNull = readNull(json);
            if(isNull){
                tokenList.add(null);
                continue;
            }
            return null;
        }
        if(json.available() > 0) return null;
        return tokenList;
    }

    private boolean readNull(InputStream stream) throws IOException {
        char currentChar = (char) stream.read();
        for(char letter : "null".toCharArray()){
            if(letter != currentChar) return false;
            stream.mark(Integer.MAX_VALUE);
            currentChar = (char) stream.read();
        }
        stream.reset();
        return true;
    }

    private Boolean readBoolean(InputStream stream) throws IOException {
        char currentChar = (char) stream.read();
        String booleanMatch;
        if(currentChar == 't') booleanMatch = "true";
        else if(currentChar == 'f') booleanMatch = "false";
        else return null;
        for(char letter : booleanMatch.toCharArray()){
            if(letter != currentChar) return null;
            stream.mark(Integer.MAX_VALUE);
            currentChar = (char) stream.read();
        }
        stream.reset();
        return Boolean.parseBoolean(booleanMatch);
    }

    /**
     * Numbers in json are read until we find a character which is not a number
     * @param stream
     * @return Integer value if parseable or null
     * @throws IOException
     */
    private Integer readNumeric(InputStream stream) throws IOException {
        char currentChar = (char) stream.read();
        StringBuilder stringBuilder = new StringBuilder();
        while(stream.available() > 0 && Character.isDigit(currentChar)){
            if(WHITESPACE == currentChar) break;
            stringBuilder.append(currentChar);
            stream.mark(Integer.MAX_VALUE);
            currentChar = (char) stream.read();
        }
        stream.reset();
        Integer intValue;
        try{
            intValue = Integer.parseInt(stringBuilder.toString());
        }catch(NumberFormatException ex){
            return null;
        }
        return intValue;
    }

    private String readString(InputStream stream) throws IOException {
        char currentChar = (char) stream.read();
        if(currentChar != DOUBLE_QUOTES) return null;
        if(stream.available() <= 0) return null;
        currentChar = (char) stream.read();
        StringBuilder stringBuilder = new StringBuilder();
        while(stream.available() > 0){
            stringBuilder.append(currentChar);
            currentChar = (char) stream.read();
            if(currentChar == DOUBLE_QUOTES) break;
        }
        if(currentChar != DOUBLE_QUOTES) return null;
        return stringBuilder.toString();
    }
}
