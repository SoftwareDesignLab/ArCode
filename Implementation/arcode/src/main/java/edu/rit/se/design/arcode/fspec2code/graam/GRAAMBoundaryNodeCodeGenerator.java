package edu.rit.se.design.arcode.fspec2code.graam;

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspec2code.subfspec.FSpecNodeCodeGenerator;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
* @author Ali Shokri (as8308@rit.edu)
*/   
public class GRAAMBoundaryNodeCodeGenerator extends GRAAMNodeCodeGenerator<DirectedGraphNode> {
    @Override
    public String generateCode(GRAAM graam, DirectedGraphNode graamNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) {
        return "";
    }
}