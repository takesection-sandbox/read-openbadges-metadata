package com.pigumer;

import java.io.*;

import com.pigumer.logic.Logic;

public class App 
{

    public static void main( String[] args ) throws Exception
    {
        Logic logic = new Logic();
        String filename = args[0];
        try (InputStream in = new FileInputStream(filename)) {
            String json = logic.analyze(in);
            if (json != null) {
                System.out.println("output: " + json);
            }
        }
    }
}
