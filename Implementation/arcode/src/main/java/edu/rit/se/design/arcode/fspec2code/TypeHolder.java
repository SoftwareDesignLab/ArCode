package edu.rit.se.design.arcode.fspec2code;
/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class TypeHolder{
    String type;

    public TypeHolder(String type) {
        this.type = type;
    }


    public String getType() {
/*
            if( type.equals("I") )
                return "int";
            if( type.equals("B") )
                return "boolean";

*/
        return type;
    }

    public String getSimpleName(){
        return getType().replaceAll(".*\\.", "").replaceAll("\\$", ".");
    }
}
