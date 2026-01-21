package com.ai.roboteacher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleP {

    public static void main(String[] args) {


        String s = "My name is Dipanjan Biswas\\c";

//       // Pattern p = Pattern.compile("[\n\\\\]");
//        // Pattern p = Pattern.compile("[\\\\c]");
        Pattern p = Pattern.compile("\\\\c");

        Matcher m = p.matcher(s);

        while (m.find()) {
            System.out.println("Found: " + m.group());
        }
    }
}
