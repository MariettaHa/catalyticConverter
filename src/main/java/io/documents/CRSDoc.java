package io.documents;

import io.parser.CRSParser;
import model.DataModel;

import java.nio.file.Path;

public class CRSDoc extends Doc {

    private CRSParser crsParser = new CRSParser(this);

    /**
     * Creates CRS document representation
     *
     * @param path path for input CRS document
     */
    public CRSDoc(Path path) {
        super(path);
        setParser(this.crsParser);
    }

    /**
     * Creates CRS document representation
     *
     * @param inputStr input CRS document as String
     */
    public CRSDoc(String inputStr) {
        super(inputStr);
        setParser(this.crsParser);
    }

    /**
     * Creates CRS document representation
     *
     * @param dataModel data model from which document is built
     */
    public CRSDoc(DataModel dataModel) {
        super(dataModel);
    }
}
