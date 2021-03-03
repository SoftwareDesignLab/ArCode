/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo;

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.ApiSequenceRepresentation;

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
