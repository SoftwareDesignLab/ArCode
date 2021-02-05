package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo;

import edu.rit.se.design.arcode.fspec2recom.clustering.ClusterNode;

import java.util.ArrayList;
import java.util.List;

public class ClusterNodeApiSeq implements ClusterNode<MapoApiCallSeq> {
    MapoApiCallSeq mapoApiCallSeq;

    public ClusterNodeApiSeq(MapoApiCallSeq mapoApiCallSeq) {
        this.mapoApiCallSeq = mapoApiCallSeq;
    }

    @Override
    public List<MapoApiCallSeq> getAllElementsInCluster() {
        List<MapoApiCallSeq> list = new ArrayList<>();
        list.add( mapoApiCallSeq );
        return list;
    }

    @Override
    public int getNumberOfElements() {
        return 1;
    }

    public String toString(){
        return "{" + mapoApiCallSeq.toString() + "}";
    }
}
