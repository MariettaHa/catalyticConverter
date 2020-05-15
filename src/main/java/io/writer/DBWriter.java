package io.writer;

import io.Misc;
import io.documents.DBDoc;
import model.Reaction;
import model.Species;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBWriter {

    DBDoc dbDoc;
    String outputPath;

    /**
     * Creates writer for DB document
     *
     * @param dbDoc DB document to write
     */
    public DBWriter(DBDoc dbDoc) {
        this.dbDoc = dbDoc;
    }

    /**
     * writes DB document to file
     *
     * @param outputPath path String into which is written
     */
    public void write(String outputPath) {
        this.outputPath = outputPath;

        Misc.writeContentToFile("", outputPath);
        createEmptyDataBase();
        fillDataBase();
    }

    /**
     * connects to SQLite database
     */
    public Connection connect() {
        String url = "jdbc:sqlite:" + outputPath;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * fills output SQLite data base tables
     */
    private void fillDataBase() {
        // add all species
        ArrayList<String> processedSpeciesIds = new ArrayList<>();
        for (Species species : dbDoc.getSpeciesSet()) {
            if (!processedSpeciesIds.contains(species.getSpeciesId())) {
                processedSpeciesIds.add(species.getSpeciesId());
            }

            StringBuilder sql = new StringBuilder("INSERT INTO species(");

            ArrayList<String> variables = new ArrayList<>(List.of("speciesId", "sbo", "initAmount", "initConcentration",
                    "names", "type", "compartment", "description",
                    "metaData", "note", "posX", "posY", "posZ", "hasOnlySubstanceUnits", "boundaryCondition",
                    "constant", "metaId", "networkId", "substanceUnits", "conversionFactor"));

            for (String variable : variables) {
                sql.append(variable);
                if (variables.indexOf(variable) < variables.size() - 1) {
                    sql.append(",");
                }
            }

            sql.append(") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            try (Connection con = this.connect(); PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
                pstmt.setString(1, species.getSpeciesId());
                pstmt.setString(2, species.getSboTerm());
                pstmt.setDouble(3, species.getInitAmount());
                pstmt.setDouble(4, species.getInitConcentration());
                pstmt.setString(5, String.join(";", species.getSpeciesNames()));
                pstmt.setString(6, species.getSpeciesType());
                if (dbDoc.getSpeciesFoodSet().contains(species) && species.getInitAmount() == 0.0) {
                    species.setInitAmount(1.0);
                }
                pstmt.setString(7, species.getCompartment());
                pstmt.setString(8, species.getDescription());
                pstmt.setString(9, species.getMetaData());
                pstmt.setString(10, species.getNote());
                pstmt.setDouble(11, species.getPosX());
                pstmt.setDouble(12, species.getPosY());
                pstmt.setDouble(13, species.getPosZ());
                pstmt.setBoolean(14, species.isHasOnlySubstanceUnits());
                pstmt.setBoolean(15, species.isBoundaryCondition());
                pstmt.setBoolean(16, species.isConstant());
                pstmt.setString(17, species.getMetaId());
                pstmt.setString(18, species.getNetworkId());
                pstmt.setString(19, species.getSubstanceUnits());
                pstmt.setString(20, species.getConversionFactor());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //add all reactions
        for (Reaction reaction : dbDoc.getReactionSet()) {
            StringBuilder sql = new StringBuilder("INSERT INTO reactions(");

            ArrayList<String> variables = new ArrayList<>(List.of("reactionId", "name", "sbo", "description",
                    "metaData", "compartment",
                    "type", "note", "networkId", "formula",
                    "startPosX", "startPosY", "startPosZ", "endPosX", "endPosY", "endPosZ",
                    "reversible", "metaId", "weight", "reactantsDNF", "productsDNF",
                    "catalystsDNF", "inhibitorsDNF"
            ));

            for (String variable : variables) {
                sql.append(variable);
                if (variables.indexOf(variable) < variables.size() - 1) {
                    sql.append(",");
                }
            }

            sql.append(") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            try (Connection con = this.connect(); PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
                pstmt.setString(1, reaction.getReactionId());
                pstmt.setString(2, reaction.getReactionName());
                pstmt.setString(3, reaction.getSboTerm());
                pstmt.setString(4, reaction.getDescription());
                pstmt.setString(5, reaction.getMetaData());
                pstmt.setString(6, reaction.getCompartment());
                pstmt.setString(7, reaction.getReactionType());
                pstmt.setString(8, reaction.getNote());
                pstmt.setString(9, reaction.getNetworkId());
                pstmt.setString(10, reaction.createFormula());
                pstmt.setDouble(11, reaction.getStartPosX());
                pstmt.setDouble(12, reaction.getStartPosY());
                pstmt.setDouble(13, reaction.getStartPosZ());
                pstmt.setDouble(14, reaction.getEndPosX());
                pstmt.setDouble(15, reaction.getEndPosY());
                pstmt.setDouble(16, reaction.getEndPosZ());
                pstmt.setBoolean(17, reaction.isReversible());
                pstmt.setString(18, reaction.getMetaId());
                pstmt.setDouble(19, reaction.getWeight());
                pstmt.setString(20, strip(reaction.getReactantsTree().getDnfWithCoeff()));
                pstmt.setString(21, strip(reaction.getProductsTree().getDnfWithCoeff()));
                pstmt.setString(22, strip(reaction.getCatalystsTree().getDnfWithCoeff()));
                pstmt.setString(23, strip(reaction.getInhibitorsTree().getDnfWithCoeff()));

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * remove coefficient formatting of DNF
     *
     * @param dnfWithCoeff input DNF
     * @return stripped DNF
     */
    private String strip(String dnfWithCoeff) {
        return dnfWithCoeff.replaceAll("%%([0-9.]+)%coeffOf%", " $1 ").strip();
    }

    /**
     * creates empty DB file / SQLite database in outputpath
     */
    private void createEmptyDataBase() {
        try {
            File file = new File(outputPath);
            PrintWriter pw = new PrintWriter(outputPath);

            pw.close();

            //System.out.println("Empty DB File Created:- (path: " + file.getAbsolutePath() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = "jdbc:sqlite:" + outputPath;

        /*try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        //create empty db
        String sqlNewSpeciesTable = "CREATE TABLE \"species\" (\n" +
                "\t\"speciesId\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"sbo\"\tTEXT DEFAULT 'defaultName',\n" +
                "\t\"sboTerm\"\tTEXT,\n" +
                "\t\"initAmount\"\tREAL,\n" +
                "\t\"initConcentration\"\tREAL,\n" +
                "\t\"posX\"\tREAL,\n" +
                "\t\"posY\"\tREAL,\n" +
                "\t\"posZ\"\tREAL,\n" +
                "\t\"names\"\tTEXT,\n" +
                "\t\"type\"\tTEXT,\n" +
                "\t\"compartment\"\tTEXT,\n" +
                "\t\"description\"\tTEXT,\n" +
                "\t\"metaData\"\tBLOB,\n" +
                "\t\"note\"\tBLOB,\n" +
                "\t\"hasOnlySubstanceUnits\"\tNUMERIC,\n" +
                "\t\"boundaryCondition\"\tNUMERIC,\n" +
                "\t\"constant\"\tNUMERIC,\n" +
                "\t\"metaId\"\tTEXT,\n" +
                "\t\"networkId\"\tTEXT,\n" +
                "\t\"substanceUnits\"\tTEXT,\n" +
                "\t\"conversionFactor\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"speciesId\")\n" +
                ");";

        String sqlNewReactionTable = "CREATE TABLE \"reactions\" (\n" +
                "\t\"reactionId\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"name\"\tTEXT,\n" +
                "\t\"sbo\"\tTEXT,\n" +
                "\t\"description\"\tTEXT,\n" +
                "\t\"metaData\"\tBLOB,\n" +
                "\t\"compartment\"\tTEXT,\n" +
                "\t\"type\"\tTEXT,\n" +
                "\t\"note\"\tBLOB,\n" +
                "\t\"networkId\"\tTEXT,\n" +
                "\t\"formula\"\tBLOB,\n" +
                "\t\"startPosX\"\tREAL,\n" +
                "\t\"startPosY\"\tREAL,\n" +
                "\t\"startPosZ\"\tREAL,\n" +
                "\t\"endPosX\"\tREAL,\n" +
                "\t\"endPosY\"\tREAL,\n" +
                "\t\"endPosZ\"\tREAL,\n" +
                "\t\"reversible\"\tNUMERIC,\n" +
                "\t\"metaId\"\tINTEGER,\n" +
                "\t\"weight\"\tREAL,\n" +
                "\t\"reactantsDNF\"\tBLOB,\n" +
                "\t\"productsDNF\"\tBLOB,\n" +
                "\t\"catalystsDNF\"\tBLOB,\n" +
                "\t\"inhibitorsDNF\"\tBLOB,\n" +
                "\tPRIMARY KEY(\"reactionId\")\n" +
                ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new species table
            stmt.execute(sqlNewSpeciesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new reaction table
            stmt.execute(sqlNewReactionTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //System.out.println("2 empty tables created.");
    }

    /**
     * get SQLite database content as readable string
     *
     * @return SQLite database tables as String
     */
    public String getDBString() {
        this.write("src/catbutler/resources/utilFiles/tmpParserOutput.tmpdb");

        try {
            return new String(Files.readAllBytes(Paths.get("src/catbutler/resources/utilFiles/tmpParserOutput.tmpdb")));
        } catch (IOException e) {
            return "";
        }
    }

}
