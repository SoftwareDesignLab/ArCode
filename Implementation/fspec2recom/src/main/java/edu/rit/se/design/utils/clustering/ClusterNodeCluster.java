package edu.rit.se.design.utils.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterNodeCluster<T> implements ClusterNode<T> {
    List<ClusterNode> clusterNodeList = new ArrayList<>();

    public ClusterNodeCluster(ClusterNode clusterNode1, ClusterNode clusterNode2) {
        clusterNodeList.add( clusterNode1 );
        clusterNodeList.add( clusterNode2 );
    }


    @Override
    public List<T> getAllElementsInCluster() {
        List<T> list = new ArrayList<>();

        List<ClusterNode> sortedClusterNodeList = clusterNodeList.stream()
                .sorted(Comparator.comparingInt(ClusterNode::getNumberOfElements))
                .collect(Collectors.toList());

        sortedClusterNodeList.forEach( clusterNode -> list.addAll( clusterNode.getAllElementsInCluster() ) );
        return list;
    }

    @Override
    public int getNumberOfElements() {
        int clusterSize = 0;
        for(ClusterNode clusterNode: clusterNodeList)
            clusterSize += clusterNode.getNumberOfElements();
        return clusterSize;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        clusterNodeList.forEach(clusterNode -> stringBuilder.append( (stringBuilder.length() > 0 ? ", " : "" )+ clusterNode.toString() ));
        return "{" + stringBuilder.toString() + "}";
    }

    public List<ClusterNode> getClusterNodeList() {
        return clusterNodeList;
    }

    public void setClusterNodeList(List<ClusterNode> clusterNodeList) {
        this.clusterNodeList = clusterNodeList;
    }
}
