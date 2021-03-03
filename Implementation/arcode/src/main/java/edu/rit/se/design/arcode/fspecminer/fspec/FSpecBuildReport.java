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

import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class FSpecBuildReport {
    List<Pair<Integer, Integer>> fSpecNodeCountGraamNodeCountList;

    public FSpecBuildReport() {
        this.fSpecNodeCountGraamNodeCountList = new ArrayList<>();
    }

    public void addLog(FSpec fSpec, GRAAM graam){
        fSpecNodeCountGraamNodeCountList.add( new ImmutablePair<>( fSpec.getNumberOfNodes(), graam.getNumberOfNodes() ) );
    }

    public StringBuilder report(){
        StringBuilder report = new StringBuilder();
        fSpecNodeCountGraamNodeCountList.forEach( pair -> report.append( "\t" + pair.getKey() ) );
        report.append("\n");
        fSpecNodeCountGraamNodeCountList.forEach( pair -> report.append( "\t" + pair.getValue() ) );
        return report;
    }


}
