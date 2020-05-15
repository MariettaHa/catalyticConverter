package io.documents;

import io.parser.WimParser;
import model.DataModel;

import java.nio.file.Path;
import java.util.ArrayList;

public class WimDoc extends Doc {

    private ArrayList<String> metaData = new ArrayList<>();
    private int molCount = 0;
    private int foodCount = 0;
    private int reactionCount = 0;
    private WimParser wimParser = new WimParser(this);

    /**
     * Creates WIM document representation
     *
     * @param path path for input WIM document
     */
    public WimDoc(Path path) {
        super(path);
        setParser(this.wimParser);
    }

    /**
     * Creates WIM document representation
     *
     * @param inputStr input WIM document as String
     */
    public WimDoc(String inputStr) {
        super(inputStr);
        setParser(this.wimParser);
    }

    /**
     * Creates WIM document representation
     *
     * @param dataModel data model from which document is built
     */
    public WimDoc(DataModel dataModel) {
        super(dataModel);
        molCount = dataModel.getSpeciesSet().size() - dataModel.getFoodSet().size();
        foodCount = dataModel.getFoodSet().size();
        reactionCount = dataModel.getReactionSet().size();
    }

    public ArrayList<String> getMetaData() {
        return metaData;
    }

    public void setMetaData(ArrayList<String> metaData) {
        this.metaData = metaData;
    }

    public int getMolCount() {
        return molCount;
    }

    public void setMolCount(int molCount) {
        this.molCount = molCount;
    }

    public int getFoodCount() {
        return foodCount;
    }

    public void setFoodCount(int foodCount) {
        this.foodCount = foodCount;
    }

    public int getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(int reactionCount) {
        this.reactionCount = reactionCount;
    }

}
