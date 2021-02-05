package edu.rit.se.design.specminer.util.graph;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

// T: node, E: edge type, G: edge info
public abstract class DirectedGraph<T extends DirectedGraphNode, E extends DirectedGraphEdgeType,
        G extends DirectedGraphEdgeInfo> implements Iterable<T>, Serializable {
    Set<T> nodeSet;
    Map<T, Map<T, Map<E, G>>> successorMap;
    Map<T, Map<T, Map<E, G>>> predecessorMap;

    public DirectedGraph() {
        this.nodeSet = new HashSet<>();
        this.predecessorMap = new HashMap<>();
        this.successorMap = new HashMap<>();
    }

    public void addNode( T node )  {
        if( containsNode( node ) )
            throw new IllegalArgumentException( "Node already exists!" );
        nodeSet.add( node );
        predecessorMap.put( node, new HashMap<>() );
        successorMap.put( node, new HashMap<>() );
    }

    public boolean containsNode(T node ){
        return nodeSet.contains( node );
    }

    public int getNumberOfNodes(){
        return nodeSet.size();
    }

    public void addEdge( T fromNode, T toNode, E edgeType, G edgeInfo ) {
        if( hasEdge( fromNode, toNode, edgeType, edgeInfo ) )
            throw new IllegalArgumentException( "Edge already exists!" );
        if( !successorMap.containsKey(fromNode) )
            successorMap.put( fromNode, new HashMap<>() );
        if( successorMap.get(fromNode).get(toNode) == null )
            successorMap.get(fromNode).put(toNode, new HashMap<>());
        successorMap.get( fromNode ).get( toNode ).put( edgeType, edgeInfo );

        if( !predecessorMap.containsKey(toNode) )
            predecessorMap.put( toNode, new HashMap<>() );
        if( predecessorMap.get(toNode).get(fromNode) == null )
            predecessorMap.get(toNode).put(fromNode, new HashMap<>());
        predecessorMap.get( toNode ).get( fromNode ).put( edgeType, edgeInfo );
    }

    public void addEdge( T fromNode, T toNode, E edgeType ) {
        addEdge( fromNode, toNode, edgeType, getDefaultEdgeInfo( edgeType ) );
    }

    public void addEdge( T fromNode, T toNode  ) {
        addEdge( fromNode, toNode, getDefaultEdgeType() );
    }

    public boolean hasEdge( T fromNode, T toNode, E edgeType, G edgeInfo  ) {
        if( !containsNode( fromNode ) || !containsNode(toNode) )
            throw new IllegalArgumentException( "fromNode or toNode does not exist!" );

        return successorMap.containsKey( fromNode ) &&
                successorMap.get( fromNode ).get( toNode ) != null &&
                successorMap.get( fromNode ).get( toNode ).get( edgeType ) != null &&
                successorMap.get( fromNode ).get( toNode ).get( edgeType ).equals( edgeInfo ) ;
    }

    public boolean hasEdge( T fromNode, T toNode, E edgeType ) {
        if( !containsNode( fromNode ) || !containsNode(toNode) )
            throw new IllegalArgumentException( "fromNode or toNode does not exist!" );

        return successorMap.containsKey( fromNode ) &&
                successorMap.get( fromNode ).get( toNode ) != null &&
                successorMap.get( fromNode ).get( toNode ).get( edgeType ) != null;
    }

    public boolean hasEdge( T fromNode, T toNode ) {
        if( !containsNode( fromNode ) || !containsNode(toNode) )
            throw new IllegalArgumentException( "fromNode or toNode does not exist!" );

        return successorMap.containsKey( fromNode ) &&
                successorMap.get( fromNode ).get( toNode ) != null &&
                !successorMap.get( fromNode ).get( toNode ).isEmpty() ;
    }

    public Iterable<G> getEdges( T fromNode, T toNode ) {
        if( !hasEdge( fromNode, toNode ) )
            throw new IllegalArgumentException( "Edge does not exist!" );
        return successorMap.get( fromNode ).get( toNode ).values()::iterator;
    }

    public G getEdge( T fromNode, T toNode, E edgeType ) {
        if( !hasEdge( fromNode, toNode, edgeType ) )
            throw new IllegalArgumentException( "Edge does not exist!" );
        return successorMap.get( fromNode ).get( toNode ).get( edgeType );
    }

    public void removeNode( T node ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        nodeSet.remove( node );
    }

    public void removeEdges( T fromNode, T toNode ) {
        if( !hasEdge( fromNode, toNode ) )
            throw new IllegalArgumentException( "Edge does not exist!" );
        successorMap.get(fromNode).remove(toNode);
        predecessorMap.get(toNode).remove(fromNode);
    }

    public void removeEdge( T fromNode, T toNode, E edgeType ) {
        if( !hasEdge( fromNode, toNode, edgeType ) )
            throw new IllegalArgumentException( "Edge does not exist!" );
        successorMap.get(fromNode).get(toNode).remove(edgeType);
        if( successorMap.get(fromNode).get(toNode).isEmpty() )
            successorMap.get(fromNode).remove(toNode);
        predecessorMap.get(toNode).get(fromNode).remove(edgeType);
        if( predecessorMap.get(toNode).get(fromNode).isEmpty() )
            predecessorMap.get(toNode).remove(fromNode);

    }

    public void removeNodeAndEdges( T node ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        StreamSupport.stream(getSuccNodes( node ).spliterator() , true).
                collect(Collectors.toSet()).forEach(successor -> removeEdges( node, successor ) );
        StreamSupport.stream(getPredNodes( node ).spliterator() , true).
                collect(Collectors.toSet()).forEach(predecessor -> removeEdges( predecessor, node ) );
//        successorMap.forEach( (fromNode, toMap) -> toMap.remove( node ) );
        successorMap.remove( node );

//        predecessorMap.forEach( (toNode, fromMap) -> fromMap.remove( node ) );
        predecessorMap.remove( node );
        removeNode( node );
    }

    public Iterable<T> getPredNodes(T node ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        if( predecessorMap.get( node ) != null )
            return predecessorMap.get( node ).keySet()::iterator;
        return (new HashSet<T>())::iterator;
    }

    public Iterable<T> getPredNodes(T node, E edgeType ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        if( predecessorMap.get( node ) != null )
            return predecessorMap.get( node ).keySet().stream().filter(
                    pred -> predecessorMap.get(node).get(pred).containsKey( edgeType )
            ).collect(Collectors.toSet())::iterator;
        return (new HashSet<T>())::iterator;
    }

    public int getPredNodeCount( T node ){
        return (int) StreamSupport.stream( getPredNodes( node ).spliterator(), false ).count();
    }

    public int getPredNodeCount( T node, E edgeType ){
        return (int) StreamSupport.stream( getPredNodes( node, edgeType ).spliterator(), false ).count();
    }

    public Iterable<T> getSuccNodes(T node ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        if( successorMap.get( node ) != null )
            return successorMap.get( node ).keySet()::iterator;
        return (new HashSet<T>())::iterator;
    }

    public Iterable<T> getSuccNodes(T node, E edgeType ) {
        if( !containsNode( node ) )
            throw new IllegalArgumentException( "Node does not exist!" );
        if( successorMap.get( node ) != null )
            return successorMap.get( node ).keySet().stream().filter(
                    succ -> successorMap.get(node).get(succ).containsKey( edgeType )
            ).collect(Collectors.toSet())::iterator;
        return (new HashSet<T>())::iterator;
    }

    public int getSuccNodeCount( T node ){
        return (int) StreamSupport.stream( getSuccNodes( node ).spliterator(), false ).count();
    }

    public T getRoot(){
        Set<T> roots = StreamSupport.stream( spliterator(), false ).filter(
                node -> getPredNodeCount( node ) == 0 ).collect(Collectors.toSet());
        if( roots.size() != 1 )
            throw new RuntimeException( "Zero or more than one root(s)!" );
        return roots.iterator().next();
    }

    @Override
    public Iterator<T> iterator() {
        return nodeSet.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T node : this) {
            action.accept(node);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

    protected abstract E getDefaultEdgeType();

    protected abstract G getDefaultEdgeInfo( E edgeType );

    public abstract String getTitle();

    public Set<T> getNodeSet(){
        return nodeSet;
    }
}
