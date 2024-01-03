package src.test.java;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import src.main.java.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class MainTests {
    @Spy private OutputStream outputStream;

    @Before
    public void configureStdOutRead(){
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

    }

    @After
    public void cleanOutputStreamAfterTest() throws IOException {
        outputStream.flush();
    }

    @Test
    public void mainClassPrintToStdOut() throws IOException {
        String string = " ";
        System.setIn(new ByteArrayInputStream(string.getBytes()));
        Main.main(null);
        Assert.assertNotNull(readOutput(outputStream));
    }

    @Test
    public void parsesBasicJsonSuccessfully() throws IOException {
        String json = "{}";
        System.setIn(new ByteArrayInputStream(json.getBytes()));
        Main.main(null);
        Assert.assertEquals("0", readOutput(outputStream));
    }

    private String readOutput(OutputStream stream){
        return outputStream.toString().trim();
    }
}
