package io.documents;

import logic.ReactionTreeBuilder;
import model.DataModel;
import model.Reaction;
import model.Species;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class DBDoc extends Doc {

    boolean validFile = true;

    /**
     * Creates DB document representation
     *
     * @param path path of input SQLite database
     */
    public DBDoc(Path path) {
        super(path);
    }

    /**
     * Creates DB document representation
     *
     * @param dataModel data model from which document is built
     */
    public DBDoc(DataModel dataModel) {
        super(dataModel);
    }

    /**
     * Tests DBDoc construction with result validation
     *
     * @param file path of input SQLite database
     * @return true if file results in valid data model
     */
    public static boolean validateFile(File file) {
        DBDoc dbDoc = new DBDoc(file.toPath());
        boolean valid = dbDoc.readInAndValidate();
        DataModel dataModel = dbDoc.getDataModel();

        return (valid && (dataModel != null));
    }

    /**
     * Creates connection to SQLite database
     */
    private Connection connect() {
        String url = "jdbc:sqlite:" + getPath();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * Reads in SQLite database
     */
    public void readIn() {
        String sql = "SELECT * FROM species";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Species species = new Species(rs.getString(1));
                species.setSpeciesNames(new HashSet<>(Arrays.asList(rs.getString(5).split(";"))));
                species.setSboTerm(rs.getString(2));
                species.setSpeciesType(rs.getString(6));
                species.setInitAmount(rs.getDouble(3));
                species.setInitConcentration(rs.getDouble(4));
                species.setCompartment(rs.getString(7));
                species.setDescription(rs.getString(8));
                species.setMetaData(rs.getString(9));
                species.setNote(rs.getString(10));
                species.setPosX(rs.getDouble(11));
                species.setPosY(rs.getDouble(12));
                species.setPosZ(rs.getDouble(13));
                species.setHasOnlySubstanceUnits(rs.getBoolean(14));
                species.setBoundaryCondition(rs.getBoolean(15));
                species.setConstant(rs.getBoolean(16));
                species.setMetaData(rs.getString(17));
                species.setNetworkId(rs.getString(18));
                species.setSubstanceUnits(rs.getString(19));
                species.setConversionFactor(rs.getString(20));

                addSpecies(species);

                if (species.getInitAmount() > 0.0) {
                    addFood(species);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "SELECT * FROM reactions";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ArrayList<String> stringsWithCoefficients = new ArrayList<>();
                Reaction reaction = new Reaction(rs.getString(1));
                reaction.setReactionName(rs.getString(2));
                reaction.setSboTerm(rs.getString(3));
                reaction.setDescription(rs.getString(4));
                reaction.setMetaData(rs.getString(5));
                reaction.setCompartment(rs.getString(6));
                reaction.setReactionType(rs.getString(7));
                reaction.setNote(rs.getString(8));
                reaction.setNetworkId(rs.getString(9));
                reaction.setFormula(rs.getString(10));
                reaction.setStartPosX(rs.getDouble(11));
                reaction.setStartPosY(rs.getDouble(12));
                reaction.setStartPosZ(rs.getDouble(13));
                reaction.setEndPosX(rs.getDouble(14));
                reaction.setEndPosY(rs.getDouble(15));
                reaction.setEndPosZ(rs.getDouble(16));
                reaction.setReversible(rs.getInt(17) == 1);
                reaction.setMetaId(rs.getString(18));
                reaction.setWeight(rs.getDouble(19));
                String reactantsStr = formatStr(rs.getString(20).replaceAll("&", "+"));
                String productsStr = formatStr(rs.getString(21).replaceAll("&", "+"));
                String catalystsStr = formatStr(rs.getString(22).replaceAll("&", "+"));
                String inhibitorsStr = formatStr(rs.getString(23).replaceAll("&", "+"));

                String catStr = "noCata";
                String inhibStr = "noInhib";

                stringsWithCoefficients.add(formatStrWithCoeff("reactants::" + rs.getString(20)
                        .replaceAll("&", "+")));
                stringsWithCoefficients.add(formatStrWithCoeff("products::" + rs.getString(21)
                        .replaceAll("&", "+")));

                if (catalystsStr.length() > 0 && inhibitorsStr.length() > 0) {
                    catStr = catalystsStr.replaceAll("[]\\[]", "").strip();
                    inhibStr = inhibitorsStr.replaceAll("[}{]", "").strip();

                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" +
                            rs.getString(22).replaceAll("&", "+").strip()));
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" +
                            rs.getString(23).replaceAll("&", "+").strip()));
                } else if (catalystsStr.length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::"
                            + rs.getString(23).replaceAll("&", "+").strip()));
                } else if (inhibitorsStr.length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::"
                            + rs.getString(22).replaceAll("&", "+").strip()));
                }

                ReactionTreeBuilder reactionTreeBuilder = new ReactionTreeBuilder("db", reaction,
                        reactantsStr, catStr + "\t" + inhibStr, productsStr, stringsWithCoefficients);
                reactionTreeBuilder.buildTrees();
                addReaction(reaction);

                for (String s : reaction.getReactantsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getReactantsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getProductsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getProductsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getCatalystsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getCatalystsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                        reaction.getModifiersList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getInhibitorsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getInhibitorsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                        reaction.getModifiersList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setDataModel(new DataModel(this));
        getDataModel().initDescendants();
    }

    /**
     * Reformats string to match DNF format
     *
     * @param str The string that is reformatted
     * @return reformatted string
     */
    private String formatStr(String str) {
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("(^|\\s|&|,|\\(|\\[|\\{)(\\d+[.]\\d+|\\d+)($|\\s)", "$1");
        str = str.replaceAll("\\s&\\s", "&").replaceAll("\\s,\\s", ",");

        return str.strip();
    }

    /**
     * Reformats string to match DNF format containing coefficients for better later parsing
     *
     * @param str The string that is reformatted
     * @return reformatted string
     */
    private String formatStrWithCoeff(String str) {
        str = str.replaceAll("( )( )*", " ");
        str = str.replaceAll("([^A-Za-z0-9-_/().'%]|^)(\\d+[.]\\d+|\\d+)(\\s+)" +
                "([A-Za-z-_/().'%][A-Za-z0-9-_/().'%]*)([^A-Za-z0-9-_/().'%]|$)", "$1%%$2%coeffOf%$4$5");
        str = str.replaceAll("\\s?\\+\\s?", "&");
        str = str.replaceAll("( )( )*", " ");
        str = str.replaceAll("\\s*&\\s*", "&").replaceAll("\\s,\\s", ",");

        return str;
    }

    /**
     * Reads in SQLite database and checks for invalid results
     *
     * @return true if valid data model can be created
     */
    private boolean readInAndValidate() {
        String sql = "SELECT * FROM species";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Species species = new Species(rs.getString(1));
                species.setSpeciesNames(new HashSet<>(Arrays.asList(rs.getString(5).split(";"))));
                species.setSboTerm(rs.getString(2));
                species.setSpeciesType(rs.getString(6));
                species.setInitAmount(rs.getDouble(3));
                species.setInitConcentration(rs.getDouble(4));
                species.setCompartment(rs.getString(7));
                species.setDescription(rs.getString(8));
                species.setMetaData(rs.getString(9));
                species.setNote(rs.getString(10));
                species.setPosX(rs.getDouble(11));
                species.setPosY(rs.getDouble(12));
                species.setPosZ(rs.getDouble(13));
                species.setHasOnlySubstanceUnits(rs.getBoolean(14));
                species.setBoundaryCondition(rs.getBoolean(15));
                species.setConstant(rs.getBoolean(16));
                species.setMetaData(rs.getString(17));
                species.setNetworkId(rs.getString(18));
                species.setSubstanceUnits(rs.getString(19));
                species.setConversionFactor(rs.getString(20));
                addSpecies(species);
                if (species.getInitAmount() > 0.0) {
                    addFood(species);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            validFile = false;
        }

        sql = "SELECT * FROM reactions";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ArrayList<String> stringsWithCoefficients = new ArrayList<>();
                Reaction reaction = new Reaction(rs.getString(1));
                reaction.setReactionName(rs.getString(2));
                reaction.setSboTerm(rs.getString(3));
                reaction.setDescription(rs.getString(4));
                reaction.setMetaData(rs.getString(5));
                reaction.setCompartment(rs.getString(6));
                reaction.setReactionType(rs.getString(7));
                reaction.setNote(rs.getString(8));
                reaction.setNetworkId(rs.getString(9));
                reaction.setFormula(rs.getString(10));
                reaction.setStartPosX(rs.getDouble(11));
                reaction.setStartPosY(rs.getDouble(12));
                reaction.setStartPosZ(rs.getDouble(13));
                reaction.setEndPosX(rs.getDouble(14));
                reaction.setEndPosY(rs.getDouble(15));
                reaction.setEndPosZ(rs.getDouble(16));
                reaction.setReversible(rs.getInt(17) == 1);
                reaction.setMetaId(rs.getString(18));
                reaction.setWeight(rs.getDouble(19));
                String reactantsStr = formatStr(rs.getString(20).replaceAll("&", "+"));
                String productsStr = formatStr(rs.getString(21).replaceAll("&", "+"));
                String catalystsStr = formatStr(rs.getString(22).replaceAll("&", "+"));
                String inhibitorsStr = formatStr(rs.getString(23).replaceAll("&", "+"));

                String catStr = "noCata";
                String inhibStr = "noInhib";

                stringsWithCoefficients.add(formatStrWithCoeff("reactants::" + rs.getString(20)
                        .replaceAll("&", "+")));
                stringsWithCoefficients.add(formatStrWithCoeff("products::" + rs.getString(21)
                        .replaceAll("&", "+")));

                if (catalystsStr.length() > 0 && inhibitorsStr.length() > 0) {
                    catStr = catalystsStr.replaceAll("\\]|\\[", "").strip();
                    inhibStr = inhibitorsStr.replaceAll("\\}|\\{", "").strip();
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + rs.getString(22)
                            .replaceAll("&", "+").strip()));
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + rs.getString(23)
                            .replaceAll("&", "+").strip()));
                } else if (catalystsStr.length() > 0 && inhibitorsStr.length() <= 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("inhibitors::" + rs.getString(23)
                            .replaceAll("&", "+").strip()));
                } else if (catalystsStr.length() <= 0 && inhibitorsStr.length() > 0) {
                    stringsWithCoefficients.add(formatStrWithCoeff("catalysts::" + rs.getString(22)
                            .replaceAll("&", "+").strip()));
                }

                ReactionTreeBuilder reactionTreeBuilder
                        = new ReactionTreeBuilder("db", reaction, reactantsStr, catStr + "\t"
                        + inhibStr, productsStr, stringsWithCoefficients);
                reactionTreeBuilder.buildTrees();

                addReaction(reaction);

                for (String s : reaction.getReactantsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getReactantsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getProductsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getProductsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getCatalystsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getCatalystsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                        reaction.getModifiersList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }
                for (String s : reaction.getInhibitorsTree().getDnf().split("[,&*+]")) {
                    if (getSpeciesSet().getIdToSpeciesMap().containsKey(s)) {
                        reaction.getInhibitorsList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                        reaction.getModifiersList().add(getSpeciesSet().getIdToSpeciesMap().get(s));
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            validFile = false;
        }

        setDataModel(new DataModel(this));
        getDataModel().initDescendants();

        return validFile;
    }
}
