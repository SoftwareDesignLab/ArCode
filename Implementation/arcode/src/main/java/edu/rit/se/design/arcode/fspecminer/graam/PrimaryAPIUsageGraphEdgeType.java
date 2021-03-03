package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphEdgeType;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public enum PrimaryAPIUsageGraphEdgeType implements DirectedGraphEdgeType {
    SEQUENCE_DEPENDENCY,
    DATA_DEPENDENCY
}
