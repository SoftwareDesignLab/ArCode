package edu.rit.se.design.specminer.ifd;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public abstract class MethodPreCondition {
    public enum PreConditionType{EQUAL, GREATER_THAN, LESS_THAN, NOT_EQUAL}
    protected int methodArgumentIndex;
    protected PreConditionType preConditionType;

    public MethodPreCondition(int methodArgumentIndex, PreConditionType preConditionType) {
        this.methodArgumentIndex = methodArgumentIndex;
    }

    public int getMethodArgumentIndex() {
        return methodArgumentIndex;
    }

    public void setMethodArgumentIndex(int methodArgumentIndex) {
        this.methodArgumentIndex = methodArgumentIndex;
    }

    public PreConditionType getPreConditionType() {
        return preConditionType;
    }

    public void setPreConditionType(PreConditionType preConditionType) {
        this.preConditionType = preConditionType;
    }
}
