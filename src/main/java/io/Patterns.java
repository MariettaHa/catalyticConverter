package io;

public class Patterns {

    public static String crsFoodSetPattern = "(?i)f(ood)?:\\s*.*";
    public static String crsInhibitorPattern = "\\{.*\\}";
    public static String crsCatalystPattern = "\\[.*\\]";
    public static String crsReactionPatternOld =
            "(.*)(\t)*:" + //rId
                    "(.*)" + //reactants
                    "(" + crsCatalystPattern + //catalysts
                    "|" + crsCatalystPattern + " " + crsInhibitorPattern + //catalysts and inhibitors
                    "|" + crsInhibitorPattern + ")" + //inhibitors
                    "(<?[-=]>)" + // (ir)reversible
                    "(.*)"; //products
    public static String crsReactionPattern = "(.*):(.*)(\\[.*\\])?(\\{.*\\})?(<?[-=]>)(.*)";
    public static String wimMolOrFoodPattern = "(.*)\t(.*)\n?";
    public static String wimReactionPattern = "(.*)\t(.*) (<?=>) (.*)\t(.*)\t(.*)\n?";
    public static String dnfPattern = "((^\\s*|\\s*&\\s*|\\s*,\\s*)([\\d]+(\\.[\\d]+)?)?\\s*([A-Za-z_])([A-Za-z0-9_'-])*)+";

}
