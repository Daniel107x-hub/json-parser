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
                    Object previousToken = tokens.get(index - 1);
                    if(previousToken == null) return null;
                    Object nextToken = tokens.get(index + 1);
                    if(!(nextToken instanceof String)) return null;
                }
                if(charToken == SEMICOLON){
                    Object key = tokens.get(index - 1);
                    if(!(key instanceof String)) return null;
                    Object value = readValue(tokens, index + 1);
                    if(value == null) return null;
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
        return null;
    }

    private List<Object> getTokens(InputStream json) throws IOException {
        List<Object> tokenList = new ArrayList<>();
        while(json.available() > 0){
            char c = (char) json.read();
            if(WHITESPACE == c) continue;
            if(SPECIAL_CHARACTERS.contains(c)){
                tokenList.add(c);
                continue;
            }
            if(DOUBLE_QUOTES == c){
                String stringValue = readString(json);
                if(stringValue == null) return null; // If string is not closed, then return null in tokens
                tokenList.add(stringValue);
            }
            json.mark(Integer.MAX_VALUE);
            Double numericValue = readNumeric(json);
            if(numericValue != null){
                tokenList.add(numericValue);
//                continue;
            }
            json.reset();
            //TODO: Add boolean and nulls
        }
        if(json.available() > 0) return null;
        return tokenList;
    }

    /**
     * Numbers in json are read until we find:
     *  - Whitespace
     *  - Comma
     * In case we find a alphabetic character, we will return this as null
     * Except in the case of a ., when the number is interpreted as a decimal value.
     * In such case, it may only have a single .
     * @param stream
     * @return
     * @throws IOException
     */
    private Double readNumeric(InputStream stream) throws IOException {
        if(stream.available() <= 0) return null;
        StringBuilder stringBuilder = new StringBuilder();
        char currentChar = (char) stream.read();
        boolean hasPointAppeared = false;
        while(stream.available() > 0){
            if(Character.isLetter(currentChar)) return null;
            if(WHITESPACE == currentChar) break;

            stringBuilder.append(currentChar);
            currentChar = (char) stream.read();
        }
        return 1D;
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
