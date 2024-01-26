package src.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
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
    private static final char CARRIAGE_RETURN = '\r';
    private static final char NEW_LINE = '\n';
    private static final Set<Character> JSON_SYNTAX = new HashSet<>(Arrays.asList(
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
        List<Object> tokens = getTokens(json);
        Object result = null;
        if(tokens == null) return null;
        try
        {
            result = parseTokens(tokens);
            return (Map<String, Object>) result;
        }catch(InvalidObjectException ex){
            return null;
        }
    }

    private Object parseTokens(List<Object> tokens) throws InvalidObjectException {
        Object accumulator = null;
        Object token = tokens.remove(0);
        if(token != null && token.equals(OPENING_CURLY_BRACE)) accumulator = new HashMap<>();
        else if(token != null && token.equals(OPENING_BRACKET)) accumulator = new ArrayList<>();
        if(accumulator == null) return token;
        while(!tokens.isEmpty()){
            token = tokens.remove(0);
            if(token != null && token.equals(CLOSING_CURLY_BRACE)){
                if(!(accumulator instanceof Map)) throw new InvalidObjectException("Not valid character");
                return accumulator;
            }
            if(token != null && token.equals(CLOSING_BRACKET)){
                if(!(accumulator instanceof List)) throw new InvalidObjectException("Not valid character");
                return accumulator;
            }
            if(accumulator instanceof List){
                if((token instanceof Character) && JSON_SYNTAX.contains((char)token)) throw new InvalidObjectException("Not valid character");
                ((List<Object>)accumulator).add(token);
                token = tokens.get(0);
                if(token instanceof Character && token.equals(CLOSING_BRACKET)) continue;
                if(!(token instanceof Character && token.equals(COMMA))) throw new InvalidObjectException("Not valid character");
                tokens.remove(0);
                token = tokens.get(0);
                if(token instanceof Character && token.equals(CLOSING_BRACKET)) throw new InvalidObjectException("Not valid character at this position"); // If this is the final value, the net token could be a curly brace
            }
            if(accumulator instanceof Map){
                if(!(token instanceof String)) throw new InvalidObjectException("Not valid character");
                String key = (String)token;
                token = tokens.remove(0); //After key, there should be a semicolon
                if(!(token instanceof Character && token.equals(SEMICOLON))) throw new InvalidObjectException("Not valid character");
                try // Then we try to get the value
                {
                    Object value = parseTokens(tokens);
                    ((Map<String, Object>)accumulator).put(key, value);
                }catch(InvalidObjectException ex){
                    break;
                }
                token = tokens.get(0);
                if(token instanceof Character && token.equals(CLOSING_CURLY_BRACE)) continue; // If this is the final value, the net token could be a curly brace
                if(!(token instanceof Character && token.equals(COMMA))) throw new InvalidObjectException("Not valid character"); // If not, there should be a comma, but after a comma there cannot be a curly brace
                tokens.remove(0);
                token = tokens.get(0);
                if(token instanceof Character && token.equals(CLOSING_CURLY_BRACE)) throw new InvalidObjectException("Not valid character at this position"); // If this is the final value, the net token could be a curly brace
            }
        }
        return null;
    }

    /**
     * Tokenizes the input stream in tokens that belong to the json syntax. Returns the list with ll json tokens.
     *
     * <p> If unable to tokenize any character sequence, {@code null} will be returned.
     * @param json - Input stream, not null
     * @return {@code List} with all json tokens
     * @throws IOException - In case any read operation fail
     */
    private List<Object> getTokens(InputStream json) throws IOException {
        Objects.requireNonNull(json);
        List<Object> tokenList = new ArrayList<>();
        while(json.available() > 0){
            json.mark(Integer.MAX_VALUE);
            char c = (char) json.read();
            if(WHITESPACE == c || CARRIAGE_RETURN == c || NEW_LINE == c) continue;
            if(JSON_SYNTAX.contains(c)){
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

    /**
     * This method will return a boolean {@code true} or {@code false} if the upcoming characters in the InputStream represent the null value.
     *
     * <p>This method will accumulate incoming characters from the stream. If the sequence represents a null value, {@code true} will be returned. In any other case, the return value will be {@code false}
     * @param stream - The input stream, not null
     * @return boolean - {@code true} if the chracter sequence represents a {@code null} value. {@code false} in any other case
     * @throws IOException -  if an I/O error occurs when reading from stream.
     */
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

    /**
     * This method will return a boolean {@code true} or {@code false} if the upcoming characters in the InputStream represent either of the 2 boolean values.
     *
     * <p>This method will accumulate incoming characters from the stream. If the sequence represents either of the booleans, the boolean value will be returned, else,  this method will return {@code null}.
     * @param stream - The input stream, not null
     * @return Boolean - Parsed boolean from stream or {@code null}
     * @throws IOException -  if an I/O error occurs when reading from stream.
     */
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
     * This method will return a string if the upcoming characters in the InputStream form an integer.
     *
     * <p>This method will accumulate incoming digits from the stream. If a whitespace is found, the accumulating ends and the formed integer is returned. If any non digit is found, this method will return {@code null}.
     * @param stream - The input stream, not null
     * @return Integer - Parsed integer from stream or {@code null}
     * @throws IOException -  if an I/O error occurs when reading from stream.
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
        try{
            return Integer.parseInt(stringBuilder.toString());
        }catch(NumberFormatException ex){
            System.out.println("Read data is not a number");
        }
        return null;
    }

    /**
     * This method will return a string if the upcoming characters in the InputStream form a json string.
     *
     * <p>By definition, the json string will start and end with double quotes. If that's not the case,
     * this method will return {@code null}.
     * @param stream - The input stream, not null
     * @return String between double quotes or null if quotes neither opened nor closed
     * @throws IOException -  if an I/O error occurs when reading from stream.
     */
    private String readString(InputStream stream) throws IOException {
        Objects.requireNonNull(stream);
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
