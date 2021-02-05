package edu.rit.se.design.patternMining.mapo;

import edu.rit.se.design.patternMining.frequentItemset.FrequentPatternSpecification;
import edu.rit.se.design.patternMining.frequentItemset.FrequentSequencePattern;
import edu.rit.se.design.patternMining.frequentItemset.FrequentSequencePatternMiner;
import edu.rit.se.design.patternMining.generic.ApiRepresentation;
import edu.rit.se.design.patternMining.generic.ApiSequenceRepresentation;
import edu.rit.se.design.patternMining.generic.CodeRepository;
import edu.rit.se.design.utils.clustering.ClusterNode;

import java.util.*;

public class MapoApiSeqCallMiner {

    Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns = new HashMap<>();
    double support = 0;
    public  void findPatterns(List<ClusterNode> clusters, double minSupport){
        foundPatterns.clear();
        support = minSupport;
        for( int i = 0; i < clusters.size(); i++ )
        {
            ClusterNode cluster = clusters.get(i);
            FrequentPatternSpecification frequentPatternSpecification = createPatternSpecification(
                    (int) Math.ceil(cluster.getAllElementsInCluster().size() * minSupport));
            CodeRepository codeRepository = createCodeReopsitory( cluster );
            MapoFrequentSequencePatterMiner mapoFrequentSequencePatterMiner = new MapoFrequentSequencePatterMiner();
            mapoFrequentSequencePatterMiner.findPatterns( codeRepository, frequentPatternSpecification );
            foundPatterns.put( String.valueOf(i), mapoFrequentSequencePatterMiner.getFoundPatterns() );
        }
    }



    FrequentPatternSpecification createPatternSpecification( int support ){
        return new FrequentPatternSpecification(support, false);
    }

    CodeRepository createCodeReopsitory( ClusterNode<MapoApiCallSeq> cluster ){
        CodeRepository codeRepository = new CodeRepository();
        Map<String, Set<ApiSequenceRepresentation>> apiSeqSetInClusterMap = new HashMap<>();
        for(int i = 0; i < cluster.getAllElementsInCluster().size(); i++ ){
            MapoApiCallSeq apiSeqInCluster = cluster.getAllElementsInCluster().get(i);
            if( apiSeqSetInClusterMap.get( String.valueOf(i) ) == null )
                apiSeqSetInClusterMap.put( String.valueOf(i), new HashSet<>());
            int finalI = i;
            apiSeqSetInClusterMap.get(String.valueOf(finalI)).add( apiSeqInCluster ) ;
        }
        codeRepository.setApiSeqUsageInPrograms( apiSeqSetInClusterMap );

        Map<String, Set<ApiRepresentation>> apiSetInClusterMap = new HashMap<>();
        Set<ApiRepresentation> apiSetInRepository = new HashSet<>();
        apiSeqSetInClusterMap.forEach((s, apiSequenceRepresentations) -> {
            if( apiSetInClusterMap.get( s ) == null )
                apiSetInClusterMap.put( s, new HashSet<>() );
            apiSequenceRepresentations.forEach(apiSequenceRepresentation -> {
                apiSequenceRepresentation.getApiRepresentationList().forEach( apiRepresentation -> {
                    apiSetInClusterMap.get(s).add( apiRepresentation );
                    apiSetInRepository.add( apiRepresentation );
                } );
            });
        });

        codeRepository.setApiSetInRepository( apiSetInRepository );
        codeRepository.setApiUsageInPrograms( apiSetInClusterMap );

        return codeRepository;
    }

    public Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> getFoundPatterns() {
        return foundPatterns;
    }
/*    public void report(){
        System.out.println("Found patterns in clusters with min support of " + support +":");
        foundPatterns.forEach((s, frequentSequencePatterns) -> {
            System.out.println( "Cluster " + s + ": " );
            frequentSequencePatterns.forEach( mapoApiCallSeqFrequentSequencePattern ->
                    System.out.println(mapoApiCallSeqFrequentSequencePattern.report()) );
            System.out.println("");
         });

    }*/

    class MapoFrequentSequencePatterMiner extends FrequentSequencePatternMiner<MapoApiCallSeq>{

        @Override
        protected MapoApiCallSeq getApiSequenceRepresentationInstance() {
            return new MapoApiCallSeq(new ApiSequenceRepresentation());
        }
    }

}

