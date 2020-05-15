package model;

import logic.BooleanTreeNode;

import java.util.HashSet;

public class Reaction {

    String rawReactantsStr = "";
    String rawProductsStr = "";
    String rawCatalysatorsStr = "";
    String rawInhibitorsStr = "";
    String reactantAND = "&";
    String reactantOR = ",";
    String productAND = "&";
    String productOR = ",";
    String catalystAND = "&";
    String catalystOR = ",";
    String inhibitorAND = "&";
    String inhibitorOR = ",";
    private String reactionId = "";
    private String reactionType = "defaultType";
    private String reactionName = "";
    private String sboTerm = "SBO:0000231";
    private String description = "";
    private String metaData = "";
    private String note = "";
    private String reactionAllParticipants = "";
    private String networkId = "defaultNetwork";
    private String formula = "";
    private double startPosX = 0.0;
    private double startPosY = 0.0;
    private double startPosZ = 0.0;
    private double endPosX = 0.0;
    private double endPosY = 0.0;
    private double endPosZ = 0.0;
    private boolean isReversible = false;
    private double weight = 1.0;
    private String metaId = "";
    private String compartment = "default";
    private BooleanTreeNode reactantsTree = new BooleanTreeNode();
    private BooleanTreeNode productsTree = new BooleanTreeNode();
    private BooleanTreeNode catalystsTree = new BooleanTreeNode();
    private BooleanTreeNode inhibitorsTree = new BooleanTreeNode();
    private HashSet<Species> reactantsList = new HashSet<>();
    private HashSet<Species> productsList = new HashSet<>();
    private HashSet<Species> catalystsList = new HashSet<>();
    private HashSet<Species> inhibitorsList = new HashSet<>();
    private org.sbml.jsbml.Reaction sbmlReactionInstance = null;


    /**
     * creates reaction instance by reaction id
     *
     * @param reactionId the reaction id
     */
    public Reaction(String reactionId) {
        setReactionId(reactionId);
    }

    /**
     * creates reaction formula from reaction's trees
     */
    public String createFormula() {
        if (getProductsList().size() > 0 && getReactantsList().size() > 0) {
            String s = getReactantsTree().getDnfWithCoeff().replaceAll("%coeffOf%|%+", " ");
            if (catalystsTree.getDnfWithCoeff().length() > 0) {
                s += "[" + catalystsTree.getDnfWithCoeff().replaceAll("%coeffOf%|%+", " ").strip() + "]";
            }
            if (inhibitorsTree.getDnfWithCoeff().length() > 0) {
                s += "{" + inhibitorsTree.getDnfWithCoeff().replaceAll("%coeffOf%|%+", " ").strip() + "}";
            }
            s += "<".repeat(isReversible() ? 1 : 0) + "->" + getProductsTree().getDnfWithCoeff().replaceAll("%coeffOf%|%+", " ");

            setFormula(s);
        }
        return getFormula().replaceAll("([\\[{])( )?,", "$1").replaceAll("([\\[{]) ", "$1");
    }

    /**
     * @return list of catalysts and inhibitors
     */
    public HashSet<Species> getModifiersList() {
        HashSet<Species> tmpLst = new HashSet<>();

        tmpLst.addAll(catalystsList);
        tmpLst.addAll(inhibitorsList);

        return tmpLst;
    }

    /**
     * @return string of formula with html complying format
     */
    public String createFormulaHTMLStyle() {
        String formula = createFormula().replaceAll("&", "+").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("[^a-zA-Z0-9-+&;\\s_\\[\\]\\{\\}]", "_");
        return formula;
    }

    public HashSet<Species> getReactantsList() {
        return reactantsList;
    }

    public void setReactantsList(HashSet<Species> reactantsList) {
        this.reactantsList = reactantsList;
    }

    public HashSet<Species> getProductsList() {
        return productsList;
    }

    public void setProductsList(HashSet<Species> productsList) {
        this.productsList = productsList;
    }

    public HashSet<Species> getCatalystsList() {
        return catalystsList;
    }

    public void setCatalystsList(HashSet<Species> catalystsList) {
        this.catalystsList = catalystsList;
    }

    public HashSet<Species> getInhibitorsList() {
        return inhibitorsList;
    }

    public void setInhibitorsList(HashSet<Species> inhibitorsList) {
        this.inhibitorsList = inhibitorsList;
    }

    public BooleanTreeNode getReactantsTree() {
        return reactantsTree;
    }

    public void setReactantsTree(BooleanTreeNode reactantsTree) {
        this.reactantsTree = reactantsTree;
    }

    public BooleanTreeNode getProductsTree() {
        return productsTree;
    }

    public void setProductsTree(BooleanTreeNode productsTree) {
        this.productsTree = productsTree;
    }

    public BooleanTreeNode getCatalystsTree() {
        return catalystsTree;
    }

    public void setCatalystsTree(BooleanTreeNode catalystsTree) {
        this.catalystsTree = catalystsTree;
    }

    public BooleanTreeNode getInhibitorsTree() {
        return inhibitorsTree;
    }

    public void setInhibitorsTree(BooleanTreeNode inhibitorsTree) {
        this.inhibitorsTree = inhibitorsTree;
    }

    public org.sbml.jsbml.Reaction getSbmlReactionInstance() {
        return sbmlReactionInstance;
    }

    public void setSbmlReactionInstance(org.sbml.jsbml.Reaction sbmlReactionInstance) {
        this.sbmlReactionInstance = sbmlReactionInstance;
    }

    public String getReactantAND() {
        return reactantAND;
    }

    public void setReactantAND(String reactantAND) {
        this.reactantAND = reactantAND;
    }

    public String getReactantOR() {
        return reactantOR;
    }

    public void setReactantOR(String reactantOR) {
        this.reactantOR = reactantOR;
    }

    public String getProductAND() {
        return productAND;
    }

    public void setProductAND(String productAND) {
        this.productAND = productAND;
    }

    public String getProductOR() {
        return productOR;
    }

    public void setProductOR(String productOR) {
        this.productOR = productOR;
    }

    public String getCatalystAND() {
        return catalystAND;
    }

    public void setCatalystAND(String catalystAND) {
        this.catalystAND = catalystAND;
    }

    public String getCatalystOR() {
        return catalystOR;
    }

    public void setCatalystOR(String catalystOR) {
        this.catalystOR = catalystOR;
    }

    public String getInhibitorAND() {
        return inhibitorAND;
    }

    public void setInhibitorAND(String inhibitorAND) {
        this.inhibitorAND = inhibitorAND;
    }

    public String getInhibitorOR() {
        return inhibitorOR;
    }

    public void setInhibitorOR(String inhibitorOR) {
        this.inhibitorOR = inhibitorOR;
    }

    public String getReactionId() {
        return reactionId;
    }

    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public String getReactionName() {
        return reactionName;
    }

    public void setReactionName(String reactionName) {
        this.reactionName = reactionName;
    }

    public String getSboTerm() {
        return sboTerm;
    }

    public void setSboTerm(String sboTerm) {
        this.sboTerm = sboTerm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReactionAllParticipants() {
        return reactionAllParticipants;
    }

    public void setReactionAllParticipants(String reactionAllParticipants) {
        this.reactionAllParticipants = reactionAllParticipants;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public double getStartPosX() {
        return startPosX;
    }

    public void setStartPosX(double startPosX) {
        this.startPosX = startPosX;
    }

    public double getStartPosY() {
        return startPosY;
    }

    public void setStartPosY(double startPosY) {
        this.startPosY = startPosY;
    }

    public double getStartPosZ() {
        return startPosZ;
    }

    public void setStartPosZ(double startPosZ) {
        this.startPosZ = startPosZ;
    }

    public double getEndPosX() {
        return endPosX;
    }

    public void setEndPosX(double endPosX) {
        this.endPosX = endPosX;
    }

    public double getEndPosY() {
        return endPosY;
    }

    public void setEndPosY(double endPosY) {
        this.endPosY = endPosY;
    }

    public double getEndPosZ() {
        return endPosZ;
    }

    public void setEndPosZ(double endPosZ) {
        this.endPosZ = endPosZ;
    }

    public boolean isReversible() {
        return isReversible;
    }

    public void setReversible(boolean reversible) {
        isReversible = reversible;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getMetaId() {
        return metaId;
    }

    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    public String getRawReactantsStr() {
        return rawReactantsStr;
    }

    public void setRawReactantsStr(String rawReactantsStr) {
        this.rawReactantsStr = rawReactantsStr;
    }

    public String getRawProductsStr() {
        return rawProductsStr;
    }

    public void setRawProductsStr(String rawProductsStr) {
        this.rawProductsStr = rawProductsStr;
    }

    public String getRawCatalysatorsStr() {
        return rawCatalysatorsStr;
    }

    public void setRawCatalysatorsStr(String rawCatalysatorsStr) {
        this.rawCatalysatorsStr = rawCatalysatorsStr;
    }

    public String getRawInhibitorsStr() {
        return rawInhibitorsStr;
    }

    public void setRawInhibitorsStr(String rawInhibitorsStr) {
        this.rawInhibitorsStr = rawInhibitorsStr;
    }
}
