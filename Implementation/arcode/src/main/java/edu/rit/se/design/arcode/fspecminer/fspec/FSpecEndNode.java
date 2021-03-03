package edu.rit.se.design.arcode.fspecminer.fspec;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecEndNode implements FSpecNode{
    @Override
    public String getTitle() {
        return "End Node";
    }

    @Override
    public FSpecEndNode clone() {
        return new FSpecEndNode();
    }
}
