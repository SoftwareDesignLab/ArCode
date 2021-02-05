package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.fspec.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecNodeCodeGeneratorFactory {
    static Map<String, FSpecNodeCodeGenerator> nodeCodeGeneratorMap = new HashMap<>();
    static final String BOUNDARY_NODE = "boundaryNode";
    static final String API_INSTANTIATION = "apiInstantiation";
    static final String API_CALL = "apiCall";


    public static FSpecNodeCodeGenerator getFSpecNodeCodeGenerator(FSpecNode fSpecNode){
        if( fSpecNode instanceof FSpecEndNode || fSpecNode instanceof FSpecStartNode) {
            if( nodeCodeGeneratorMap.get(BOUNDARY_NODE) == null )
                nodeCodeGeneratorMap.put( BOUNDARY_NODE, new FSpecBoundaryNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(BOUNDARY_NODE);
        }
        if( fSpecNode instanceof FSpecAPIInstantiationNode){
            if( nodeCodeGeneratorMap.get(API_INSTANTIATION) == null )
                nodeCodeGeneratorMap.put( API_INSTANTIATION, new FSpecApiInstantiationNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(API_INSTANTIATION);
        }
        if( fSpecNode instanceof FSpecAPICallNode){
            if( nodeCodeGeneratorMap.get(API_CALL) == null )
                nodeCodeGeneratorMap.put( API_CALL, new FSpecAPICallNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(API_CALL);
        }
        return null;
    }
}
