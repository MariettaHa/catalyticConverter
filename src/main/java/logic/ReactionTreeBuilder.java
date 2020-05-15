package logic;

import io.parser.CustomParser;
import model.Reaction;

import java.util.ArrayList;

public class ReactionTreeBuilder {

    private Reaction reaction;
    private String parserType;
    private String reactantsStr;
    private String modifiersStr;
    private String catalystsStr;
    private String inhibitorsStr;
    private String productsStr;
    private String reactantsStrCoeff;
    private String catalystsStrCoeff;
    private String inhibitorsStrCoeff;
    private String productsStrCoeff;
    private ArrayList<String> stringsWithCoefficients;

    /**
     * constructs builder for (boolean) reaction trees
     *
     * @param parserType               type of parser: wim, crs, sbml, or db
     * @param reaction                 respective reaction instance
     * @param reactantsStr,            reactants boolean equation
     * @param modifiersStr,            modifiers boolean equation
     * @param productsStr,             products boolean equation
     * @param stringsWithCoefficients, list of equations containing coefficients
     */
    public ReactionTreeBuilder(String parserType, Reaction reaction, String reactantsStr,
                               String modifiersStr, String productsStr, ArrayList<String> stringsWithCoefficients) {
        this.parserType = parserType;
        this.reaction = reaction;
        this.reactantsStr = reactantsStr;
        this.modifiersStr = modifiersStr;
        this.productsStr = productsStr;
        this.stringsWithCoefficients = stringsWithCoefficients;

        initCoefficientStrings();
        defineStrings();
    }

    /**
     * processes equations containing coefficients
     */
    private void initCoefficientStrings() {
        for (String s : stringsWithCoefficients) {
            String[] strSplit = s.split("::");

            if (strSplit.length > 1) {
                switch (strSplit[0]) {
                    case "reactants":
                        reactantsStrCoeff = strSplit[1];
                        break;
                    case "products":
                        productsStrCoeff = strSplit[1];
                        break;
                    case "catalysts":
                        catalystsStrCoeff = strSplit[1];
                        break;
                    case "inhibitors":
                        inhibitorsStrCoeff = strSplit[1];
                        break;
                }
            }
        }
    }

    /**
     * processes equations
     */
    private void defineStrings() {
        if (parserType.matches("sbml|wim|db|crs|table")) {
            productsStr = productsStr.replaceAll("\\s+", "").replaceAll("\\s+\\s*", "\\s");
            reactantsStr = reactantsStr.replaceAll("\\s+", "").replaceAll("\\s+\\s*", "\\s");
            catalystsStr = "";
            inhibitorsStr = "";
            String[] modStrSplit = modifiersStr.split("\\t");

            if (!modStrSplit[0].equals("noCata")) {
                catalystsStr = modStrSplit[0].strip().replaceAll("\\s?&\\s?", "&").replaceAll("\\s", ",").replaceAll("\\s+\\s*", "\\s");
            }
            if (!modStrSplit[1].equals("noInhib")) {
                inhibitorsStr = modStrSplit[1].strip().replaceAll("\\s?&\\s?", "&").replaceAll("\\s", ",").replaceAll("\\s+\\s*", "\\s");
            }
        }
    }

    /**
     * builds trees for each four equations
     */
    public void buildTrees() {
        reaction.setReactantsTree(buildTree(reactantsStr, reactantsStrCoeff));
        reaction.setProductsTree(buildTree(productsStr, productsStrCoeff));

        if (catalystsStr != null) {
            if (!catalystsStr.isBlank()) {
                reaction.setCatalystsTree(buildTree(catalystsStr, catalystsStrCoeff));
            }
        }

        if (inhibitorsStr != null) {
            if (!inhibitorsStr.isBlank()) {
                reaction.setInhibitorsTree(buildTree(inhibitorsStr, inhibitorsStrCoeff));
            }
        }
    }

    /**
     * builds tree
     *
     * @param equation respective part of formula without coefficients
     * @param equation with coefficients
     */
    private BooleanTreeNode buildTree(String equation, String equationWithCoefficients) {
        BooleanTreeNode root = new BooleanTreeNode(true);
        String dnfStr = "";
        String dnfStrWithCoefficients = "";

        if (equation != null) {
            if (!equation.isBlank()) {
                dnfStr = DisjunctiveNormalForm.compute(equation);
                if (equationWithCoefficients != null) {
                    if (!equationWithCoefficients.isBlank()) {
                        dnfStrWithCoefficients = DisjunctiveNormalForm.compute(equationWithCoefficients);
                    } else {
                        dnfStrWithCoefficients = DisjunctiveNormalForm.compute(equation);
                    }
                } else {
                    dnfStrWithCoefficients = DisjunctiveNormalForm.compute(equation);
                }
            }
        }

        if (dnfStr.matches("[,&A-Za-z0-9-_/().'% ]+")) {
            root.setDnf(dnfStr);
            root.setDnfWithCoeff(dnfStrWithCoefficients);
            //split by or
            if (dnfStr.split(",").length == 1) {
                //split by and
                if (dnfStr.split("&").length == 1) {
                    //break. rootNode is leafNode or has no children.
                    root.addChild(new BooleanTreeNode(1, dnfStr, 0, root));
                } else {
                    BooleanTreeNode andTreeNode = new BooleanTreeNode(1, dnfStr, 1, root);
                    root.addChild(andTreeNode);

                    for (String conj : dnfStr.split("&")) {
                        andTreeNode.addChild(new BooleanTreeNode(2, conj, 0, andTreeNode));
                    }
                }
            } else {
                BooleanTreeNode orTreeNode = new BooleanTreeNode(1, dnfStr, 2, root);
                root.addChild(orTreeNode);

                for (String disj : dnfStr.split(",")) {
                    if (disj.split("&").length == 1) {
                        //break. rootNode is leafNode or has no children.
                        orTreeNode.addChild(new BooleanTreeNode(2, disj, 0, orTreeNode));

                    } else {
                        BooleanTreeNode andTreeNode = new BooleanTreeNode(2, disj, 1, orTreeNode);
                        orTreeNode.addChild(andTreeNode);
                        for (String conj : disj.split("&")) {
                            andTreeNode.addChild(new BooleanTreeNode(3, conj, 0, andTreeNode));
                        }
                    }
                }
            }
        } else {
            CustomParser.isInvalid = true;
            System.err.println(equation + " could not be parsed to DNF");
        }

        return root;
    }

}
