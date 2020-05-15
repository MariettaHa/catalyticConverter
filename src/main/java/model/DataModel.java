package model;

import io.documents.*;
import logic.ReactionTreeBuilder;

import java.util.ArrayList;
import java.util.Collection;

public class DataModel {

    FoodSet<Species> foodSet = new FoodSet<>();
    ReactionSet<Reaction> reactionSet = new ReactionSet<>();
    SpeciesSet<Species> speciesSet = new SpeciesSet<>();
    ArrayList<String> comments = new ArrayList<>();
    CRSDoc crsDoc = null;
    DBDoc dbDoc = null;
    SBMLDoc sbmlDoc = null;
    WimDoc wimDoc = null;

    public DataModel() {
    }

    /**
     * Creates copy of existing data model
     *
     * @param dataModel original data model
     */
    public DataModel(DataModel dataModel) {
        this.foodSet = new FoodSet<>(dataModel.getFoodSet());
        this.speciesSet = new SpeciesSet<>(dataModel.getSpeciesSet());
        this.reactionSet = new ReactionSet<>(dataModel.getReactionSet());
        this.comments = new ArrayList<>(dataModel.getComments());
    }

    /**
     * Creates data model based on document representation
     *
     * @param doc document representation
     */
    public DataModel(Doc doc) {
        foodSet = doc.getSpeciesFoodSet();
        reactionSet = doc.getReactionSet();
        speciesSet = doc.getSpeciesSet();
        comments = doc.getComments();

        setDocVar(doc);
    }

    /**
     * Sets the document representation
     *
     * @param doc document representation
     */
    private void setDocVar(Doc doc) {
        switch (doc.getClass().getSimpleName()) {
            case "CRSDoc":
                crsDoc = (CRSDoc) doc;
                break;
            case "DBDoc":
                dbDoc = (DBDoc) doc;
                break;
            case "SBMLDoc":
                sbmlDoc = (SBMLDoc) doc;
                break;
            case "WimDoc":
                wimDoc = (WimDoc) doc;
                break;
            default:
                break;
        }
    }

    /**
     * @return amount of food items in data model's food set
     */
    public int getFoodCount() {
        return foodSet.size();
    }

    /**
     * @return amount of all food and nonfood items in data model's species set
     */
    public int getSpeciesCount() {
        return speciesSet.size();
    }

    /**
     * @return amount of reactions in data model's reaction set
     */
    public int getReactionCount() {
        return reactionSet.size();
    }

    /**
     * adds species to species set
     *
     * @param species species instance
     */
    public void addSpecies(Species species) {
        speciesSet.add(species);
    }

    /**
     * adds reaction to reaction set
     *
     * @param reaction reaction instance
     */
    public void addReaction(Reaction reaction) {
        reactionSet.add(reaction);
    }

    /**
     * adds species to food set
     *
     * @param species species instance
     */
    public void addFood(Species species) {
        foodSet.addFood(species);
    }

    /**
     * adds species list to species set
     *
     * @param species species instance
     */
    public void addSpecies(Collection<Species> species) {
        speciesSet.addAll(species);
    }

    /**
     * adds reaction list to reaction set
     *
     * @param reactions reaction instance
     */
    public void addReactions(Collection<Reaction> reactions) {
        reactionSet.addAll(reactions);
    }

    /**
     * adds species list to food set
     *
     * @param food species instance
     */
    public void addFood(Collection<Species> food) {
        foodSet.addAll(food);
    }

    /**
     * checks CRS criteria (min 1. reactant, min 1. product, min 1. modifier, min 1. reaction, min 1. food item)
     *
     * @return true if model satisfies CRS criteria
     */
    public boolean satisfiesCRScriteria() {
        boolean hasMod = true;
        boolean hasReactant = true;
        boolean hasProduct = true;
        boolean hasReactionAndFood = !(reactionSet.isEmpty()) && !(foodSet.isEmpty());

        for (Reaction r : reactionSet) {
            hasMod = !(r.getCatalystsList().isEmpty() && r.getInhibitorsList().isEmpty());
            hasReactant = !(r.getReactantsList().isEmpty());
            hasProduct = !(r.getProductsList().isEmpty());
        }

        return hasMod && hasReactant && hasProduct && hasReactionAndFood;
    }

    /**
     * sets list for all four reaction input or output types
     */
    public void initDescendants() {
        for (Reaction r : reactionSet) {
            String reactants[] = r.getReactantsTree().getDnf().split("[,&]");

            for (String s : reactants) {
                if (!speciesSet.getIdToSpeciesMap().containsKey(s)) {
                    speciesSet.addSpecies(s);
                }
                r.getReactantsList().add(speciesSet.getIdToSpeciesMap().get(s));
            }

            String products[] = r.getProductsTree().getDnf().split("[,&]");

            for (String s : products) {
                if (!speciesSet.getIdToSpeciesMap().containsKey(s)) {
                    speciesSet.addSpecies(s);
                }
                r.getProductsList().add(speciesSet.getIdToSpeciesMap().get(s));
            }

            if (r.getCatalystsTree().getChildren().size() > 0) {
                String catalysts[] = r.getCatalystsTree().getDnf().split("[,&]");

                for (String s : catalysts) {
                    if (!speciesSet.getIdToSpeciesMap().containsKey(s)) {
                        speciesSet.addSpecies(s);
                    }
                    r.getCatalystsList().add(speciesSet.getIdToSpeciesMap().get(s));
                }
            }

            if (r.getInhibitorsTree().getChildren().size() > 0) {
                String inhibitors[] = r.getInhibitorsTree().getDnf().split("[,&]");

                for (String s : inhibitors) {
                    if (!speciesSet.getIdToSpeciesMap().containsKey(s)) {
                        speciesSet.addSpecies(s);
                    }
                    r.getInhibitorsList().add(speciesSet.getIdToSpeciesMap().get(s));
                }
            }
        }
    }

    /**
     * builds reaction trees for custom formatted document
     *
     * @param parserType the custom file's type
     */
    public void buildCustomTrees(String parserType) {
        if (parserType.equals("custom")) {
            for (Reaction reaction : reactionSet) {
                ArrayList<String> stringsWithCoefficients = new ArrayList<>();
                String reactantsStr = formatStr(reaction.getRawReactantsStr());
                String productsStr = formatStr(reaction.getRawProductsStr());
                String catStr = formatStr(reaction.getRawCatalysatorsStr());
                String inhibStr = formatStr(reaction.getRawInhibitorsStr());

                if (catStr.length() == 0) {
                    catStr = "noCata";
                }
                if (inhibStr.length() == 0) {
                    inhibStr = "noInhib";
                }

                stringsWithCoefficients.add(formatStrWithCoeff("reactants::" + reaction.getRawReactantsStr().strip()));
                stringsWithCoefficients.add(formatStrWithCoeff("products::" + reaction.getRawProductsStr().strip()));

                if (reaction.getRawCatalysatorsStr().length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + reaction.getRawCatalysatorsStr().strip()));
                }
                if (reaction.getRawCatalysatorsStr().length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + reaction.getRawInhibitorsStr().strip()));
                }

                ReactionTreeBuilder reactionTreeBuilder
                        = new ReactionTreeBuilder(parserType, reaction, reactantsStr, catStr + "\t" + inhibStr, productsStr, stringsWithCoefficients);
                reactionTreeBuilder.buildTrees();
            }
        } else if (parserType.equals("table")) {

            String reactantsStr = "";
            String productsStr = "";
            String catStr = "";
            String inhibStr = "";

            for (Reaction reaction : reactionSet) {
                ArrayList<String> stringsWithCoefficients = new ArrayList<>();

                reactantsStr = formatStr(reaction.getRawReactantsStr());
                productsStr = formatStr(reaction.getRawProductsStr());
                catStr = formatStr(reaction.getRawCatalysatorsStr());
                inhibStr = formatStr(reaction.getRawInhibitorsStr());

                if (catStr.length() == 0) {
                    catStr = "noCata";
                }
                if (inhibStr.length() == 0) {
                    inhibStr = "noInhib";
                }
                stringsWithCoefficients.add(formatStrWithCoeff("reactants::" + reaction.getRawReactantsStr().strip()));
                stringsWithCoefficients.add(formatStrWithCoeff("products::" + reaction.getRawProductsStr().strip()));

                if (reaction.getRawCatalysatorsStr().length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + reaction.getRawCatalysatorsStr().strip()));
                }
                if (reaction.getRawCatalysatorsStr().length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + reaction.getRawInhibitorsStr().strip()));
                }
                ReactionTreeBuilder reactionTreeBuilder
                        = new ReactionTreeBuilder(parserType, reaction, reactantsStr, catStr + "\t" + inhibStr, productsStr, stringsWithCoefficients);
                reactionTreeBuilder.buildTrees();
            }
        }
    }

    /**
     * formats equation string without coefficients
     *
     * @param str input string
     * @return formatted string
     */
    private String formatStr(String str) {
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("(^|\\s|&|,|\\(|\\[|\\{)(\\d+[.]\\d+|\\d+)($|\\s)", "$1");
        str = str.replaceAll("\\s&\\s", "&").replaceAll("\\s,\\s", ",");

        return str.strip();
    }

    /**
     * formats equation string with coefficients
     *
     * @param str input string
     * @return formatted string
     */
    private String formatStrWithCoeff(String str) {
        str = str.replaceAll("\\s\\s+", " "); //remove multiple white spaces before assessing coefficient
        str = str.replaceAll("([^A-Za-z0-9-_/().'%]|^)(\\d+[.]\\d+|\\d+)(\\s+)([A-Za-z-_/().'%][A-Za-z0-9-_/().'%]*)([^A-Za-z0-9-_/().'%]|$)", "$1%%$2%coeffOf%$4$5");
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("\\s\\s+", " ");
        str = str.replaceAll("\\s*&\\s*", "&").replaceAll("\\s,\\s", ",");

        return str;
    }

    /**
     * checks WIM criteria (all molecule instances from reactions are listed in molecule or food set paragraph,
     * min. 1 nonfood or food item)
     *
     * @return true if model satisfies WIM criteria
     */
    public boolean satisfiesWIMcriteria() {
        boolean hasReactant = true;
        boolean hasProduct = true;
        boolean hasMoleculesOrFood = speciesSet.size() + foodSet.size() > 0;
        boolean hasReaction = !reactionSet.isEmpty();
        boolean allMolsDefined = true;

        for (Reaction r : reactionSet) {
            hasReactant = !(r.getReactantsList().isEmpty());
            hasProduct = !(r.getProductsList().isEmpty());

            for (Species s : r.getReactantsList()) {
                if (!speciesSet.contains(s)) {
                    allMolsDefined = false;
                    break;
                }
            }

            if (allMolsDefined) {
                for (Species s : r.getProductsList()) {
                    if (!speciesSet.contains(s)) {
                        allMolsDefined = false;
                        break;
                    }
                }
            }

            if (allMolsDefined) {
                for (Species s : r.getCatalystsList()) {
                    if (!speciesSet.contains(s)) {
                        allMolsDefined = false;
                        break;
                    }
                }
            }

            if (allMolsDefined) {
                for (Species s : r.getInhibitorsList()) {
                    if (!speciesSet.contains(s)) {
                        allMolsDefined = false;
                        break;
                    }
                }
            }
        }

        return hasReactant && hasProduct & hasMoleculesOrFood & allMolsDefined & hasReaction;
    }

    /**
     * @return true if model contains reaction(s) without min. 1 reactant and min 1. product
     */
    public boolean notEmpty() {
        boolean hasReactant = true;
        boolean hasProduct = true;
        boolean hasReaction = !reactionSet.isEmpty();

        for (Reaction r : reactionSet) {
            hasReactant = !(r.getReactantsList().isEmpty());
            hasProduct = !(r.getProductsList().isEmpty());
        }

        boolean valid = false;
        valid = (hasReactant && hasProduct && hasReaction);

        return valid;
    }

    public FoodSet<Species> getFoodSet() {
        return foodSet;
    }

    public ReactionSet<Reaction> getReactionSet() {
        return reactionSet;
    }

    public SpeciesSet<Species> getSpeciesSet() {
        return speciesSet;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

}
