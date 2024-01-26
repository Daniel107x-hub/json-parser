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
        if(tokens == null) return null;
        return readJsonTokens(tokens, 0);
    }

    private Map<String, Object> readJsonTokens(List<Object> tokens, Integer startIndex){
        Map<String, Object> map = new HashMap<>();
        if(!tokens.get(0).equals(OPENING_CURLY_BRACE)) return null;
        for(Integer index = startIndex ; index < tokens.size() ; index++){
            Object token = tokens.get(index);
            if(token instanceof Character && JSON_SYNTAX.contains((char) token)){
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

    /**
     * Reads the possible value after a semicolon according to the json syntax.
     *
     * <p>If the token is a single character in the json syntax, it needs to eb interpreted accordingly. Possible value characters are braces and brackets.
     * In the case of curly braces, the nested json needs to be interpreted.
     * In the case of brackets, the json array needs to be read.
     *
     * <p>If the token is a solid value, ust return it.
     * @param tokens - List of json tokens from the input stream
     * @param index - Current index where interpretation is happening
     * @return Interpretation of the section being read
     */
    private Object readValue(List<Object> tokens, Integer index){
        Object token = tokens.get(index);
        if(token instanceof Character && JSON_SYNTAX.contains((char) token)){
            char charToken = (char) token;
            if(charToken == OPENING_CURLY_BRACE){
                return readJsonTokens(tokens, index + 1);
            }
        }
        return token;
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
