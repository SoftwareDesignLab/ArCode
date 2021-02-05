package edu.rit.se.design.specminer.graam;

import edu.rit.se.design.specminer.util.graph.DirectedGraphEdgeType;

import java.io.Serializable;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public enum GRAAMEdgeType implements DirectedGraphEdgeType {
    EXPLICIT_DATA_DEP, IMPLICIT_DATA_DEP
}
