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

package edu.rit.se.design.arcode.fspecminer.fspec;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphEdgeInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecEdge implements DirectedGraphEdgeInfo {
    int frequency;
    Set<String> projectNames = new HashSet<>();
    public FSpecEdge(String projectName){
        frequency = 1;
        projectNames.add( projectName );
    }
    public FSpecEdge( int frequency ){
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void addFrequencyBy( int increment, String projectName ){
        frequency += increment;
        projectNames.add( projectName );
    }

    @Override
    public String getTitle() {
        StringBuilder projectNameTitles = new StringBuilder();
        projectNames.forEach( s -> projectNameTitles.append( (projectNameTitles.length() > 0 ? ", " : "") + s ) );

        return String.valueOf(frequency) + "[" + projectNameTitles + "]";
    }
}
