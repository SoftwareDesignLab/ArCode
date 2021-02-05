package edu.rit.se.design.specminer.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class CallGraphDataBasedSlicerUtil {
  static Set<NormalStatement> findRoots(Graph<NormalStatement> graph ){
    Set<NormalStatement> roots = new HashSet<>();
    graph.iterator().forEachRemaining( normalStatement -> {
      if( graph.getPredNodeCount( normalStatement ) == 0 && graph.getSuccNodeCount( normalStatement ) > 0 )
        roots.add( normalStatement );
    } );
    return roots;
  }

  static void mergeToGraph(Graph<NormalStatement> mergeToGraph, Graph<NormalStatement> toBeMergedGraph ){
    toBeMergedGraph.iterator().forEachRemaining( toBeMergedStatement -> {
      if( !mergeToGraph.containsNode( toBeMergedStatement ) )
        mergeToGraph.addNode( toBeMergedStatement );
      toBeMergedGraph.getSuccNodes( toBeMergedStatement ).forEachRemaining( toBeMergedStatementSucc -> {
        if( !mergeToGraph.containsNode( toBeMergedStatementSucc ) )
          mergeToGraph.addNode( toBeMergedStatementSucc );
        if( !mergeToGraph.hasEdge( toBeMergedStatement, toBeMergedStatementSucc ) )
          mergeToGraph.addEdge( toBeMergedStatement, toBeMergedStatementSucc );
      } );
    } );
  }

  static int getMethodParameterIndex(CGNode cgNode , int vn ){
    for( int i = 0; i < cgNode.getIR().getNumberOfParameters(); i++ )
      if( cgNode.getIR().getParameter(i) == vn )
        return i;
    return -1;
  }


  public static Graph<NormalStatement> reverseGraphEdges(Graph<NormalStatement> graph){
    SlowSparseNumberedGraph result = SlowSparseNumberedGraph.make();
    graph.iterator().forEachRemaining( normalStatement -> {
      result.addNode( normalStatement );
    } );

    graph.iterator().forEachRemaining( normalStatement -> {
      graph.getSuccNodes( normalStatement ).forEachRemaining( normalStatementSucc -> {
        result.addEdge( normalStatementSucc, normalStatement );
      } );
    } );
    return result;
  }
}
