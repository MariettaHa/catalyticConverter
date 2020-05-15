package io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Misc {

    public static String correctSBOTerm(String sboTerm, String parent) {
        if (sboTerm.matches("(SBO:)?\\d{7}")) {
            return sboTerm;
        } else {
            switch (parent) {
                case "species":
                    return "SBO:0000241"; //"functional entity" as default
                case "reaction":
                    return "SBO:0000375"; //"process" as default
                default:
                    return "SBO:0000000"; //"sbo term" as default
            }
        }
    }

    public static String getFileEnding(String type) {
        switch (type.toLowerCase()) {
            case "xml":
            case "sbml":
                return "xml";
            case "wim":
            case "txt":
                return "txt";
            case "crs":
                return "crs";
            case "db":
                return "db";
            default:
                return "txt";
        }
    }

    public static boolean writeContentToFile(String s, String path) {
        try {
            Files.writeString(Paths.get(path), s);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void copyContentToFile(Path inputPath, Path outputPath) throws IOException {
        File inputFile = new File(String.valueOf(inputPath));
        FileInputStream inputFileStream = new FileInputStream(inputFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputFileStream));
        FileWriter fileWriter = new FileWriter(String.valueOf(outputPath), false);
        BufferedWriter out = new BufferedWriter(fileWriter);
        String aLine = null;
        while ((aLine = in.readLine()) != null) {
            out.write(aLine);
            out.newLine();
        }
        in.close();
        out.close();
    }

    public static String getContentOfFile(Path inputPath) throws IOException {
        return Files.readString(inputPath, StandardCharsets.US_ASCII);
    }

}
