package edu.rit.se.design.patternMining.mapo;

import edu.rit.se.design.utils.clustering.ClusterNode;

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
