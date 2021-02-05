package edu.rit.se.design.fspec2code;

import edu.rit.se.design.specminer.fspec.FSpecAPICallNode;
import edu.rit.se.design.specminer.fspec.FSpecAPIInstantiationNode;
import edu.rit.se.design.specminer.fspec.FSpecNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class SymbolTable {
    final static String UNKNOWN_VAR_NAME = "var";
    Map<String, Integer> varNameCounterMap = new HashMap<>();
    Map<Object, String> objectSymbolMap = new HashMap<>();


    public String getSymbol( Object object ){
        return objectSymbolMap.get( object );
    }

    public Set<Object> getAllObjects(){
        return objectSymbolMap.keySet();
    }

    public String createSymbol( FSpecNode fSpecNode ){
        return createSymbolForAllObjects( fSpecNode );
    }

    public String createSymbol( String classFullName ){
        return createSymbolForAllObjects( new TypeHolder( classFullName ) );
    }

    String createSymbolForAllObjects( Object object ){
        String symbol = getSymbol(  object );
        if( symbol != null )
            return symbol;
        String symbolName = generateVarName( object );

        varNameCounterMap.put( symbolName,
                Integer.valueOf((varNameCounterMap.get( symbolName ) != null ? varNameCounterMap.get( symbolName ) : -1) + 1 ) );

        if( varNameCounterMap.get( symbolName ) > 0 ) {
            symbolName += "_" + varNameCounterMap.get(symbolName);
        }

        objectSymbolMap.put( object, symbolName );
        return objectSymbolMap.get( object );
    }


    class TypeHolder{
        String type;

        public TypeHolder(String type) {
            this.type = type;
        }

        public String getType() {
/*
            if( type.equals("I") )
                return "int";
            if( type.equals("B") )
                return "boolean";

*/
            return type;
        }

        public String getSimpleName(){
            return getType().replaceAll(".*\\.", "").replaceAll("\\$", ".");
        }
    }

    String generateVarName( Object object ){
        //TODO: refactor this part. It should go inside each FSpecNodeCodeGenerator
        String varName = null;
        if( object instanceof FSpecAPICallNode)
            varName = ((FSpecAPICallNode) object).getReturnType().replaceAll(".*\\/", "");
        if( object instanceof FSpecAPIInstantiationNode)
            varName = ((FSpecAPIInstantiationNode) object).getSimpleClassName();

        if( /*!(object instanceof edu.rit.se.design.specminer.fspec.FSpecAPINode)*/ varName == null && object instanceof TypeHolder )
            varName = ((TypeHolder)object).getSimpleName();
        if(varName.equals("Class"))
            varName = "clazz";

        if(varName.equals("int"))
            varName = "i";

        if(varName.equals("boolean"))
            varName = "b";

        varName = varName.replaceAll("\\.", "_");
        return varName.substring(0,1).toLowerCase() + varName.substring(1);
    }
}
