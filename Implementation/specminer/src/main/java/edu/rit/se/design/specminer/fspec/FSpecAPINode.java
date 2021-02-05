package edu.rit.se.design.specminer.fspec;

import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public interface FSpecAPINode extends FSpecNode{
    public String getFullClassName();
    public String getSimpleClassName();
    public List<String> getArgumentTypes();
}
