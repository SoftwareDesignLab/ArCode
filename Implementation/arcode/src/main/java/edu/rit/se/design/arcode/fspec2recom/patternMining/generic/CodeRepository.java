package edu.rit.se.design.arcode.fspec2recom.patternMining.generic;

import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CodeRepository {
    Map<String, List<GRAAM>> projectNameGRAAMListMap;
    Map<String, Set<ApiRepresentation>> apiUsageInPrograms;
    Map<String, Set<ApiSequenceRepresentation>> apiSeqUsageInPrograms;
    Set<ApiRepresentation> apiSetInRepository;

    public CodeRepository(  ){

    }

    public CodeRepository( Map<String, List<GRAAM>> projectNameGRAAMListMap ){
        setProjectNameGRAAMListMap( projectNameGRAAMListMap );
        setApiUsageInPrograms( extractAPIUsageInPrograms( getProjectNameGRAAMListMap() ) );
        setApiSeqUsageInPrograms( extractAPISequenceUsageInPrograms( getProjectNameGRAAMListMap() ) );
        setApiSetInRepository( findAPISetInRepository( getApiUsageInPrograms() ) );
    }

    public Map<String, List<GRAAM>> getProjectNameGRAAMListMap() {
        return projectNameGRAAMListMap;
    }

    public void setProjectNameGRAAMListMap(Map<String, List<GRAAM>> projectNameGRAAMListMap) {
        this.projectNameGRAAMListMap = projectNameGRAAMListMap;
    }


    public Map<String, Set<ApiRepresentation>> getApiUsageInPrograms() {
        return apiUsageInPrograms;
    }

    public void setApiUsageInPrograms(Map<String, Set<ApiRepresentation>> apiUsageInPrograms) {
        this.apiUsageInPrograms = apiUsageInPrograms;
    }

    public Set<ApiRepresentation> getApiSetInRepository() {
        return apiSetInRepository;
    }

    public void setApiSetInRepository(Set<ApiRepresentation> apiSetInRepository) {
        this.apiSetInRepository = apiSetInRepository;
    }

    public Map<String, Set<ApiSequenceRepresentation>> getApiSeqUsageInPrograms() {
        return apiSeqUsageInPrograms;
    }

    public void setApiSeqUsageInPrograms(Map<String, Set<ApiSequenceRepresentation>> apiSeqUsageInPrograms) {
        this.apiSeqUsageInPrograms = apiSeqUsageInPrograms;
    }

    protected Map<String, Set<ApiRepresentation>> extractAPIUsageInPrograms(Map<String, List<GRAAM>> projectNameGRAAMListMap ){
        Map<String, Set<ApiRepresentation>> programAPIMap = new HashMap<>();
        projectNameGRAAMListMap.keySet().forEach( primaryProjectName -> {
            String projectName = primaryProjectName.endsWith(".jar") ? primaryProjectName : primaryProjectName.substring( 0, primaryProjectName.indexOf(".jar") + 4 );
            projectNameGRAAMListMap.get( primaryProjectName ).forEach( graam -> {
                if( programAPIMap.get( projectName ) == null )
                    programAPIMap.put( projectName, new HashSet<>());
                graam.getNodeSet().forEach( graamNode -> {
                    if( graamNode instanceof FrameworkRelatedNode) {
                        ApiRepresentation apiRepresentation = new ApiRepresentation((FrameworkRelatedNode) graamNode);
                        programAPIMap.get(projectName).add(apiRepresentation);
                    }
                } );
            } );
        } );
        return programAPIMap;
    }

/*
    public Map<String, Map<String, Set<ApiRepresentation>>> extractAPIUsageInProgramsClasses(){
        Map<String, Map<String, Set<ApiRepresentation>>> programClassAPIMap = new HashMap<>();
        projectNameGRAAMListMap.keySet().forEach( primaryProjectName -> {
            String projectName = primaryProjectName.endsWith(".jar") ? primaryProjectName : primaryProjectName.substring( 0, primaryProjectName.indexOf(".jar") + 4 );
            projectNameGRAAMListMap.get( primaryProjectName ).forEach( graam -> {
                if( programClassAPIMap.get( projectName ) == null )
                    programClassAPIMap.put( projectName, new HashMap<>());
                graam.getNodeSet().forEach( graamNode -> {
                    if( graamNode instanceof FrameworkRelatedNode ) {
                        ApiRepresentation apiRepresentation = new ApiRepresentation((FrameworkRelatedNode) graamNode);
                        if( programClassAPIMap.get(projectName).get(((FrameworkRelatedNode) graamNode).getGRAAMNodeOrigin()) == null )
                            programClassAPIMap.get(projectName).put(((FrameworkRelatedNode) graamNode).getGRAAMNodeOrigin(), new HashSet<>());
                        programClassAPIMap.get(projectName).get(((FrameworkRelatedNode) graamNode).getGRAAMNodeOrigin()).add(apiRepresentation);
                    }
                } );
            } );
        } );
        return programClassAPIMap;
    }
*/


    protected Map<String, Set<ApiSequenceRepresentation>> extractAPISequenceUsageInPrograms(Map<String, List<GRAAM>> projectNameGRAAMListMap ){
        Map<String, Set<ApiSequenceRepresentation>> programAPISeqMap = new HashMap<>();
        projectNameGRAAMListMap.keySet().forEach( primaryProjectName -> {
            String projectName = primaryProjectName.endsWith(".jar") ? primaryProjectName : primaryProjectName.substring( 0, primaryProjectName.indexOf(".jar") + 4 );
            projectNameGRAAMListMap.get( primaryProjectName ).forEach( graam -> {
                if( programAPISeqMap.get( projectName ) == null )
                    programAPISeqMap.put( projectName, new HashSet<>());

                List<ApiSequenceRepresentation> apiSequenceRepresentationList = extractAPISequenceUsageFromGRAAM( graam );
                // If there are more than 3 sequences found in the program, reduce the list to 3
                int listSize = apiSequenceRepresentationList.size();
                Random rand = new Random(System.currentTimeMillis());
                for( int i = 0; i < listSize - 20; i++ )
                    apiSequenceRepresentationList.remove(rand.nextInt( apiSequenceRepresentationList.size() ));
                apiSequenceRepresentationList.forEach( apiSequenceRepresentation -> {
                    programAPISeqMap.get( projectName ).add( apiSequenceRepresentation );
                } );
            } );
        } );
        return programAPISeqMap;
    }

    protected List<ApiSequenceRepresentation> extractAPISequenceUsageFromGRAAM( GRAAM graam ){
        Map<DirectedGraphNode, Set<DirectedGraphNode>> incomingEdges = new HashMap<>();
        graam.iterator().forEachRemaining( node -> {
            if( !incomingEdges.containsKey( node ) )
                incomingEdges.put( node, new HashSet<>() );
            graam.getPredNodes( node ).forEach( pred -> {
/*
                if( pred instanceof NonFrameworkBoundaryNode && ((NonFrameworkBoundaryNode) pred).getType().equals(
                        NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE ) )
*/
                incomingEdges.get(node).add( pred );
            } );
        } );


        incomingEdges.keySet().forEach( node -> incomingEdges.get(node).remove( graam.getStartNode() ) );
        incomingEdges.get( graam.getStartNode() ).add( graam.getStartNode() );

        Set<DirectedGraphNode> visitedNodes = new HashSet<>();
        visitedNodes.add( graam.getStartNode() );

/*        List<ApiSequenceRepresentation> buildSeq = new ArrayList<>();
        ApiSequenceRepresentation apiSequenceRepresentation = new ApiSequenceRepresentation();
        apiSequenceRepresentation.addAtFirst( new ApiRepresentation( graam.getStartNode() ) );
        buildSeq.add( new ApiSequenceRepresentation() );*/

        List<ApiSequenceRepresentation> apiSequenceRepresentationList = buildAPISequenceRepresentationList( graam, visitedNodes, incomingEdges, new ArrayList<>() );//new ArrayList<>();
/*
        try {
            graam.getSuccNodes( graam.getRoot()*/
/*, GRAAMEdge.GRAAMEdgeType.SEQUENCE*//*
).forEach( graamNode -> {
                try {
                    apiSequenceRepresentationList.addAll( buildAPISequenceRepresentationList( graam, graamNode, new HashSet<>() ) );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } );

        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        apiSequenceRepresentationList.forEach( apiSequenceRepresentation -> apiSequenceRepresentation.setGraam(graam) );
        return apiSequenceRepresentationList;
    }

    protected List<ApiSequenceRepresentation> buildAPISequenceRepresentationList( GRAAM graam, Set<DirectedGraphNode> visitedGRAAMNodes, Map<DirectedGraphNode, Set<DirectedGraphNode>> incomingEdges, List<ApiSequenceRepresentation> builtSeq ) {
        if( visitedGRAAMNodes.size() == graam.getNodeSet().size() )
            return builtSeq;
        List<ApiSequenceRepresentation> seqList = new ArrayList<>();
        List<DirectedGraphNode> toBeVisitedNodes = StreamSupport.stream(graam.spliterator(), false).
                filter( node ->  incomingEdges.get( node ).size() == 0 && !visitedGRAAMNodes.contains( node ) && !( node instanceof NonFrameworkBoundaryNode ) ).collect(Collectors.toList());

        if(toBeVisitedNodes.isEmpty())
            seqList.addAll( builtSeq );
        else
            toBeVisitedNodes.forEach(toBeVisitedNode -> {
                List<ApiSequenceRepresentation> builtSeqClone = new ArrayList<>();
                builtSeq.forEach( apiSequenceRepresentation -> builtSeqClone.add( new ApiSequenceRepresentation( apiSequenceRepresentation.getApiRepresentationList() ) ) );

                if( builtSeqClone.isEmpty() )
                    builtSeqClone.add( new ApiSequenceRepresentation() );
                builtSeqClone.forEach( clonedAPISeq -> clonedAPISeq.addToLast( new ApiRepresentation((FrameworkRelatedNode) toBeVisitedNode) ) );

                Set<DirectedGraphNode> visitedGRAAMNodesClone = new HashSet<>( visitedGRAAMNodes );
                Map<DirectedGraphNode, Set<DirectedGraphNode>> incomingEdgesClone = new HashMap<>();
                incomingEdges.forEach( (node, set) -> incomingEdgesClone.put( node, new HashSet<>( set ) ) );
                visitedGRAAMNodesClone.add( toBeVisitedNode );
                incomingEdgesClone.keySet().forEach( node -> incomingEdgesClone.get( node ).remove( toBeVisitedNode ) );

                seqList.addAll( buildAPISequenceRepresentationList( graam, visitedGRAAMNodesClone, incomingEdgesClone, builtSeqClone ) );

            } );

        return seqList;
/*        Set<DirectedGraphNode> clonedVisitedGRAAMNodes = new HashSet<>( visitedGRAAMNodes );
        clonedVisitedGRAAMNodes.add( graamNode );

        List<ApiSequenceRepresentation> childrenApiSequenceRepresentationList = new ArrayList<>();

        graam.getSuccNodes( graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP).forEach(childGRAAMNode -> {
            try {
                childrenApiSequenceRepresentationList.addAll( buildAPISequenceRepresentationList( graam, childGRAAMNode, clonedVisitedGRAAMNodes ) );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } );

        apiSequenceRepresentationList = generateSequenceCombinations( childrenApiSequenceRepresentationList );
        apiSequenceRepresentationList.forEach( apiSequenceRepresentation -> apiSequenceRepresentation.addAtFirst( new ApiRepresentation((FrameworkRelatedNode) graamNode) ) );

        if( apiSequenceRepresentationList.isEmpty() ) {
            // This is a leaf
            ApiSequenceRepresentation apiSequenceRepresentation = new ApiSequenceRepresentation();
            apiSequenceRepresentation.addAtFirst( new ApiRepresentation((FrameworkRelatedNode) graamNode) );
            apiSequenceRepresentationList.add(apiSequenceRepresentation);
        }
        return apiSequenceRepresentationList;*/
    }

    List<ApiSequenceRepresentation> generateSequenceCombinations( List<ApiSequenceRepresentation> childrenSequences){
        List<ApiSequenceRepresentation> generatedSequenceCombinations = new ArrayList<>();
        childrenSequences.forEach( apiSequenceRepresentation -> {
            List<ApiSequenceRepresentation> tmp = new ArrayList<>();
            tmp.addAll( childrenSequences );
            tmp.remove( apiSequenceRepresentation );
            List<ApiSequenceRepresentation> generatedSequenceCombinationsTmp = generateSequenceCombinations( tmp );
            generatedSequenceCombinationsTmp.forEach( apiSequenceRepresentation1 -> {
                apiSequenceRepresentation1.getApiRepresentationList().addAll( 0, apiSequenceRepresentation.getApiRepresentationList() );
            } );
        } );
        return null;
    }

    protected Set<ApiRepresentation> findAPISetInRepository(Map<String, Set<ApiRepresentation>> programAPISetMap ){
        Set<ApiRepresentation> apiRepresentationSet = HashSetFactory.make();
        programAPISetMap.keySet().forEach( programName -> {
                    Set<ApiRepresentation> programAPISet = programAPISetMap.get(programName);
                    apiRepresentationSet.addAll(programAPISet);
                }
        );
        return apiRepresentationSet;
    }
}
