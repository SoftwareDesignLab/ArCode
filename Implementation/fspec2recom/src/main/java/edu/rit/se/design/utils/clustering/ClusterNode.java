package edu.rit.se.design.utils.clustering;

import java.util.List;

public interface ClusterNode<T> {
    List<T> getAllElementsInCluster();
    int getNumberOfElements();
}
