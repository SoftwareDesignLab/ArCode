package edu.rit.se.design.arcode.fspec2code.subfspec;

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecBoundaryNodeCodeGenerator extends FSpecNodeCodeGenerator<FSpecNode>{
    @Override
    public String generateCode(FSpec fSpec, FSpecNode fSpecNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) {
        return "";
    }
}
