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

package edu.rit.se.design.arcode.fspec2recom.patternMining.generic;

import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;

public class ApiRepresentation implements Comparable{
    FrameworkRelatedNode graamNode;
    public ApiRepresentation(FrameworkRelatedNode graamNode){
        setFrameworkRelatedNode( graamNode );
    }

    public String toString() {
        return getSimpleTitle( getFrameworkRelatedNode() );
    }

    private String getSimpleTitle( FrameworkRelatedNode graamNode ){
        String label = graamNode.getTitle();
/*        if( graamNode instanceof GraamFrameworkNode)
            label += " : " + ((GraamFrameworkNode)graamNode).getFrameworkRelatedNodeOrigin();*/
        label = label.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","");
        label = label.indexOf( "(" ) >= 0? label.substring(0, label.indexOf( "(" ) ) : label;
        label = (label.startsWith("init ") ? label.substring(5, label.length()) + " instantiation" : label);
        return label;
    }

    public FrameworkRelatedNode getFrameworkRelatedNode() {
        return graamNode;
    }

    public void setFrameworkRelatedNode(FrameworkRelatedNode graamNode) {
        this.graamNode = graamNode;
    }

    public boolean equals(Object object ){
        if( object == null || !(object instanceof ApiRepresentation) )
            return false;
        ApiRepresentation other = (ApiRepresentation) object;
        if(GRAAMBuilder.areSemanticallyTheSame( other.getFrameworkRelatedNode(), getFrameworkRelatedNode() ) ) {
            return true;
        }
        return false;
    }

    public int hashCode(){
        return 0;
    }

    @Override
    public int compareTo(Object o) {
        if( o == null || !(o instanceof ApiRepresentation) )
            return 1;

        return toString().compareTo( o.toString() );
    }

    public String getCallerClassName(){
        String className = "";
        if( getFrameworkRelatedNode() instanceof FrameworkRelatedNode) {
//            String graamNodeOrigin = ((FrameworkRelatedNode) getFrameworkRelatedNode()).getOriginClass();
//            className = graamNodeOrigin.substring(0, graamNodeOrigin.lastIndexOf("."));

            className = ((FrameworkRelatedNode) getFrameworkRelatedNode()).getOriginClass();

        }
        return className;
    }
    public String getCallerMethodName(){
        String methodName = "";
        if( getFrameworkRelatedNode() instanceof FrameworkRelatedNode) {
//            String graamNodeOrigin = ((FrameworkRelatedNode) getFrameworkRelatedNode()).getOriginMethod();
//            methodName = graamNodeOrigin.substring(graamNodeOrigin.lastIndexOf(".")+1, graamNodeOrigin.length());

            methodName = ((FrameworkRelatedNode) getFrameworkRelatedNode()).getOriginMethod();

        }
        return methodName;
    }
}
