package io.documents;

import io.parser.Parser;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Doc {

    private Path path;
    private Parser parser;
    private DataModel dataModel;
    private String inputStr = "";
    private ReactionSet<Reaction> reactionSet = new ReactionSet<>();
    private FoodSet<Species> speciesFoodSet = new FoodSet<>();
    private SpeciesSet<Species> speciesSet = new SpeciesSet<>();
    private ArrayList<String> comments = new ArrayList<>();

    /**
     * Creates document representation
     *
     * @param path path of input file
     */
    public Doc(Path path) {
        this.path = path;
    }

    /**
     * Creates document representation
     *
     * @param inputStr input document as String
     */
    public Doc(String inputStr) {
        this.inputStr = inputStr;
    }

    /**
     * Creates document representation
     *
     * @param dataModel data model from which document is built
     */
    public Doc(DataModel dataModel) {
        setReactionSet(dataModel.getReactionSet());
        setSpeciesFoodSet(dataModel.getFoodSet());
        setSpeciesSet(dataModel.getSpeciesSet());
        comments.addAll(dataModel.getComments());
        setDataModel(dataModel);
    }

    /**
     * Reads in document defined by path variable
     */
    public void readIn() {
        if (path.toFile().exists()) {
            try (Stream<String> stream = Files.lines(path)) {
                stream.forEach(parser::parseLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            dataModel = new DataModel(this);
            dataModel.initDescendants();
        } else {
            System.err.println("File " + path.toString() + " does not exist.");
        }
    }

    /**
     * Reads in document from input string
     */
    public void readIn(String inputStr) {
        Arrays.asList(inputStr.split("[\n]")).forEach(parser::parseLine);
        dataModel = new DataModel(this);
        dataModel.initDescendants();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public ReactionSet<Reaction> getReactionSet() {
        return reactionSet;
    }

    public void setReactionSet(ReactionSet<Reaction> reactionSet) {
        this.reactionSet = reactionSet;
    }

    public FoodSet<Species> getSpeciesFoodSet() {
        return speciesFoodSet;
    }

    public void setSpeciesFoodSet(FoodSet<Species> speciesFoodSet) {
        this.speciesFoodSet = speciesFoodSet;
    }

    public SpeciesSet<Species> getSpeciesSet() {
        return speciesSet;
    }

    public void setSpeciesSet(SpeciesSet<Species> speciesSet) {
        this.speciesSet = speciesSet;
    }

    public void addSpecies(Species s) {
        speciesSet.addSpecies(s);
    }

    public void addSpecies(String s) {
        speciesSet.addSpecies(s);
    }

    public void addFood(Species s) {
        speciesFoodSet.addFood(s);
    }

    public void addFood(String s) {
        speciesFoodSet.addFood(s);
    }

    public void addReaction(Reaction r) {
        reactionSet.addReaction(r);
    }

    public void addReaction(String s) {
        reactionSet.addReaction(s);
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public CRSDoc toCRSDoc() {
        return (CRSDoc) this;
    }

    public WimDoc toWimDoc() {
        return (WimDoc) this;
    }

    public SBMLDoc toSBMLDoc() {
        return (SBMLDoc) this;
    }

    public DBDoc toDBDoc() {
        return (DBDoc) this;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public String getInputStr() {
        return inputStr;
    }

    public void setInputStr(String inputStr) {
        this.inputStr = inputStr;
    }
}
