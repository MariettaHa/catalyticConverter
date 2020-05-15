package converter;

import io.Misc;
import io.documents.DBDoc;
import io.documents.SBMLDoc;
import io.parser.CRSParser;
import io.parser.CustomParser;
import io.parser.WimParser;
import io.writer.SBMLDocWriter;
import model.DataModel;
import org.apache.commons.io.FileUtils;
import org.sbml.jsbml.SBMLError;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Validator {

    /**
     * Validates input file, against scheme if not null
     *
     * @param filePath          The path of the file to validate
     * @param type              The input file format
     * @param parsingSchemePath not null for custom file formats,
     *                          file path of parsing scheme
     * @return true if file is valid
     */
    public boolean validateFile(Path filePath, String type, Path parsingSchemePath) {
        File file = filePath.toFile();
        if (file.exists()) {
            if (parsingSchemePath == null) {
                switch (type) {
                    case "crs":
                        return CRSParser.validateFile(file);
                    case "sbml":
                    case "xml":
                        return validateSBMLFile(file, 3, 1).length() > 0;
                    case "wim":
                    case "txt":
                        return WimParser.validateFile(file);
                    case "db":
                        return DBDoc.validateFile(file);
                    default:
                        break;
                }
            } else {
                File schemeFile = parsingSchemePath.toFile();
                return validateCustomFile(file, schemeFile);
            }
        } else {
            System.err.println("Input file " + filePath.toString() + " does not exist.");
        }
        return false;
    }

    /**
     * Validates input file, against scheme if not null
     *
     * @param inputString       input file content as string
     * @param type              The input file format
     * @param parsingSchemePath not null for custom file formats,
     *                          file path of parsing scheme
     * @return true if file is valid
     */
    public boolean validateString(String inputString, String type, Path parsingSchemePath) {
        File file = new File("utilfiles/tmpFile_validateStr." + Misc.getFileEnding(type));

        if (Misc.writeContentToFile(inputString, file.getPath())) {
            return validateFile(file.toPath(), type, parsingSchemePath);
        } else {
            return false;
        }
    }

    /**
     * Validates input file, against scheme if not null
     *
     * @param file              The file to validate
     * @param parsingSchemePath not null for custom file formats,
     *                          file path of parsing scheme
     * @return true if file is valid
     */
    private boolean validateCustomFile(File file, File parsingSchemePath) {
        CustomParser customParser = new CustomParser(file, parsingSchemePath);

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(parsingSchemePath);
            Node rootNode = document.getChildNodes().item(0);
            String nodeMatchStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            customParser.recursion(rootNode, nodeMatchStr);
            customParser.getDataModel().buildCustomTrees("custom");
            customParser.getDataModel().initDescendants();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return false;
        }
        DataModel dataModel = customParser.getDataModel();
        boolean isInvalid = CustomParser.isIsInvalid();
        return (dataModel != null && !isInvalid);
    }

    /**
     * Validates custom parsing scheme
     *
     * @param parsingSchemePath The scheme file to validate
     * @return true if file is valid parsing scheme
     */
    public boolean validateParsingScheme(Path parsingSchemePath) {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        File schemaFile = new File("src/main/resources/utilFiles/fileParsingScheme.xsd");
        Schema schema = null;

        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        if (schema != null) {
            javax.xml.validation.Validator validator = schema.newValidator();
            Source source = null;
            try {
                source = new StreamSource(new StringReader(Misc.getContentOfFile(parsingSchemePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                try {
                    validator.validate(source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } catch (SAXException e) {
                System.err.println("File " + parsingSchemePath.toString() + " is not a valid parsing scheme. " +
                        "Please refer to the xml scheme src/main/resources/utilFiles/fileParsingScheme.xsd");
                return false;
            }
        } else {
            System.err.println("XSD file to parse against is not valid. " +
                    "Check if file src/main/resources/utilFiles/fileParsingScheme.xsd is missing.");
            return false;
        }
    }

    /**
     * Validates custom parsing scheme
     *
     * @param file    The SBML file to validate
     * @param level   The SBML file's level
     * @param version The SBML file's version
     * @return error log
     */
    public String validateSBMLFile(File file, int level, int version) {
        StringBuilder errorBuilder = new StringBuilder();
        SBMLDoc sbmlDoc = new SBMLDoc(file.toPath(), level, version);

        sbmlDoc.readIn();

        if (sbmlDoc.getDataModel().notEmpty()) {
            SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
            if (sbmlDocWriter.getSbmlDocument().getErrorLog().getValidationErrors().size() > 0) {
                for (SBMLError err : sbmlDocWriter.getSbmlDocument().getErrorLog().getValidationErrors()) {
                    errorBuilder.append(err.getMessage()).append("\n");
                }
            }
        }
        return errorBuilder.toString();
    }
}
