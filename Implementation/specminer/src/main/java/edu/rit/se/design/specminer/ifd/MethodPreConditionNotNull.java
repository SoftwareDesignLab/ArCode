package edu.rit.se.design.specminer.ifd;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class MethodPreConditionNotNull extends MethodPreCondition{
    public MethodPreConditionNotNull(int methodArgumentIndex) {
        super(methodArgumentIndex, PreConditionType.NOT_EQUAL);
    }
    
}
