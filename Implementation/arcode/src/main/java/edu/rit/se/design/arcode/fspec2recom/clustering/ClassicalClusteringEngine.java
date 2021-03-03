package edu.rit.se.design.arcode.fspec2recom.clustering;

import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.ClusterNodeApiSeq;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassicalClusteringEngine<T> {

    // Finds the max similarity between two cluster nodes
    double computeSimilarity(ClusterNode clusterNode1, ClusterNode clusterNode2 ){
        List<T> clusterNode1Elements = clusterNode1.getAllElementsInCluster();
        List<T> clusterNode2Elements = clusterNode2.getAllElementsInCluster();
        double maxSimilarity = -1;
        for( T clusterNode1Element: clusterNode1Elements )
            for( T clusterNode2Element: clusterNode2Elements ) {
                double similarity = computeSimilarity( clusterNode1Element, clusterNode2Element );
                if ( maxSimilarity < similarity )
                    maxSimilarity = similarity;
            }
        return maxSimilarity;
    }

    protected abstract double computeSimilarity(T clusterNodeElement1, T clusterNodeElement2);

    protected abstract ClusterNode<T> createClusterNodeForElement(T element);


    public ClusterNode binaryCluster(List<T> elements ){
        List<ClusterNode> clusterNodeList = new ArrayList<>();
        elements.forEach( element -> clusterNodeList.add( createClusterNodeForElement(element) ) );

        while( clusterNodeList.size() > 1 ){
            int clusterSize = clusterNodeList.size();
            double maxSimilarityAmount = -1;
            int maxSimilarityIndex1 = -1;
            int maxSimilarityIndex2 = -1;

            for( int i = 0; i < clusterSize && maxSimilarityAmount < 1; i++ ) {
                ClusterNode node1 = clusterNodeList.get(i);
                for (int j = i + 1; j < clusterSize && maxSimilarityAmount < 1; j++) {
                    ClusterNode node2 = clusterNodeList.get(j);
                    double similarity = computeSimilarity( node1, node2 );
                    if( similarity > maxSimilarityAmount ){
                        maxSimilarityAmount = similarity;
                        maxSimilarityIndex1 = i;
                        maxSimilarityIndex2 = j;
                    }
                }
            }

            if( maxSimilarityIndex1 >= 0 && maxSimilarityIndex2 >= 0 ){
                ClusterNode newClusterNode = new ClusterNodeCluster(clusterNodeList.get(maxSimilarityIndex1),
                        clusterNodeList.get( maxSimilarityIndex2 ));
                clusterNodeList.remove( maxSimilarityIndex1 );
                clusterNodeList.remove( maxSimilarityIndex2 - 1 );
                clusterNodeList.add( newClusterNode );
            }
        }
        return clusterNodeList.get(0);

    }

/*    public String similarityReport( List<MapoApiCallSeq> mapoApiCallSeqList ){
        StringBuilder stringBuilder = new StringBuilder( "MAPO Clustering Engine report:" );
        for( int i = 0; i < mapoApiCallSeqList.size(); i++ )
            for( int j = i+1; j < mapoApiCallSeqList.size(); j++ )
                stringBuilder.append( "\n(" + i + "," + j + "): " + computeSimilarity( mapoApiCallSeqList.get(i), mapoApiCallSeqList.get(j) )   );
        return stringBuilder.toString();
    }*/

    public List<ClusterNode> toClusters(ClusterNode clusterNode, int clusterNumber ){
        List<ClusterNode> clusters = new ArrayList<>();
        clusters.add( clusterNode );
        int clustersSize = clusters.size();
        while( clusters.size() < clusterNumber || clusters.size() == clustersSize ){
            clustersSize = clusters.size();
            List<ClusterNode> toBeAddedClusterNodes = new ArrayList<>();
            clusters.forEach( clusterNode1 -> {
                if( clusterNode1 instanceof ClusterNodeApiSeq)
                    toBeAddedClusterNodes.add( clusterNode1 );
                else{ // is instance of ClusterNodeCluster
                    toBeAddedClusterNodes.addAll( ((ClusterNodeCluster)clusterNode1).getClusterNodeList() );
                }
            } );
            clusters.clear();
            clusters.addAll( toBeAddedClusterNodes );
        }
        return clusters;
    }

}
