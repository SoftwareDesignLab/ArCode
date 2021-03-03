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

import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiSequenceRepresentation {
    List<ApiRepresentation> apiRepresentationList;
    GRAAM graam;
    public ApiSequenceRepresentation(List<ApiRepresentation> apiRepresentationList){
        setApiRepresentationList( new ArrayList<>() );
        getApiRepresentationList().addAll( apiRepresentationList );
//        setApiRepresentationList( apiRepresentationList );
    }

    public ApiSequenceRepresentation(){
        setApiRepresentationList( new ArrayList<>());
    }


    public GRAAM getGraam() {
        return graam;
    }

    public void setGraam(GRAAM graam) {
        this.graam = graam;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        getApiRepresentationList().forEach( apiRepresentation -> stringBuilder.append( (stringBuilder.length() > 0 ? "→" : "") + apiRepresentation ) );
        return stringBuilder.toString();
    }

    public List<ApiRepresentation> getApiRepresentationList() {
        return apiRepresentationList;
    }

    public void setApiRepresentationList(List<ApiRepresentation> apiRepresentationList) {
        this.apiRepresentationList = apiRepresentationList;
    }

    public void addToLast(ApiRepresentation apiRepresentation ){
        getApiRepresentationList().add( apiRepresentation );
    }
    public void addAtFirst(ApiRepresentation apiRepresentation ){
        getApiRepresentationList().add( 0, apiRepresentation );
    }
    public boolean doesContain( ApiSequenceRepresentation apiSequenceRepresentation ){
        return Collections.indexOfSubList( getApiRepresentationList(), apiSequenceRepresentation.getApiRepresentationList() ) >= 0;
    }

    public boolean equals(Object object ){
        if( object == null || !(object instanceof ApiSequenceRepresentation) )
            return false;
        ApiSequenceRepresentation other = (ApiSequenceRepresentation) object;
        return other.getApiRepresentationList().equals( getApiRepresentationList() );
    }

    public int hashCode(){
        return 0;
    }

    public ApiSequenceRepresentation clone(){
        ApiSequenceRepresentation cloned = new ApiSequenceRepresentation();
        getApiRepresentationList().forEach( apiRepresentation -> cloned.addToLast( apiRepresentation ) );
        return cloned;
    }
}
