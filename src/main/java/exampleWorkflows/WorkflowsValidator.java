package exampleWorkflows;

import converter.Validator;

import java.nio.file.Paths;

public class WorkflowsValidator {

    public static void workflow_validate_CRS() {
        Validator validator = new Validator();
        System.out.println(validator.validateFile(Paths.get("src/main/resources/exampleFiles/crs/example-9.crs"),
                "crs", null));
    }

    public static void workflow_validate_WIM() {
        Validator validator = new Validator();
        System.out.println(validator.validateFile(Paths.get("src/main/resources/exampleFiles/wim/Original_Archaea.txt"),
                "wim", null));
    }

    public static void workflow_validate_InvalidWIM() {
        Validator validator = new Validator();
        System.out.println(validator.validateFile(Paths.get("src/main/resources/exampleFiles/wim/WrongWimTest.wim"),
                "wim", null));
    }

    public static void workflow_validate_WIM_against_Scheme() {
        Validator validator = new Validator();
        System.out.println(validator.validateFile(Paths.get("src/main/resources/exampleFiles/wim/Original_Archaea.txt"),
                "wim", Paths.get("src/main/resources/exampleFiles/parsingSchemes/wimParsingScheme.xml")));
    }

    public static void workflow_validate_parsingScheme() {
        Validator validator = new Validator();
        System.out.println(validator.validateParsingScheme(
                Paths.get("src/main/resources/exampleFiles/parsingSchemes/wimParsingScheme.xml")));
    }
}
