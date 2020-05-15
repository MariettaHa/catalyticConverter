package logic;

import java.util.ArrayList;

public class BooleanTreeNode {

    int treeType = 0;
    private ArrayList<BooleanTreeNode> children = new ArrayList<>();
    private int level = -1;
    private String annotation = "";
    private BooleanTreeNode parent = null;
    private boolean isRoot = true;
    private String dnf = "";
    private String dnfWithCoeff = "";

    public BooleanTreeNode() {
    }

    /**
     * constructs booleanTreeNode
     *
     * @param level     level of node in its parent tree
     * @param string    label of node
     * @param treeType, 0: leaf node; 1: and-Node; 2: or-Node
     * @param parent,   parental booleanTreeNode
     */
    BooleanTreeNode(int level, String string, int treeType, BooleanTreeNode parent) {
        this.level = level;
        this.annotation = string;
        this.treeType = treeType;
        this.parent = parent;
    }

    /**
     * constructs booleanTreeNode that is root
     *
     * @param isRoot, should be true
     */
    BooleanTreeNode(boolean isRoot) {
        if (isRoot) {
            this.level = -1;
            this.annotation = "root";
            this.parent = this;
        }
    }

    void addChild(BooleanTreeNode child) {
        children.add(child);
    }

    public ArrayList<BooleanTreeNode> getChildren() {
        return children;
    }

    public String getDnf() {
        return dnf;
    }

    public void setDnf(String dnf) {
        this.dnf = dnf;
    }

    public String getDnfWithCoeff() {
        return dnfWithCoeff;
    }

    public void setDnfWithCoeff(String dnfStrWithCoefficients) {
        this.dnfWithCoeff = dnfStrWithCoefficients;
    }

}
