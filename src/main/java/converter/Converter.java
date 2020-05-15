package converter;

import io.Misc;
import io.documents.CRSDoc;
import io.documents.DBDoc;
import io.documents.SBMLDoc;
import io.documents.WimDoc;
import io.parser.CustomParser;
import io.writer.CRSWriter;
import io.writer.DBWriter;
import io.writer.SBMLDocWriter;
import io.writer.WimWriter;
import model.DataModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Converter {

    public DataModel lastDataModel;

    /**
     * Converts input file and writes result
     * to file.
     *
     * @param inputPath  The path of the file that
     *                   is converted. Its file ending defines
     *                   input format
     * @param outputPath The path of the file that
     *                   is (created and) written to.
     *                   The file ending defines the output
     *                   file type.
     */
    public void convert(Path inputPath, Path outputPath) {
        String inputPathStr = inputPath.toString();
        String fromType = inputPathStr.split("\\.")[inputPathStr.split("\\.").length - 1];
        String outputPathStr = outputPath.toString();
        String toType = outputPathStr.split("\\.")[outputPathStr.split("\\.").length - 1];
        lastDataModel = null;

        if (fromType.equals(toType)) {
            try {
                Misc.copyContentToFile(inputPath, outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switch (fromType) {
                case "crs":
                    CRSDoc crsDoc = new CRSDoc(inputPath);
                    crsDoc.readIn();
                    lastDataModel = crsDoc.getDataModel();
                    break;
                case "sbml":
                case "xml":
                    SBMLDoc sbmlDoc = new SBMLDoc(inputPath, 3, 1);
                    sbmlDoc.readIn();
                    lastDataModel = sbmlDoc.getDataModel();
                    break;
                case "wim":
                case "txt":
                    WimDoc wimDoc = new WimDoc(inputPath);
                    wimDoc.readIn();
                    lastDataModel = wimDoc.getDataModel();
                    break;
                case "db":
                    DBDoc dbDoc = new DBDoc(inputPath);
                    dbDoc.readIn();
                    lastDataModel = dbDoc.getDataModel();
                    break;
                default:
                    break;
            }

            if (lastDataModel != null) {
                switch (toType) {
                    case "crs":
                        CRSDoc crsDoc = new CRSDoc(lastDataModel);
                        CRSWriter crsWriter = new CRSWriter(crsDoc);
                        crsWriter.write(outputPathStr);
                        break;
                    case "sbml":
                    case "xml":
                        SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                        SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                        sbmlDocWriter.write(outputPathStr);
                        break;
                    case "wim":
                    case "txt":
                        WimDoc wimDoc = new WimDoc(lastDataModel);
                        WimWriter wimWriter = new WimWriter(wimDoc);
                        wimWriter.write(outputPathStr);
                        break;
                    case "db":
                        DBDoc dbDoc = new DBDoc(lastDataModel);
                        DBWriter dbWriter = new DBWriter(dbDoc);
                        dbWriter.write(outputPathStr);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputPath The path of the file that
     *                  is converted
     * @param fromType  file format of input file
     * @param toType    file format of output String
     * @return input file as string in output format
     */
    public String convertToString(Path inputPath, String fromType, String toType) {
        lastDataModel = null;

        if (fromType.equals(toType)) {
            try {
                return Misc.getContentOfFile(inputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switch (fromType) {
                case "crs":
                    CRSDoc crsDoc = new CRSDoc(inputPath);
                    crsDoc.readIn();
                    lastDataModel = crsDoc.getDataModel();
                    break;
                case "sbml":
                case "xml":
                    SBMLDoc sbmlDoc = new SBMLDoc(inputPath, 3, 1);
                    sbmlDoc.readIn();
                    lastDataModel = sbmlDoc.getDataModel();
                    break;
                case "wim":
                case "txt":
                    WimDoc wimDoc = new WimDoc(inputPath);
                    wimDoc.readIn();
                    lastDataModel = wimDoc.getDataModel();
                    break;
                case "db":
                    DBDoc dbDoc = new DBDoc(inputPath);
                    dbDoc.readIn();
                    lastDataModel = dbDoc.getDataModel();
                    break;
                default:
                    break;
            }

            if (lastDataModel != null) {
                switch (toType) {
                    case "crs":
                        CRSDoc crsDoc = new CRSDoc(lastDataModel);
                        CRSWriter crsWriter = new CRSWriter(crsDoc);
                        return crsWriter.buildString();
                    case "sbml":
                    case "xml":
                        SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                        SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                        sbmlDocWriter.buildSBMLDocument();
                        return sbmlDocWriter.getDocAsString();
                    case "wim":
                    case "txt":
                        WimDoc wimDoc = new WimDoc(lastDataModel);
                        WimWriter wimWriter = new WimWriter(wimDoc);
                        return wimWriter.buildString();
                    case "db":
                        DBDoc dbDoc = new DBDoc(lastDataModel);
                        DBWriter dbWriter = new DBWriter(dbDoc);
                        return dbWriter.getDBString();
                    default:
                        break;
                }
            }
        }

        return "";
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputStr The input file content as string
     * @param fromType file format of input file
     * @param toType   file format of output String
     * @return input file as string in output format
     */
    public String convertStringToString(String inputStr, String fromType, String toType) {
        lastDataModel = null;

        if (fromType.equals(toType)) {
            return inputStr;
        } else {
            switch (fromType) {
                case "crs":
                    CRSDoc crsDoc = new CRSDoc(inputStr);
                    crsDoc.readIn(inputStr);
                    lastDataModel = crsDoc.getDataModel();
                    break;
                case "sbml":
                case "xml":
                    SBMLDoc sbmlDoc = new SBMLDoc(inputStr, 2, 4);
                    sbmlDoc.readIn(inputStr);
                    lastDataModel = sbmlDoc.getDataModel();
                    break;
                case "wim":
                case "txt":
                    WimDoc wimDoc = new WimDoc(inputStr);
                    wimDoc.readIn(inputStr);
                    lastDataModel = wimDoc.getDataModel();
                default:
                    break;
            }

            if (lastDataModel != null) {
                switch (toType) {
                    case "crs":
                        CRSDoc crsDoc = new CRSDoc(lastDataModel);
                        CRSWriter crsWriter = new CRSWriter(crsDoc);
                        return crsWriter.buildString();
                    case "sbml":
                    case "xml":
                        SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                        SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                        sbmlDocWriter.buildSBMLDocument();
                        return sbmlDocWriter.getDocAsString();
                    case "wim":
                    case "txt":
                        WimDoc wimDoc = new WimDoc(lastDataModel);
                        WimWriter wimWriter = new WimWriter(wimDoc);
                        return wimWriter.buildString();
                    case "db":
                        DBDoc dbDoc = new DBDoc(lastDataModel);
                        DBWriter dbWriter = new DBWriter(dbDoc);
                        return dbWriter.getDBString();
                    default:
                        break;
                }
            }

            return "";
        }
    }

    /**
     * Converts input file and returns result String.
     *
     * @param dataModel The input model object
     * @param toType    file format of output String
     * @return model written as string in output format
     */
    public String convertDataModelToString(DataModel dataModel, String toType) {
        switch (toType) {
            case "crs":
                CRSDoc crsDoc = new CRSDoc(dataModel);
                CRSWriter crsWriter = new CRSWriter(crsDoc);
                return crsWriter.buildString();
            case "sbml":
            case "xml":
                SBMLDoc sbmlDoc = new SBMLDoc(dataModel);
                SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                sbmlDocWriter.buildSBMLDocument();
                return sbmlDocWriter.getDocAsString();
            case "wim":
            case "txt":
                WimDoc wimDoc = new WimDoc(dataModel);
                WimWriter wimWriter = new WimWriter(wimDoc);
                return wimWriter.buildString();
            case "db":
                DBDoc dbDoc = new DBDoc(dataModel);
                DBWriter dbWriter = new DBWriter(dbDoc);
                return dbWriter.getDBString();
            default:
                break;
        }

        return "";
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputPath  The path of the file that
     *                   is converted.
     * @param outputPath The path of the file that
     *                   is (created and) written to.
     *                   The file ending defines the output
     *                   file type.
     * @param schemeFile parsing scheme for custom input format
     */
    public void convertCustomFileToFile(Path inputPath, Path outputPath, File schemeFile) {
        String outputPathStr = outputPath.toString();
        String toType = outputPathStr.split("\\.")[outputPathStr.split("\\.").length - 1];
        CustomParser customParser = new CustomParser(inputPath.toFile(), schemeFile);
        lastDataModel = customParser.getDataModel();

        switch (toType) {
            case "crs":
                CRSDoc crsDoc = new CRSDoc(lastDataModel);
                CRSWriter crsWriter = new CRSWriter(crsDoc);
                crsWriter.write(outputPathStr);
                break;
            case "sbml":
            case "xml":
                SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                sbmlDocWriter.write(outputPathStr);
                break;
            case "wim":
            case "txt":
                WimDoc wimDoc = new WimDoc(lastDataModel);
                WimWriter wimWriter = new WimWriter(wimDoc);
                wimWriter.write(outputPathStr);
                break;
            case "db":
                DBDoc dbDoc = new DBDoc(lastDataModel);
                DBWriter dbWriter = new DBWriter(dbDoc);
                dbWriter.write(outputPathStr);
                break;
            default:
                break;
        }
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputStr   The input file content as string
     * @param outputPath The path of the file that
     *                   is (created and) written to.
     *                   The file ending defines the output
     *                   file type.
     * @param schemeFile parsing scheme for custom input format
     */
    public void convertCustomStringToFile(String inputStr, Path outputPath, File schemeFile) {
        String outputPathStr = outputPath.toString();
        String toType = outputPathStr.split("\\.")[outputPathStr.split("\\.").length - 1];
        CustomParser customParser = new CustomParser(inputStr, schemeFile);
        lastDataModel = customParser.getDataModel();

        switch (toType) {
            case "crs":
                CRSDoc crsDoc = new CRSDoc(lastDataModel);
                CRSWriter crsWriter = new CRSWriter(crsDoc);
                crsWriter.write(outputPathStr);
                break;
            case "sbml":
            case "xml":
                SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                sbmlDocWriter.write(outputPathStr);
                break;
            case "wim":
            case "txt":
                WimDoc wimDoc = new WimDoc(lastDataModel);
                WimWriter wimWriter = new WimWriter(wimDoc);
                wimWriter.write(outputPathStr);
                break;
            case "db":
                DBDoc dbDoc = new DBDoc(lastDataModel);
                DBWriter dbWriter = new DBWriter(dbDoc);
                dbWriter.write(outputPathStr);
                break;
            default:
                break;
        }
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputStr   The input file content as string
     * @param schemeFile parsing scheme for custom input format
     * @param toType     file format of output String
     * @return input String as string in output format
     */
    public String convertCustomStringToString(String inputStr, File schemeFile, String toType) {
        CustomParser customParser = new CustomParser(inputStr, schemeFile);
        lastDataModel = customParser.getDataModel();

        switch (toType) {
            case "crs":
                CRSDoc crsDoc = new CRSDoc(lastDataModel);
                CRSWriter crsWriter = new CRSWriter(crsDoc);
                return crsWriter.buildString();
            case "sbml":
            case "xml":
                SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel);
                SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
                sbmlDocWriter.buildSBMLDocument();
                return sbmlDocWriter.getDocAsString();
            case "wim":
            case "txt":
                WimDoc wimDoc = new WimDoc(lastDataModel);
                WimWriter wimWriter = new WimWriter(wimDoc);
                return wimWriter.buildString();
            case "db":
                DBDoc dbDoc = new DBDoc(lastDataModel);
                DBWriter dbWriter = new DBWriter(dbDoc);
                return dbWriter.getDBString();
            default:
                break;
        }

        return "";
    }

    /**
     * Converts input file and returns result String.
     *
     * @param inputPath      The path of the file that
     *                       is converted. Its file ending defines
     *                       input format
     * @param outputPath     The path of the file that
     *                       is (created and) written to.
     * @param schemeFilePath parsing scheme file for custom input format
     * @param level          SBML level of output file
     * @param version        SBML version of output file
     */
    public void convertToSBML(Path inputPath, Path outputPath, Path schemeFilePath, int level, int version) {
        String inputPathStr = inputPath.toString();
        String fromType = inputPathStr.split("\\.")[inputPathStr.split("\\.").length - 1];
        String outputPathStr = outputPath.toString();
        boolean convert = true;

        if (schemeFilePath != null) {
            File file = schemeFilePath.toFile();
            if (file.exists()) {
                CustomParser customParser = new CustomParser(inputPath.toFile(), schemeFilePath.toFile());
                lastDataModel = customParser.getDataModel();
            }
        } else {
            if (fromType.matches("sbml|xml")) {
                try {
                    Misc.copyContentToFile(inputPath, outputPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                convert = false;
            } else {
                lastDataModel = null;
                switch (fromType) {
                    case "crs":
                        CRSDoc crsDoc = new CRSDoc(inputPath);
                        crsDoc.readIn();
                        lastDataModel = crsDoc.getDataModel();
                        break;
                    case "sbml":
                    case "xml":
                        SBMLDoc sbmlDoc = new SBMLDoc(inputPath, 3, 1);
                        sbmlDoc.readIn();
                        lastDataModel = sbmlDoc.getDataModel();
                        break;
                    case "wim":
                    case "txt":
                        WimDoc wimDoc = new WimDoc(inputPath);
                        wimDoc.readIn();
                        lastDataModel = wimDoc.getDataModel();
                        break;
                    case "db":
                        DBDoc dbDoc = new DBDoc(inputPath);
                        dbDoc.readIn();
                        lastDataModel = dbDoc.getDataModel();
                        break;
                    default:
                        break;
                }
            }
        }

        if (convert && lastDataModel != null) {
            SBMLDoc sbmlDoc = new SBMLDoc(lastDataModel, level, version);
            SBMLDocWriter sbmlDocWriter = new SBMLDocWriter(sbmlDoc);
            sbmlDocWriter.write(outputPathStr);
        }
    }
}
