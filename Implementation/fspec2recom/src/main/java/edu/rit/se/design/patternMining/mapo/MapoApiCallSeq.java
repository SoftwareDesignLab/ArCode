package edu.rit.se.design.patternMining.mapo;

import edu.rit.se.design.patternMining.generic.ApiSequenceRepresentation;

public class MapoApiCallSeq extends ApiSequenceRepresentation {
//    String className;
//    String methodName;
    public MapoApiCallSeq(ApiSequenceRepresentation apiSequenceRepresentation) {
        super(apiSequenceRepresentation.getApiRepresentationList());
//        setClassName( apiSequenceRepresentation.getApiRepresentationList().get(0).getCallerClassName() );
//        setMethodName( apiSequenceRepresentation.getApiRepresentationList().get(0).getCallerMethodName() );
        setGraam( apiSequenceRepresentation.getGraam() );
    }

    public String getClassName() {
        return getApiRepresentationList().isEmpty()? "NOTHING" : getApiRepresentationList().get(0).getCallerClassName();
    }

//    public void setClassName(String className) {
//        this.className = className;
//    }

    public String getMethodName() {
        return getApiRepresentationList().isEmpty()? "NOTHING" : getApiRepresentationList().get(0).getCallerMethodName() ;
    }

//    public void setMethodName(String methodName) {
//        this.methodName = methodName;
//    }

    public MapoApiCallSeq clone(){
        MapoApiCallSeq cloned = new MapoApiCallSeq(this);
//        getApiRepresentationList().forEach( apiRepresentation -> cloned.addToLast( apiRepresentation ) );
        return cloned;
    }


    public String report(){
        StringBuilder report = new StringBuilder( "Caller site:\t" );
        report.append( getClassName() + "." + getMethodName() + "\t" + getApiRepresentationList().size() + "\t");
//        report.append( " - " );
        getApiRepresentationList().forEach( apiRepresentation -> report.append( "->" + apiRepresentation.toString() ) );
        return report.toString();
    }
}
