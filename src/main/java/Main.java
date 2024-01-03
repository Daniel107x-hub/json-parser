package src.main.java;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if(System.in.available() == 0) throw new IllegalArgumentException("Provide the json string");
        InputStream stream = new BufferedInputStream(System.in);
        JsonParser parser = new JsonParser();
        Map<String, Object> parsedJson = parser.parse(stream);
        if(parsedJson == null) System.out.println(1);
        else System.out.println(0);
    }
}
