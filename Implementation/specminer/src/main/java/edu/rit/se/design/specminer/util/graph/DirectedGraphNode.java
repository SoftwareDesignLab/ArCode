package edu.rit.se.design.specminer.util.graph;

import java.io.Serializable;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public interface DirectedGraphNode extends Serializable {
    public String getTitle();
    public DirectedGraphNode clone();
}
