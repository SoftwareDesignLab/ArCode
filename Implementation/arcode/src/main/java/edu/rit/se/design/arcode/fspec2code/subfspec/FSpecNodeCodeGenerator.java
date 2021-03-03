package edu.rit.se.design.arcode.fspec2code.subfspec;

import edu.rit.se.design.arcode.fspec2code.AbstractNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.fspec.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class FSpecNodeCodeGenerator<T extends FSpecNode> extends AbstractNodeCodeGenerator<FSpec, FSpecNode, T> {

    protected String findDirectedGraphNodeClassName(FSpecNode candidateFSpecNode){
        String candidateFSpecNodeClass = null;
        if( candidateFSpecNode instanceof FSpecAPICallNode )
            candidateFSpecNodeClass = ((FSpecAPICallNode)candidateFSpecNode).getReturnType();
        else
        if( candidateFSpecNode instanceof FSpecAPIInstantiationNode )
            candidateFSpecNodeClass = ((FSpecAPIInstantiationNode)candidateFSpecNode).getFullClassName();

        if( !candidateFSpecNodeClass.startsWith("L") )
            candidateFSpecNodeClass = "L" + candidateFSpecNodeClass;
        return candidateFSpecNodeClass;
    }

    protected boolean considerDirectedGraphNodeForArgumentMap( FSpecNode node ){
        return node instanceof FSpecAPINode;
    }

    protected boolean definesSymbol(FSpecNode fSpecNode ){
        return fSpecNode instanceof FSpecAPIInstantiationNode ||
                (fSpecNode instanceof FSpecAPICallNode && !((FSpecAPICallNode) fSpecNode).getReturnType().equals("V")) ;
    }
}
