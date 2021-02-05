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
