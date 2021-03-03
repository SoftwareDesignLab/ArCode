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

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;

import java.util.ArrayList;
import java.util.List;

public class MapoCodeAnalyzer{
    CodeRepository codeRepository;
    List<MapoApiCallSeq> allMapoApiCallSequences;
    public MapoCodeAnalyzer(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
        populateMapoApiCallSequences();
    }


    public CodeRepository getCodeRepository() {
        return codeRepository;
    }

    public void setCodeRepository(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    void populateMapoApiCallSequences(){
        // TODO: In the case that there are more than one sequence in a inner-lined caller site, we need to choose those who contain
        // all the APIs called in that inner-lined call site.
        allMapoApiCallSequences = new ArrayList<>();
        getCodeRepository().getApiSeqUsageInPrograms().values().forEach( apiSequenceRepresentations ->
                apiSequenceRepresentations.forEach( apiSequenceRepresentation -> allMapoApiCallSequences.add(
                        new MapoApiCallSeq( apiSequenceRepresentation ))));
    }

    public List<MapoApiCallSeq> getAllMapoApiCallSequences() {
        return allMapoApiCallSequences;
    }

    public void setAllMapoApiCallSequences(List<MapoApiCallSeq> allMapoApiCallSequences) {
        this.allMapoApiCallSequences = allMapoApiCallSequences;
    }

    public String report(){
        StringBuilder stringBuilder = new StringBuilder( "MAPO Code Analyzer report:" );
        getAllMapoApiCallSequences().forEach( mapoApiCallSeq -> stringBuilder.append( "\n" + mapoApiCallSeq.report() ) );
        return stringBuilder.toString();
    }
}
