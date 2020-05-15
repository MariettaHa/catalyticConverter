package io.writer;

import io.documents.CRSDoc;
import model.FoodSet;
import model.Reaction;
import model.Species;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CRSWriter {

    private CRSDoc crsDoc;
    private String crsString;

    /**
     * Creates writer for CRS document
     *
     * @param crsDoc CRS document to write
     */
    public CRSWriter(CRSDoc crsDoc) {
        this.crsDoc = crsDoc;
    }

    /**
     * writes CRS document to file
     *
     * @param outputPath path String into which is written
     */
    public void write(String outputPath) {
        if (crsDoc.getDataModel().satisfiesCRScriteria()) {
            crsString = buildString();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(outputPath));
                crsDoc.setPath(new File(outputPath).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.write(crsString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Input does not satisfy criteria for CRS format (min. 1 food item, min." +
                    " 1 reaction item with min. 1 reactant and min. 1 product");
        }
    }

    /**
     * creation of output string
     */
    public String buildString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : crsDoc.getComments()) {
            stringBuilder.append("# " + s + "\n");
        }

        if (crsDoc.getComments().size() > 0) {
            stringBuilder.append("\n");
        }

        for (Reaction r : crsDoc.getReactionSet().getSortedList()) {
            stringBuilder.append(r.getReactionId() + ": ");
            stringBuilder.append(dnfToCRS(r.getReactantsTree().getDnf(), "+") + " ");
            if (r.getCatalystsTree().getDnf().length() > 0) {
                stringBuilder.append("[" + dnfToCRS(r.getCatalystsTree().getDnf(), "*") + "] ");
            }

            if (r.getInhibitorsTree().getDnf().length() > 0) {
                stringBuilder.append("{" + dnfToCRS(r.getInhibitorsTree().getDnf(), "*") + "} ");
            }
            stringBuilder.append("<".repeat((r.isReversible()) ? 1 : 0) + "-> ");
            stringBuilder.append(dnfToCRS(r.getProductsTree().getDnf(), "+") + "\n");
        }

        if (crsDoc.getSpeciesFoodSet().size() > 0) {
            stringBuilder.append("\n");
        }

        stringBuilder.append("Food: ");
        int c = 1;
        FoodSet<Species> foods = crsDoc.getSpeciesFoodSet();
        for (Species s : foods.getSortedSet()) {
            stringBuilder.append(s.getSpeciesId());
            if (c < foods.size()) {
                stringBuilder.append(",");
            }
            c++;
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    String dnfToCRS(String dnf, String and) {
        return dnf.strip().replaceAll("\\s?&\\s?", and);
    }

    public String getCrsString() {
        return crsString;
    }
}
