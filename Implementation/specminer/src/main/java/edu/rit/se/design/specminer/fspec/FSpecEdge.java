package edu.rit.se.design.specminer.fspec;

import edu.rit.se.design.specminer.util.graph.DirectedGraphEdgeInfo;

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
