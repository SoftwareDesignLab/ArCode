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

package edu.rit.se.design.arcode.fspec2recom.clustering;

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
