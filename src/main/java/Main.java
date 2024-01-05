package src.main.java;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        InputStream stream = new BufferedInputStream(System.in);
        JsonParser parser = new JsonParser();
        Map<String, Object> parsedJson = null;
        try {
            parsedJson = parser.parse(stream);
        }catch(IOException e){
            System.out.println("Error reading from input streaml");
        }
        if(parsedJson == null) System.out.println(1);
        else System.out.println(0);
    }
}
