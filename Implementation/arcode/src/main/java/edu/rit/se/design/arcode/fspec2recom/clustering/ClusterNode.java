package edu.rit.se.design.arcode.fspec2recom.clustering;

import java.util.List;

public interface ClusterNode<T> {
    List<T> getAllElementsInCluster();
    int getNumberOfElements();
}
