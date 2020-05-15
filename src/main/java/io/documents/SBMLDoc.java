package io.documents;

import io.SBOTerms;
import logic.ReactionTreeBuilder;
import model.DataModel;
import org.apache.commons.io.IOUtils;
import org.sbml.jsbml.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SBMLDoc extends Doc {

    SBMLDocument sbmlDocument = new SBMLDocument();
    private int sbmlLevel = 3;
    private int sbmlVersion = 2;
    private String modelName = "";

    /**
     * Creates SBML document representation
     *
     * @param path        path of input file
     * @param sbmlLevel   level of SBML document
     * @param sbmlVersion version of SBML document
     */
    public SBMLDoc(Path path, int sbmlLevel, int sbmlVersion) {
        super(path);

        this.sbmlLevel = sbmlLevel;
        this.sbmlVersion = sbmlVersion;
    }

    /**
     * Creates SBML document representation from string
     *
     * @param inputStr    input string
     * @param sbmlLevel   level of SBML document
     * @param sbmlVersion version of SBML document
     */
    public SBMLDoc(String inputStr, int sbmlLevel, int sbmlVersion) {
        super(inputStr);

        this.sbmlLevel = sbmlLevel;
        this.sbmlVersion = sbmlVersion;
    }

    public SBMLDoc(DataModel dataModel) {
        super(dataModel);
    }

    public SBMLDoc(DataModel dataModel, int sbmlLevel, int sbmlVersion) {
        super(dataModel);

        this.sbmlLevel = sbmlLevel;
        this.sbmlVersion = sbmlVersion;
    }

    /**
     * Reads in SBML document with JSBML API
     */
    public void readIn() {
        SBMLReader reader = new SBMLReader();

        if (this.getInputStr().length() > 0) {
            try {
                sbmlDocument = reader.readSBMLFromStream(IOUtils.toInputStream(this.getInputStr(), Charset.defaultCharset()));
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        } else {
            try {
                sbmlDocument = reader.readSBML(getPath().toFile());
            } catch (XMLStreamException | IOException | ClassCastException e) {
                e.printStackTrace();
            }
        }

        setDataModel(new DataModel(this));
        getDataModel().initDescendants();
        getData();
    }

    /**
     * Reads in SBML document with JSBML API
     *
     * @param inputStr input string in SBML format
     */
    public void readIn(String inputStr) {
        SBMLReader reader = new SBMLReader();

        if (inputStr.length() > 0) {
            this.setInputStr(inputStr);
            try {
                sbmlDocument = reader.readSBMLFromStream(IOUtils.toInputStream(inputStr, Charset.defaultCharset()));
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }

            setDataModel(new DataModel(this));
            getDataModel().initDescendants();
            getData();
        }
    }

    /**
     * Builds SBMLDoc instance from input
     */
    private void getData() {
        if (sbmlDocument != null) {
            if (sbmlDocument.getModel() != null) {
                for (Species species : sbmlDocument.getModel().getListOfSpecies()) {
                    model.Species s = new model.Species(species.getId());

                    s.init(species);
                    s.addSpeciesName(species.getName());
                    addSpecies(s);

                    if (species.getInitialAmount() + species.getInitialConcentration() > 0.0) {
                        addFood(s);
                    }

                    s.setInitAmount(species.getInitialAmount());
                    s.setInitConcentration(species.getInitialConcentration());
                }

                for (Reaction sbmlReaction : sbmlDocument.getModel().getListOfReactions()) {
                    model.Reaction r = new model.Reaction(sbmlReaction.getId());

                    r.setSbmlReactionInstance(sbmlReaction);
                    r.setReversible(sbmlReaction.isReversible());

                    StringBuilder reactantsListWithCoeff = new StringBuilder("reactants::");
                    StringBuilder productsListWithCoeff = new StringBuilder("products::");
                    StringBuilder catalystsListWithCoeff = new StringBuilder("catalysts::");
                    StringBuilder inhibitorsListWithCoeff = new StringBuilder("inhibitors::");

                    for (SpeciesReference speciesReference : sbmlReaction.getListOfReactants()) {
                        if (getSpeciesSet().getIdToSpeciesMap().containsKey(speciesReference.getSpeciesInstance().getId())) {
                            r.getReactantsList().add(getSpeciesSet().getIdToSpeciesMap().get(speciesReference.getSpeciesInstance().getId()));
                            if (!reactantsListWithCoeff.toString().equals("reactants::")) {
                                reactantsListWithCoeff.append("&");
                            }
                            reactantsListWithCoeff.append("%%").append(speciesReference.getStoichiometry()).append("%coeffOf%").append(speciesReference.getSpeciesInstance().getId());
                        }
                    }

                    for (SpeciesReference speciesReference : sbmlReaction.getListOfProducts()) {
                        if (getSpeciesSet().getIdToSpeciesMap().containsKey(speciesReference.getSpeciesInstance().getId())) {
                            r.getProductsList().add(getSpeciesSet().getIdToSpeciesMap().get(speciesReference.getSpeciesInstance().getId()));
                            if (!productsListWithCoeff.toString().equals("products::")) {
                                productsListWithCoeff.append("&");
                            }
                            productsListWithCoeff.append("%%").append(speciesReference.getStoichiometry()).append("%coeffOf%").append(speciesReference.getSpeciesInstance().getId());
                        }
                    }

                    for (ModifierSpeciesReference speciesReference : sbmlReaction.getListOfModifiers()) {
                        if (getSpeciesSet().getIdToSpeciesMap().containsKey(speciesReference.getSpeciesInstance().getId())) {
                            model.Species species = getSpeciesSet().getIdToSpeciesMap().get(speciesReference.getSpeciesInstance().getId());
                            double coefficient = 1.0;
                            String coeffStr = "";

                            try {
                                if (speciesReference.getNotesString().length() > 0 && speciesReference.getNotesString().contains("MODIFIER_STOICHIOMETRY: ")) {
                                    coefficient = Double.parseDouble(speciesReference.getNotesString().split("MODIFIER_STOICHIOMETRY: ")[1].split("</p>|&lt")[0]);
                                }
                            } catch (XMLStreamException e) {
                                e.printStackTrace();
                            }

                            coeffStr += "%%" + coefficient + "%coeffOf%" + speciesReference.getSpeciesInstance().getId();

                            if (speciesReference.getSBOTermID().equals(SBOTerms.getDefaultSBOInhibitor())) {
                                if (!coeffStr.equals("inhibitors::")) {
                                    coeffStr += ",";
                                }
                                r.getInhibitorsList().add(species);
                                inhibitorsListWithCoeff.append(coeffStr);
                            } else {
                                if (!coeffStr.equals("catalysts::")) {
                                    coeffStr += ",";
                                }
                                r.getCatalystsList().add(species);
                                catalystsListWithCoeff.append(coeffStr);
                            }
                        }
                    }

                    createTree(r, new ArrayList<String>(Arrays.asList(reactantsListWithCoeff.toString(),
                            productsListWithCoeff.toString(), catalystsListWithCoeff.toString(),
                            inhibitorsListWithCoeff.toString())));
                    r.setSboTerm(sbmlReaction.getSBOTermID());
                    r.setReactionName(sbmlReaction.getName());
                    r.setMetaId(sbmlReaction.getMetaId());

                    try {
                        r.setNote(sbmlReaction.getNotesString());
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }

                    getReactionSet().addReaction(r);
                }
            }
        }
    }

    /**
     * Creates tree representations of reactions' boolean equations
     *
     * @param reaction                 The input reaction
     * @param equationWithCoefficients The respective boolean equation in format including coefficients
     */
    private void createTree(model.Reaction reaction, ArrayList<String> equationWithCoefficients) {
        String catStr = "noCata";
        String inhStr = "noInhib";

        if (toDNF(reaction.getCatalystsList()).length() > 0) {
            catStr = toDNF(reaction.getCatalystsList());
        }
        if (toDNF(reaction.getInhibitorsList()).length() > 0) {
            inhStr = toDNF(reaction.getInhibitorsList());
        }

        String modifierStr = catStr + "\t" + inhStr;

        ReactionTreeBuilder reactionTreeBuilder
                = new ReactionTreeBuilder("sbml", reaction,
                toDNF(reaction.getReactantsList()), modifierStr, toDNF(reaction.getProductsList()), equationWithCoefficients);
        reactionTreeBuilder.buildTrees();
    }

    /**
     * Creates DNF from species' identifiers
     *
     * @param speciesSet Set containing Species instances
     */
    private String toDNF(HashSet<model.Species> speciesSet) {
        HashSet<String> set = new HashSet();
        for (model.Species s : speciesSet) {
            set.add(s.getSpeciesId());
        }
        return String.join(",", set);
    }

    public int getSbmlLevel() {
        return sbmlLevel;
    }

    public int getSbmlVersion() {
        return sbmlVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

}
