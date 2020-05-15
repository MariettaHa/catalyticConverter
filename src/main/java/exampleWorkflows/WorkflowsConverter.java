package exampleWorkflows;

import converter.Converter;

import java.nio.file.Paths;

public class WorkflowsConverter {

    public static void workflow_crs_to_wim() {
        Converter converter = new Converter();
        converter.convert(Paths.get("src/main/resources/exampleFiles/crs/example-1.crs"),
                Paths.get("src/main/resources/exampleFiles/output/example-1.wim"));
    }

    public static void workflow_wim_to_crs() {
        Converter converter = new Converter();
        converter.convert(Paths.get("src/main/resources/exampleFiles/output/example-9.wim"),
                Paths.get("src/main/resources/exampleFiles/output/example-9.crs"));
    }

    public static void workflow_crs_to_sbml() {
        Converter converter = new Converter();
        converter.convertToSBML(Paths.get("src/main/resources/exampleFiles/crs/example-9.crs"),
                Paths.get("src/main/resources/exampleFiles/output/example-9.xml"), null,
                2, 4);
    }

    public static void workflow_wim_to_sbml() {
        Converter converter = new Converter();
        converter.convertToSBML(Paths.get("src/main/resources/exampleFiles/wim/Original_Archaea.txt"),
                Paths.get("src/main/resources/exampleFiles/output/Original_Archaea.xml"), null,
                2, 4);
    }

    public static void workflow_wim_to_db() {
        Converter converter = new Converter();
        converter.convert(Paths.get("src/main/resources/exampleFiles/wim/Original_Archaea.txt"),
                Paths.get("src/main/resources/exampleFiles/output/Original_Archaea.db"));
    }

    public static String workflow_convert_file_to_str() {
        String crsStr = "# Example CRS\n" +
                "r1: a+b,c [d,e*f] {g,h*i} -> j+k,l\n" +
                "r2: j,k [l,n] <-> m\n" +
                "Food: a,b,c,d,e,f,g,h,i,n";
        Converter converter = new Converter();
        return converter.convertStringToString(crsStr, "crs", "wim");
    }
}
