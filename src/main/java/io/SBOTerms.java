package io;

public class SBOTerms {

    public static String defaultSBOCatalyst = "SBO:0000013";
    public static String defaultSBOInhibitor = "SBO:0000020";
    public static String SBOGeneralStimulator = "SBO:0000459";
    public static String defaultSBOProduct = "SBO:0000011";
    public static String defaultSBOReactant = "SBO:0000010";

    public static String getDefaultSBOCatalyst() {
        return defaultSBOCatalyst;
    }

    public static void setDefaultSBOCatalyst(String defaultSBOCatalyst) {
        SBOTerms.defaultSBOCatalyst = defaultSBOCatalyst;
    }

    public static String getDefaultSBOInhibitor() {
        return defaultSBOInhibitor;
    }

    public static void setDefaultSBOInhibitor(String defaultSBOInhibitor) {
        SBOTerms.defaultSBOInhibitor = defaultSBOInhibitor;
    }

    public static String getSBOGeneralStimulator() {
        return SBOGeneralStimulator;
    }

    public static void setSBOGeneralStimulator(String SBOGeneralStimulator) {
        SBOTerms.SBOGeneralStimulator = SBOGeneralStimulator;
    }

    public static String getDefaultSBOProduct() {
        return defaultSBOProduct;
    }

    public static String getDefaultSBOReactant() {
        return defaultSBOReactant;
    }
}
