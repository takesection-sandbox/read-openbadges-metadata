package com.pigumer;

import com.pigumer.logic.Logic;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class App 
{

    public static void main( String[] args ) throws Exception
    {
        Logic logic = new Logic();
        String filename = args[0];
        try (InputStream in = new FileInputStream(filename)) {
            Collection<Map<String, String>> result = logic.analyze(in);
            System.out.println(result.toString());
        }
    }
}
