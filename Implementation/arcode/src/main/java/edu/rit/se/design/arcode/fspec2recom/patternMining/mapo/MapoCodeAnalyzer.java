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
