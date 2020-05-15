package io.parser;

import io.Patterns;
import io.documents.CRSDoc;
import logic.ReactionTreeBuilder;
import model.DataModel;
import model.Reaction;
import model.Species;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CRSParser extends Parser {

    private CRSDoc crsDocument;

    /**
     * Creates CRS Parser
     *
     * @param doc CRS document that is build by this
     */
    public CRSParser(CRSDoc doc) {
        super(doc);

        this.crsDocument = doc;
    }

    /**
     * Validates if file complies to CRS criteria
     *
     * @param file Input file
     */
    public static boolean validateFile(File file) {
        CRSDoc crsDoc = new CRSDoc(file.toPath());
        crsDoc.readIn();
        DataModel dataModel = crsDoc.getDataModel();

        return dataModel.satisfiesCRScriteria();
    }


    private static String formatStr(String str) {
        str = str.replaceAll("([+*])", "&");
        str = str.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\{", "").replaceAll("\\}", "");
        return str;
    }

    /**
     * Parses input line
     *
     * @param line Input line
     */
    public void parseLine(String line) {
        String lineStrip = line.replaceAll("\\s+", "");

        if (line.startsWith("#")) {
            parseComment(line);
        } else if (line.matches(Patterns.crsFoodSetPattern) && !(line.matches(Patterns.crsReactionPattern))) {
            parseFoodSet(line);
        } else if (lineStrip.matches(Patterns.crsReactionPattern)) {
            parseReactionLine(lineStrip);
        }
    }

    /**
     * Parses reaction line
     *
     * @param line Input line
     */
    private void parseReactionLine(String line) {
        String reactionId = line.split(":")[0];//m.group(1);
        String leftSplit = line.split(":")[1].split("<?[-=]>")[0];
        String rightSplit = line.split(":")[1].split("<?[-=]>")[1];
        Reaction reaction = new Reaction(reactionId);
        String reactantsStr = "";
        String catStr = "noCata";
        String inhStr = "noInhib";

        if (leftSplit.contains("[")) {
            reactantsStr = formatStr(leftSplit.split("\\[")[0]);
        } else {
            reactantsStr = formatStr(leftSplit.split("\\{")[0]);
        }

        if (leftSplit.contains("[")) {
            catStr = formatStr(leftSplit.split("\\[")[1].split("\\]")[0]);
        }

        if (leftSplit.contains("{")) {
            inhStr = formatStr(leftSplit.split("\\{")[1].split("\\}")[0]);
        }

        if ((line.contains("<->")) || line.contains("<=>")) {
            reaction.setReversible(true);
        }

        //System.out.println("IS REVERSIBLE: " + reaction.isReversible());

        String productsStr = formatStr(rightSplit);

        ReactionTreeBuilder reactionTreeBuilder =
                new ReactionTreeBuilder("crs", reaction, reactantsStr, catStr + "\t" + inhStr, productsStr,
                        new ArrayList<>(Arrays.asList("reactants::" + reactantsStr, "products::" + productsStr,
                                "catalysts::" + catStr, "inhibitors::" + inhStr)));
        reactionTreeBuilder.buildTrees();

        crsDocument.getReactionSet().addReaction(reaction);
    }

    /**
     * Parses food set line
     *
     * @param line Input line
     */
    private void parseFoodSet(String line) {
        line = line.split(":")[1].strip().replaceAll("\\s+|\\t+", ",");

        for (String s : line.split(",")) {
            if (!s.isBlank()) {
                crsDocument.getSpeciesSet().addSpecies(s);
                Species sp = crsDocument.getSpeciesSet().getIdToSpeciesMap().get(s);
                if (sp.getInitConcentration() + sp.getInitAmount() <= 0) {
                    sp.setInitAmount(1.0);
                }
                crsDocument.getSpeciesFoodSet().addFood(crsDocument.getSpeciesSet().getIdToSpeciesMap().get(s));
            }
        }
    }

    /**
     * Parses comment line
     *
     * @param line Input line
     */
    private void parseComment(String line) {
        crsDocument.getComments().add(line.replaceAll("#", ""));
    }

}
