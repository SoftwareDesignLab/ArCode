package edu.rit.se.design.specminer.graam;

import edu.rit.se.design.specminer.util.graph.DirectedGraphEdgeType;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public enum PrimaryAPIUsageGraphEdgeType implements DirectedGraphEdgeType {
    SEQUENCE_DEPENDENCY,
    DATA_DEPENDENCY
}
