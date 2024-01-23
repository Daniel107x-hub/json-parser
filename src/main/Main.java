package src.main;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream stream = new BufferedInputStream(System.in);
        JsonParser parser = new JsonParser();
        int result = parser.validate(stream);
        System.out.println(result);
    }
}
