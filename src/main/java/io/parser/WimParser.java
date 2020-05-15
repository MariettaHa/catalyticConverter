package io.parser;

import io.documents.Doc;
import io.documents.WimDoc;
import logic.ReactionTreeBuilder;
import model.DataModel;
import model.Reaction;
import model.Species;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class WimParser extends Parser {

    private WimDoc wimDocument;
    private int paragraphType = 0; //0: meta-data; 1: molecules; 2: food set; 3: reactions
    private Map<String, Integer> paragraphMap = Map.of(
            "<meta-data>", 1, "<molecules>", 2, "<food set>", 3, "<reactions>", 4);

    /**
     * Creates WIM Parser
     *
     * @param doc WIM document that is build by this
     */
    public WimParser(Doc doc) {
        super(doc);

        this.wimDocument = (WimDoc) doc;
        this.wimDocument.setParser(this);
    }

    /**
     * Validates if file complies to WIM criteria
     *
     * @param file Input file
     */
    public static boolean validateFile(File file) {
        WimDoc wimDoc = new WimDoc(file.toPath());
        wimDoc.readIn();
        DataModel dataModel = wimDoc.getDataModel();

        return dataModel.satisfiesWIMcriteria();
    }

    /**
     * Parses input line
     *
     * @param line Input line
     */
    @Override
    public void parseLine(String line) {
        if (paragraphMap.containsKey(line)) {
            paragraphType = paragraphMap.get(line);
        } else {
            if (!line.isBlank()) {
                switch (paragraphType) {
                    case 1:
                        parseMetaData(line);
                        break;
                    case 2:
                        parseMolLine(line);
                        break;
                    case 3:
                        parseFoodSet(line);
                        break;
                    case 4:
                        parseReactionLine(line);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Parses reaction line
     *
     * @param line Input line
     */
    private void parseReactionLine(String line) {
        try {
            String[] info = line.split("\\t");

            if (info.length == 4) {
                ArrayList<String> stringsWithCoefficients = new ArrayList<>();
                Reaction reaction = new Reaction(info[0]);
                reaction.setWeight(Double.parseDouble(info[3]));
                String reactantsStr = formatStr(info[1].split(" <?=> ")[0]);
                reaction.setReversible(info[1].contains("<=>"));
                String productsStr = formatStr(info[1].split("=> ")[1]);
                String modifiersStr = formatStr(info[2]);
                stringsWithCoefficients.add(formatStrWithCoeff("reactants::" + info[1].split(" <?=> ")[0])); //add reactants with coefficients (idx0)
                stringsWithCoefficients.add(formatStrWithCoeff("products::" + info[1].split("=> ")[1])); //add products with coefficients (idx1)

                String catStr = "noCata";
                String[] splitMod = modifiersStr.split("\\{");

                String inhibStr = "noInhib";

                if (splitMod.length > 1) {
                    catStr = splitMod[0].strip();
                    inhibStr = splitMod[1].replaceAll("\\}|\\{", "");
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + info[2].split("\\{")[0].strip())); //add catalysts with coefficients
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + info[2].split("\\{")[1].replaceAll("\\}|\\{", ""))); //add catalysts with coefficients
                } else {
                    if (modifiersStr.contains("{")) {
                        inhibStr = modifiersStr.strip().replaceAll("\\}|\\{", "");
                        stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + info[2].split("\\{")[1].replaceAll("\\}|\\{", ""))); //add catalysts with coefficients
                    } else if (modifiersStr.length() > 0) {
                        stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + info[2].split("\\{")[0].strip())); //add catalysts with coefficients
                        catStr = modifiersStr.strip();
                    }
                }

                ReactionTreeBuilder reactionTreeBuilder
                        = new ReactionTreeBuilder("wim", reaction, reactantsStr, catStr + "\t" + inhibStr, productsStr, stringsWithCoefficients);
                reactionTreeBuilder.buildTrees();

                wimDocument.addReaction(reaction);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();

        }

    }

    /**
     * Parses line containing food molecule
     *
     * @param line Input line
     */
    private void parseFoodSet(String line) {
        String[] info = line.split("\\t");

        if (!wimDocument.getSpeciesSet().getIdToSpeciesMap().containsKey(info[1])) {
            wimDocument.addSpecies(info[1]);
            Species s = wimDocument.getSpeciesSet().getIdToSpeciesMap().get(info[1]);
            s.addSpeciesNames(info[0].split("; "));
        }

        Species s = wimDocument.getSpeciesSet().getIdToSpeciesMap().get(info[1]);
        wimDocument.addFood(s);
    }

    /**
     * Parses line containing molecule
     *
     * @param line Input line
     */
    private void parseMolLine(String line) {
        String[] info = line.split("\\t");

        if (info.length == 2) {
            Species species = new Species(info[1]);
            species.addSpeciesNames(info[0].split("; "));
            wimDocument.addSpecies(species);
        }
    }

    /**
     * Parses line containing meta data
     *
     * @param line Input line
     */
    private void parseMetaData(String line) {
        wimDocument.getComments().add(line);

        if (line.matches("nrMolecules\\s+=\\s+(\\d+)")) {
            wimDocument.setMolCount(Integer.parseInt(line.split("= ")[1]));
        } else if (line.matches("nrFoodSet\\s+=\\s+(\\d+)")) {
            wimDocument.setFoodCount(Integer.parseInt(line.split("= ")[1]));
        } else if (line.matches("nrReactions\\s+=\\s+(\\d+)")) {
            wimDocument.setReactionCount(Integer.parseInt(line.split("= ")[1]));
        }
    }

    /**
     * Formats str as valid DNF string
     *
     * @param str Input String
     * @return formatted DNF string
     */
    private String formatStr(String str) {
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("(^|\\s|&|,|\\(|\\[|\\{)(\\d+[.]\\d+|\\d+)($|\\s)", "$1");
        str = str.replaceAll("\\s&\\s", "&").replaceAll("\\s,\\s", ",");

        return str.strip();
    }

    /**
     * Formats str as valid DNF string including formatted coefficients
     *
     * @param str Input String
     * @return formatted DNF string with coefficients
     */
    private String formatStrWithCoeff(String str) {
        str = str.replaceAll("\\s\\s+", " ");
        str = str.replaceAll("([^A-Za-z0-9-_/().'%]|^)(\\d+[.]\\d+|\\d+)(\\s+)([A-Za-z-_/().'%][A-Za-z0-9-_/().'%]*)([^A-Za-z0-9-_/().'%]|$)", "$1%%$2%coeffOf%$4$5");
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("\\s\\s+", " ");
        str = str.replaceAll("\\s*&\\s*", "&").replaceAll("\\s,\\s", ",");

        return str;
    }


}
