package edu.rit.se.design.arcode.fspec2code.subfspec;

import edu.rit.se.design.arcode.fspec2code.*;
import edu.rit.se.design.arcode.fspec2recom.SubFSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;


/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SubFSpecCodeGenerator extends AbstractCodeGenerator<FSpec, FSpecNode> {

    public SubFSpecCodeGenerator(ClassHierarchyUtil classHierarchyUtil ){
        super(classHierarchyUtil);
    }

    protected Pair getClassNameVarName(Object o, SymbolTable symbolTable ){
        String className = null;
        String varName = null;
        if( o instanceof FSpecNode){
            if( o instanceof FSpecAPIInstantiationNode &&
                    ( ((FSpecAPIInstantiationNode) o).isAbstractOrInterface() || !((FSpecAPIInstantiationNode) o).isPublicConstructor() )){
                className = ((FSpecAPIInstantiationNode) o).getSimpleClassName();
                varName = symbolTable.getSymbol( o );
            }
        }
        else if( o instanceof TypeHolder ){
            className = ((TypeHolder) o).getSimpleName();
            varName = symbolTable.getSymbol( o );
        }

        return new ImmutablePair( className, varName );
    }

}
