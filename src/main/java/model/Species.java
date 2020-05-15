package model;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.HashSet;

public class Species {
    String speciesId = "";
    String sboTerm = "SBO:0000285";
    String speciesType = "defaultType";
    double initAmount = 0.0;
    String compartment = "default";
    String description = "";
    String metaData = "";
    String note = "";
    String networkId = "defaultNetwork";
    double posX = 100.0;
    double posY = 100.0;
    double posZ = 100.0;
    boolean hasOnlySubstanceUnits = false;
    boolean boundaryCondition = false;
    boolean constant = false;
    String metaId = "";
    double initConcentration = 0.0;
    String substanceUnits = "";
    String conversionFactor = "";

    HashSet<Reaction> speciesAsReactantOfList = new HashSet<>();
    HashSet<Reaction> speciesAsProductOfList = new HashSet<>();
    HashSet<Reaction> speciesAsCatalystOfList = new HashSet<>();
    HashSet<Reaction> speciesAsInhibitorOfList = new HashSet<>();

    HashSet<String> speciesNames = new HashSet<>();

    public Species() {
    }

    /**
     * creates species instance by species id
     *
     * @param speciesId the species id
     */
    public Species(String speciesId) {
        setSpeciesId(speciesId);
    }

    /**
     * adds name to names set
     *
     * @param name the new name
     */
    public void addSpeciesName(String name) {
        speciesNames.add(name);
    }

    /**
     * adds array content to names set
     *
     * @param names the new names
     */
    public void addSpeciesNames(String[] names) {
        if (names != null) {
            if (names.length > 0) {
                getSpeciesNames().addAll(Arrays.asList(names));
            }
        }
    }

    /**
     * initialize species from JSBML Species representation
     *
     * @param sbmlSpecies the JSBML Species instance
     */
    public void init(org.sbml.jsbml.Species sbmlSpecies) {
        this.setSpeciesId(sbmlSpecies.getId());
        this.setCompartment(sbmlSpecies.getCompartment());
        this.setHasOnlySubstanceUnits(sbmlSpecies.getHasOnlySubstanceUnits());
        this.setBoundaryCondition(sbmlSpecies.getBoundaryCondition());
        this.setConstant(sbmlSpecies.getConstant());
        this.setMetaId(sbmlSpecies.getMetaId());
        this.setSboTerm(String.valueOf(sbmlSpecies.getSBOTerm()));
        this.getSpeciesNames().add(sbmlSpecies.getName());
        this.setInitAmount(sbmlSpecies.getInitialAmount());
        this.setInitConcentration(sbmlSpecies.getInitialConcentration());
        this.setSubstanceUnits(sbmlSpecies.getSubstanceUnits());
        this.setConversionFactor(sbmlSpecies.getConversionFactor());
        try {
            this.setNote(sbmlSpecies.getNotesString());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getSboTerm() {
        return sboTerm;
    }

    public void setSboTerm(String sboTerm) {
        this.sboTerm = sboTerm;
    }

    public String getSpeciesType() {
        return speciesType;
    }

    public void setSpeciesType(String speciesType) {
        this.speciesType = speciesType;
    }

    public double getInitAmount() {
        return initAmount;
    }

    public void setInitAmount(double initAmount) {
        this.initAmount = initAmount;
    }

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
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

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public boolean isHasOnlySubstanceUnits() {
        return hasOnlySubstanceUnits;
    }

    public void setHasOnlySubstanceUnits(boolean hasOnlySubstanceUnits) {
        this.hasOnlySubstanceUnits = hasOnlySubstanceUnits;
    }

    public boolean isBoundaryCondition() {
        return boundaryCondition;
    }

    public void setBoundaryCondition(boolean boundaryCondition) {
        this.boundaryCondition = boundaryCondition;
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    public String getMetaId() {
        return metaId;
    }

    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    public double getInitConcentration() {
        return initConcentration;
    }

    public void setInitConcentration(double initConcentration) {
        this.initConcentration = initConcentration;
    }

    public String getSubstanceUnits() {
        return substanceUnits;
    }

    public void setSubstanceUnits(String substanceUnits) {
        this.substanceUnits = substanceUnits;
    }

    public String getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(String conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public HashSet<String> getSpeciesNames() {
        return speciesNames;
    }

    public void setSpeciesNames(HashSet<String> speciesNames) {
        this.speciesNames = speciesNames;
    }

    public HashSet<Reaction> getSpeciesAsReactantOfList() {
        return speciesAsReactantOfList;
    }

    public void setSpeciesAsReactantOfList(HashSet<Reaction> speciesAsReactantOfList) {
        this.speciesAsReactantOfList = speciesAsReactantOfList;
    }

    public HashSet<Reaction> getSpeciesAsProductOfList() {
        return speciesAsProductOfList;
    }

    public void setSpeciesAsProductOfList(HashSet<Reaction> speciesAsProductOfList) {
        this.speciesAsProductOfList = speciesAsProductOfList;
    }

    public HashSet<Reaction> getSpeciesAsCatalystOfList() {
        return speciesAsCatalystOfList;
    }

    public void setSpeciesAsCatalystOfList(HashSet<Reaction> speciesAsCatalystOfList) {
        this.speciesAsCatalystOfList = speciesAsCatalystOfList;
    }

    public HashSet<Reaction> getSpeciesAsInhibitorOfList() {
        return speciesAsInhibitorOfList;
    }

    public void setSpeciesAsInhibitorOfList(HashSet<Reaction> speciesAsInhibitorOfList) {
        this.speciesAsInhibitorOfList = speciesAsInhibitorOfList;
    }

}
