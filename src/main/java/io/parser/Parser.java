package io.parser;

import io.documents.Doc;

public abstract class Parser {

    private final Doc doc;

    public Parser(Doc doc) {
        this.doc = doc;
    }

    public abstract void parseLine(String s);
}
