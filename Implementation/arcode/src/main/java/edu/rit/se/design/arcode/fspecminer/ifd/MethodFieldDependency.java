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

package edu.rit.se.design.arcode.fspecminer.ifd;

import java.util.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class MethodFieldDependency {
    enum DependencyType{READ, WRITE}

    Map< FieldRepresentation, Map<DependencyType, Set<MethodRepresentation>>> fieldDependencyMap = new HashMap<>();

    public void clear(){
        fieldDependencyMap.clear();
    }

    public void addReadDependency (MethodRepresentation methodRepresentation, FieldRepresentation field){
        addDependency(methodRepresentation, field, DependencyType.READ );
    }

    public void addWriteDependency (MethodRepresentation methodRepresentation, FieldRepresentation field){
        addDependency(methodRepresentation, field, DependencyType.WRITE );
    }

    void addDependency(MethodRepresentation methodRepresentation, FieldRepresentation field, DependencyType dependencyType ){
        fieldDependencyMap.computeIfAbsent(field, k -> new HashMap<>());
        if( fieldDependencyMap.get( field ).get( dependencyType ) == null )
            fieldDependencyMap.get( field ).put( dependencyType, new HashSet<>());
        fieldDependencyMap.get( field ).get( dependencyType ).add(methodRepresentation);
    }


/*    public void removeFieldsWithZeroDependency(){
        Set<FieldRepresentation> toBeRemovedFields = new HashSet<>();
        fieldDependencyMap.keySet().forEach( fieldName -> {
            if( (fieldDependencyMap.get( fieldName ).get( DependencyType.WRITE ) == null || fieldDependencyMap.get( fieldName ).get( DependencyType.WRITE ).isEmpty() ) &&
                    (fieldDependencyMap.get( fieldName ).get( DependencyType.READ ) == null || fieldDependencyMap.get( fieldName ).get( DependencyType.READ ).isEmpty() )
             )
                toBeRemovedFields.add( fieldName );
        } );

        toBeRemovedFields.forEach( fieldRepresentation -> fieldDependencyMap.remove( fieldRepresentation ) );

    }

    public void removeIncomingEdgesForSameOutgoing() {
    }

    public void removeSelfLoops() {
        fieldDependencyMap.keySet().forEach( fieldRepresentation -> {
            if( fieldDependencyMap.get( fieldRepresentation ).get( DependencyType.WRITE ) != null &&
                    fieldDependencyMap.get( fieldRepresentation ).get( DependencyType.READ ) != null ) {
                Set<MethodRepresentation> result = fieldDependencyMap.get(fieldRepresentation).get(DependencyType.WRITE).stream()
                        .distinct()
                        .filter(fieldDependencyMap.get(fieldRepresentation).get(DependencyType.READ)::contains)
                        .collect(Collectors.toSet());
                fieldDependencyMap.get(fieldRepresentation).get(DependencyType.WRITE).removeAll(result);
                fieldDependencyMap.get(fieldRepresentation).get(DependencyType.READ).removeAll(result);
            }

        } );
    }

    public void removePrivateMethods(){
        fieldDependencyMap.keySet().forEach( fieldName -> {
            fieldDependencyMap.get( fieldName ).keySet().forEach( actionType -> {
                fieldDependencyMap.get(fieldName).get(actionType).removeIf(methodRepresentation -> methodRepresentation.isPrivateMethod() );
            } );
        } );
    }


    public void removeConstructorMethods(){
        fieldDependencyMap.keySet().forEach( fieldName -> {
            fieldDependencyMap.get( fieldName ).keySet().forEach( actionType -> {
                fieldDependencyMap.get(fieldName).get(actionType).removeIf(methodRepresentation ->
                methodRepresentation.isConstructor()
                );
            } );
        } );
    }

    public StringBuilder toDotGraph(boolean showSelfLoops){
        Map<MethodRepresentation, Map<MethodRepresentation, Set<FieldRepresentation>>> fromToMap = new HashMap<>();
        List<FieldRepresentation> fieldNodeList = new ArrayList<>();
        fieldDependencyMap.keySet().forEach( fieldNode -> {
            if ( !fieldNodeList.contains(fieldNode) )
                fieldNodeList.add( fieldNode );
            if( fieldDependencyMap.get( fieldNode ).get( DependencyType.WRITE ) != null &&  fieldDependencyMap.get( fieldNode ).get( DependencyType.WRITE ).size() > 0 &&
                    fieldDependencyMap.get( fieldNode ).get( DependencyType.READ ) != null &&  fieldDependencyMap.get( fieldNode ).get( DependencyType.READ ).size() > 0 ){
                fieldDependencyMap.get( fieldNode ).get( DependencyType.WRITE ).forEach(fromMethodNode -> {

                    fieldDependencyMap.get( fieldNode ).get( DependencyType.READ ).forEach(toMethodNode -> {
//                        if( !fromMethodNode.equals( toMethodNode ) ) {
                            if (fromToMap.get(fromMethodNode) == null)
                                fromToMap.put(fromMethodNode, new HashMap<>());

                            if( showSelfLoops || !fromMethodNode.equals(toMethodNode) ) {
                                if (fromToMap.get(fromMethodNode).get(toMethodNode) == null)
                                    fromToMap.get(fromMethodNode).put(toMethodNode, new HashSet<>());
                                fromToMap.get(fromMethodNode).get(toMethodNode).add(fieldNode);
                            }
//                        }
                    } );
                } );
            }
        } );
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "digraph prof {\n" +
                "graph [label=\"" + fieldNodeList.stream().map( fieldNode -> "\\n" + String.valueOf( fieldNodeList.indexOf(fieldNode) + 1 ) + ": " + fieldNode ).collect(Collectors.joining("")) + "\"];" +
                "\tratio = fill;\n" +
                "\tnode [style=filled];\n" );

        fromToMap.forEach(
                (fromMethodNode, toMethodNodes) -> {
                    toMethodNodes.keySet().forEach(toMethodNode -> {
                                String label = "\"" + fromMethodNode + "\" -> \"" + toMethodNode + "\"";
                                label = label.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","");
                                label += " [label = \"" + fromToMap.get(fromMethodNode).get(toMethodNode).stream().map(e -> String.valueOf(fieldNodeList.indexOf(e) + 1)).collect(Collectors.joining(","))  + "\"];";
                                stringBuilder.append( label + "\n");
                            }
                    );
                }
        );
        stringBuilder.append("}");

        return stringBuilder;
    }*/

    public Iterator<FieldRepresentation> iterator() {
        return fieldDependencyMap.keySet().iterator();
    }

    public Set<MethodRepresentation> getDependencies( FieldRepresentation fieldRepresentation, DependencyType dependencyType ){
        Set<MethodRepresentation> fromDependencies = new HashSet<>();
        if( !fieldDependencyMap.containsKey( fieldRepresentation ) ||
                !fieldDependencyMap.get( fieldRepresentation ).containsKey( dependencyType )  )
            return fromDependencies;
        return fieldDependencyMap.get( fieldRepresentation ).get( dependencyType );
    }
}
