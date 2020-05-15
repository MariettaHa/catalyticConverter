package io.writer;

import io.SBOTerms;
import io.documents.SBMLDoc;
import org.sbml.jsbml.*;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;

import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;
import java.beans.PropertyChangeEvent;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SBMLDocWriter implements TreeNodeChangeListener {
    private SBMLDoc sbmlDoc;
    private SBMLDocument sbmlDocument;
    private String modelName = "defaultName";
    private String modelDescription = "";
    private String modelType = "";
    private String modelMetaData = "";
    private String authorGivenName = "";
    private String authorFamilyName = "";
    private String authorOrganization = "";
    private String authorEmail = "";

    private boolean correctInvalidStrings = true;

    private int level = 3;
    private int version = 1;

    private HashSet<String> speciesIds = new HashSet<>();
    private HashSet<String> speciesMeta = new HashSet<>();
    private HashSet<String> reactionIds = new HashSet<>();
    private HashSet<String> reactionMeta = new HashSet<>();

    /**
     * Creates writer for SBML document
     *
     * @param sbmlDoc SBML document to write
     */
    public SBMLDocWriter(SBMLDoc sbmlDoc) {
        this.sbmlDoc = sbmlDoc;
        buildSBMLDocument();
    }

    /**
     * writes SBML document to file
     *
     * @param outputPath path String into which is written
     */
    public void write(String outputPath) {
        SBMLWriter writer = new SBMLWriter();
        try {
            writer.writeSBMLToFile(sbmlDocument, outputPath);
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * builds SBML document with JSBML API
     */
    public void buildSBMLDocument() {
        level = sbmlDoc.getSbmlLevel();
        version = sbmlDoc.getSbmlVersion();
        sbmlDocument = new SBMLDocument(sbmlDoc.getSbmlLevel(), sbmlDoc.getSbmlVersion());
        sbmlDocument.addTreeNodeChangeListener(this);
        Model model = sbmlDocument.createModel();

        if (modelDescription.length() + modelType.length() + modelMetaData.length() > 0) {
            model.setName(modelName);

            try {
                StringBuilder noteSb = new StringBuilder();
                model.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n");

                if (!modelDescription.isBlank()) {
                    noteSb.append("    <p>Model Description: ").append(modelDescription).append("</p>\n");
                }
                if (!modelDescription.isBlank()) {
                    noteSb.append("    <p>Model Type: ").append(modelType).append("</p>\n");
                }
                if (!modelDescription.isBlank()) {
                    noteSb.append("    <p>Meta Data: ").append(modelMetaData).append("</p>\n");
                }

                noteSb.append("  </body>");
                model.setNotes(noteSb.toString());
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        History hist = model.getHistory();
        Creator creator = new Creator(authorGivenName, authorFamilyName, authorOrganization, authorEmail);
        hist.addCreator(creator);

        for (model.Species s : sbmlDoc.getSpeciesSet().getSortedSet()) {
            s.setSpeciesId(correctName(s.getSpeciesId()));
            s.setMetaId(correctName(s.getMetaId()));
            speciesIds.add(s.getSpeciesId());
            Species species = new Species(s.getSpeciesId());
            species.setLevel(sbmlDocument.getLevel());
            species.setVersion(sbmlDocument.getVersion());
            species.setName(correctName(s.getSpeciesNames().stream().collect(Collectors.joining("_"))));

            if (s.getInitAmount() == 0.0 && s.getInitConcentration() > 0.0) {
                species.setInitialConcentration(s.getInitConcentration());
            } else {
                species.setInitialAmount(s.getInitAmount());
            }

            if (!model.containsCompartment(correctName(s.getCompartment()))) {
                Compartment compartment = model.createCompartment(correctName(s.getCompartment()));
                compartment.setSpatialDimensions(3.0);
                compartment.setSize(1.0);
                if (level > 2) {
                    compartment.setConstant(false);
                }
            }
            species.setCompartment(correctName(s.getCompartment()));
            species.setHasOnlySubstanceUnits(s.isHasOnlySubstanceUnits());
            species.setBoundaryCondition(s.isBoundaryCondition());

            if (level > 2) {
                species.setConstant(s.isConstant());
            }
            speciesMeta.add(s.getMetaId());
            if (level > 2) {
                species.setSBOTerm(correctSBO(String.valueOf(s.getSboTerm())));
            }

            species.setName(String.join("_", s.getSpeciesNames()).replaceAll(" ", "_"));
            species.setSubstanceUnits(s.getSubstanceUnits());

            if (level > 2) {
                species.setConversionFactor(s.getConversionFactor());
            }

            if (model.containsSpecies(s.getSpeciesId())) {
                System.err.println("Duplicate species with id = " + s.getSpeciesId() + " ignored.");
            } else {
                if (model.containsSpecies(s.getMetaId())) {
                    System.err.println("Duplicate species with metaId = " + s.getMetaId() + " ignored.");
                } else {
                    model.addSpecies(species);
                }
            }
        }

        for (model.Reaction r : sbmlDoc.getReactionSet().getSortedList()) {
            r.setReactionId(correctName(r.getReactionId()));
            r.setMetaId(correctName(r.getMetaId()));

            if (model.containsReaction(r.getReactionId())) {
                System.err.println("Duplicate reaction with id + " + r.getReactionId() + " ignored.");
            } else {
                reactionIds.add(r.getReactionId());
                Reaction reaction = model.createReaction(r.getReactionId());
                //System.out.println(r.createFormulaHTMLStyle());

                try {
                    reaction.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                            "    <p>FORMULA: " + r.createFormulaHTMLStyle() + "</p>\n" +
                            "    <p>FORMULA MIGHT CONTAIN REPAIRED IDs - DUPLICATES POSSIBLE - SEE LOG.</p>\n" +
                            "  </body>");
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }

                reaction.setLevel(sbmlDocument.getLevel());
                reaction.setVersion(sbmlDocument.getVersion());
                reaction.setName(correctName(r.getReactionName()));

                if (level > 2) {
                    reaction.setSBOTerm(correctSBO(r.getSboTerm()));
                }

                reactionMeta.add(r.getMetaId());

                if (!model.containsCompartment(correctName(r.getCompartment()))) {
                    Compartment compartment = model.createCompartment(correctName(r.getCompartment()));
                    compartment.setSpatialDimensions(3.0);
                    compartment.setSize(1.0);
                    if (level > 2) {
                        compartment.setConstant(false);
                    }
                }
                if (level > 2) {
                    reaction.setCompartment(correctName(r.getCompartment()));
                }
                for (String string : r.getReactantsTree().getDnfWithCoeff().split("[&,]")) {
                    if (string.length() > 0) {
                        String[] split = string.split("%coeffOf%");
                        double coeff = 1.0;
                        String id = correctName(split[0]);

                        if (split.length > 1) {
                            coeff = correctDouble(split[0].replaceAll("%%", ""));
                            id = correctName(split[1]);
                        }

                        if (!model.containsSpecies(id)) {
                            System.err.println("Reaction specifies unlisted reactant species - adding this species. (" + id + ")");
                            Species species = model.createSpecies(id);
                            species.setCompartment(r.getCompartment());
                            species.setMetaId(species.getId() + model.getSpeciesCount());
                        }

                        String refId = id + "_reactantOf_" + reaction.getId();

                        if (model.findSpeciesReference(refId) == null) {
                            SpeciesReference ref = model.createReactant();
                            ref.setSpecies(id);
                            if (level > 2) {
                                ref.setConstant(false);
                            }
                            ref.setId(refId);
                            try {
                                ref.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                        "    <p>REACTANT_STOICHIOMETRY: " + coeff + "</p>\n" +
                                        "  </body>");
                            } catch (XMLStreamException e) {
                                e.printStackTrace();
                            }
                            ref.setStoichiometry(coeff);
                            if (level > 2) {
                                ref.setSBOTerm(SBOTerms.getDefaultSBOReactant());
                            }
                        } else {
                            reaction.addReactant(model.findSpeciesReference(refId));
                        }
                    }
                }

                for (String string : r.getProductsTree().getDnfWithCoeff().split("[&,]")) {
                    if (string.length() > 0) {
                        String[] split = string.split("%coeffOf%");
                        double coeff = 1.0;
                        String id = correctName(split[0]);

                        if (split.length > 1) {
                            coeff = correctDouble(split[0].replaceAll("%%", ""));
                            id = correctName(split[1]);
                        }

                        if (!model.containsSpecies(id)) {
                            System.err.println("Reaction specifies unlisted product species - adding this species. (" + id + ")");
                            Species species = model.createSpecies(id);
                            species.setCompartment(r.getCompartment());
                            species.setMetaId(species.getId() + model.getSpeciesCount());
                        }

                        String refId = id + "_productOf_" + reaction.getId();

                        if (model.findSpeciesReference(refId) == null) {
                            SpeciesReference ref = model.createProduct();
                            ref.setSpecies(id);
                            if (level > 2) {
                                ref.setConstant(false);
                            }
                            ref.setId(refId);
                            try {
                                ref.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                        "    <p>PRODUCT_STOICHIOMETRY: " + coeff + "</p>\n" +
                                        "  </body>");
                            } catch (XMLStreamException e) {
                                e.printStackTrace();
                            }
                            ref.setStoichiometry(coeff);
                            if (level > 2) {
                                ref.setSBOTerm(SBOTerms.getDefaultSBOProduct());
                            }
                        } else {
                            reaction.addProduct(model.findSpeciesReference(refId));
                        }
                    }
                }

                for (String string : r.getCatalystsTree().getDnfWithCoeff().split("[&,]")) {
                    if (string.length() > 0) {
                        String[] split = string.split("%coeffOf%");
                        String id = correctName(split[0]);
                        double coefficient = 1.0;

                        if (split.length > 1) {
                            id = correctName(split[1]);
                            coefficient = Double.parseDouble(split[0].replaceAll("%%", ""));
                        }

                        if (!model.containsSpecies(id)) {
                            System.err.println("Reaction specifies unlisted catalyst/modifier species - adding this species. (" + id + ")");
                            Species species = model.createSpecies(id);
                            species.setCompartment(r.getCompartment());
                            species.setMetaId(species.getId() + model.getSpeciesCount());
                        }

                        String modId = id + "_catalystOf_" + reaction.getId();

                        if (model.findModifierSpeciesReference(modId) == null) {
                            ModifierSpeciesReference mod = model.createModifier();
                            if (level > 2) {
                                mod.setSBOTerm(SBOTerms.getDefaultSBOCatalyst());
                            }

                            mod.setSpecies(id);
                            mod.setId(modId);
                            model.getModifierSpeciesReferences().add(mod);

                            try {
                                mod.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                        "    <p>CATALYST/MODIFIER_STOICHIOMETRY: " + coefficient + "</p>\n" +
                                        "  </body>");
                            } catch (XMLStreamException e) {
                                e.printStackTrace();
                            }
                        } else {
                            reaction.addModifier(model.findModifierSpeciesReference(modId));
                        }
                    }
                }

                for (String string : r.getInhibitorsTree().getDnfWithCoeff().split("[&,]")) {
                    if (string.length() > 0) {
                        String[] split = string.split("%coeffOf%");
                        String id = correctName(split[0]);
                        double coefficient = 1.0;

                        if (split.length > 1) {
                            id = correctName(split[1]);
                            coefficient = Double.parseDouble(split[0].replaceAll("%%", ""));
                        }

                        ModifierSpeciesReference m = null;

                        if (!model.containsSpecies(id)) {
                            System.err.println("Reaction specifies unlisted inhibitor species - adding this species. (" + id + ")");
                            Species species = model.createSpecies(id);
                            species.setCompartment(r.getCompartment());
                            species.setMetaId(species.getId() + model.getSpeciesCount());
                        }

                        String modId = id + "_inhibitorOf_" + reaction.getId();

                        if (model.findModifierSpeciesReference(modId) == null) {
                            ModifierSpeciesReference mod = model.createModifier();
                            if (level > 2) {
                                mod.setSBOTerm(SBOTerms.getDefaultSBOInhibitor());
                            }
                            mod.setSpecies(id);
                            mod.setId(modId);
                            model.getModifierSpeciesReferences().add(mod);

                            try {
                                mod.setNotes("<body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                        "    <p>INHIBITOR_STOICHIOMETRY: " + coefficient + "</p>\n" +
                                        "  </body>");
                            } catch (XMLStreamException e) {
                                e.printStackTrace();
                            }
                        } else {
                            reaction.addModifier(model.findModifierSpeciesReference(modId));
                        }
                    }
                }
                reaction.setReversible(r.isReversible());
            }
        }
    }

    /**
     * corrects string representing double
     *
     * @param str input double as string
     * @return corrected double
     */
    private double correctDouble(String str) {
        if (str.matches("\\d+(\\.\\d+)")) {
            return Double.parseDouble(str);
        } else {
            return 0.0;
        }
    }

    /**
     * corrects SBO term
     *
     * @param sboTerm input SBO term
     * @return sboTerm or 1 if invalid SBO
     */
    private int correctSBO(String sboTerm) {
        if (sboTerm.matches("(SBO:)?\\d{7}")) {
            return Integer.parseInt(sboTerm.replace("SBO:", ""));
        } else {
            return 1;
        }
    }

    /**
     * corrects string (identifier)
     *
     * @param name input string
     * @return input with all non valid characters replaced by underscores
     */
    String correctName(String name) {
        if (correctInvalidStrings) {
            if (!name.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                name = name.replaceAll("[&+]", "_and_");
                name = name.replaceAll("[,/]", "_or_");
                name = name.replaceAll("[.'() ]", "_");
                name = name.replaceAll("(_and_|_or_|_)+", "_");
                name = name.replaceAll("[^_a-zA-Z0-9]", "");

                if (name.length() == 0) {
                    name = "null";
                }
            } else if (name.length() == 0) {
                return "null";
            }
        }

        return name;
    }

    /**
     * get SBML file content in readable string
     *
     * @return SBML file content as String
     */
    public String getDocAsString() {
        SBMLWriter writer = new SBMLWriter();

        try {
            return writer.writeSBMLToString(this.sbmlDocument);
        } catch (SBMLException | XMLStreamException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void nodeAdded(TreeNode treeNode) {
    }

    @Override
    public void nodeRemoved(TreeNodeRemovedEvent treeNodeRemovedEvent) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    public SBMLDocument getSbmlDocument() {
        return sbmlDocument;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public void setModelMetaData(String modelMetaData) {
        this.modelMetaData = modelMetaData;
    }

    public void setAuthorGivenName(String authorGivenName) {
        this.authorGivenName = authorGivenName;
    }

    public void setAuthorFamilyName(String authorFamilyName) {
        this.authorFamilyName = authorFamilyName;
    }

    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public boolean isCorrectInvalidStrings() {
        return correctInvalidStrings;
    }

    public void setCorrectInvalidStrings(boolean correctInvalidStrings) {
        this.correctInvalidStrings = correctInvalidStrings;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
