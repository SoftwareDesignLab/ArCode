package edu.rit.se.design.arcode.fspecminer.ifd;

import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class IFDBuilder {
    private MethodFieldDependency methodFieldDependency = new MethodFieldDependency();
    private IFD ifd;
    public IFD buildIFD(String frameworkName, String frameworkJarPath, String frameworkPackage) throws IOException {
        CommonConstants.LOGGER.log( Level.INFO, "Creating the IFD model for " + frameworkName + " framework ..." );
        ifd = new IFD( frameworkName );

        createApiFieldDependency( frameworkJarPath, frameworkPackage );
        createIFD();
        removeConstructorMethods();
        removePrivateMethods();
        removeSelfLoopedMethods();
        removeSetterGetterLinks();
        removeFieldsWithZeroDependency();
        removeBidirectionalEdges();
        CommonConstants.LOGGER.log( Level.INFO, "IFD model for " + frameworkName + " was created successfully!" );
        return ifd;
    }

    void createIFD(){
        methodFieldDependency.iterator().forEachRemaining(fieldRepresentation -> {
            methodFieldDependency.getDependencies( fieldRepresentation, MethodFieldDependency.DependencyType.WRITE ).forEach(
                    fromMthodRepresentation -> {
                        methodFieldDependency.getDependencies( fieldRepresentation, MethodFieldDependency.DependencyType.READ ).forEach(
                                toMethodRepresentation ->{
                                    /*ifd.addAPIDependency( fromMthodRepresentation.toString(), toMethodRepresentation.toString(),
                                            PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY);*/
                                    if( !ifd.containsNode( fromMthodRepresentation ) )
                                        ifd.addNode( fromMthodRepresentation );
                                    if( !ifd.containsNode( toMethodRepresentation ) )
                                        ifd.addNode( toMethodRepresentation );
                                    if( !ifd.hasEdge( fromMthodRepresentation, toMethodRepresentation, IFDEdgeType.FIELD_BASE_DEPENDENCY ) )
                                        ifd.addEdge( fromMthodRepresentation, toMethodRepresentation, IFDEdgeType.FIELD_BASE_DEPENDENCY );
                                }
                        );
                    } );
        } );
    }
    void createApiFieldDependency(String jarFilePath, String frameworkPackage) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();
        List<JarEntry> entryList = new ArrayList<>();
        while (entries.hasMoreElements())
            entryList.add( entries.nextElement() );

        Map<ClassNode, List<FieldNode>> allClassesFields = extractAllClassesFields( jarFile );

        for( JarEntry entry: entryList ) {

            String entryName = entry.getName();
            if (entryName.endsWith(/*"LoginContext.class"*/".class" /*"ServicePermission.class"*/ ) && entryName.startsWith( frameworkPackage ) ) {
                InputStream in = jarFile.getInputStream(entry);
                ClassReader reader = new ClassReader(in);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);

                classNode.methods.forEach( methodNode -> {
                    if( methodNode.access <= 2 )
                        completeReaderWriter( classNode, methodNode, allClassesFields, methodFieldDependency);
                } );

            }
        }

/*        methodFieldDependency.removeSelfLoops();
        methodFieldDependency.removeIncomingEdgesForSameOutgoing();
        methodFieldDependency.removeConstructorMethods();
        methodFieldDependency.removePrivateMethods();
        methodFieldDependency.removeFieldsWithZeroDependency();*/
    }

    void removeConstructorMethods(){
        List<MethodRepresentation> toBeRemovedNodes = StreamSupport.stream( ifd.spliterator(), false )
                .filter(node -> node.isConstructor())
                .collect(Collectors.toList());
        toBeRemovedNodes.forEach( ifdNode -> {
                ifd.removeNodeAndEdges( ifdNode );
        });
    }

    void removePrivateMethods(){
        List<MethodRepresentation> toBeRemovedNodes = StreamSupport.stream(ifd.spliterator(), false)
                .filter(node -> node.isPrivateMethod())
                .collect(Collectors.toList());
        toBeRemovedNodes.forEach( ifdNode -> {
                ifd.removeNodeAndEdges( ifdNode );
        });
    }

    public void removeFieldsWithZeroDependency(){
        Set<MethodRepresentation> toBeRemovedNodes = StreamSupport.stream(ifd.spliterator(), false).filter( methodRepresentation -> {
            return ifd.getPredNodeCount( methodRepresentation ) == 0 && ifd.getSuccNodeCount( methodRepresentation ) == 0;
        } ).collect(Collectors.toSet());

        toBeRemovedNodes.forEach( methodRepresentation -> ifd.removeNodeAndEdges( methodRepresentation ) );
    }
    void removeSelfLoopedMethods(){
        ifd.iterator().forEachRemaining( ifdNode -> {
            if( ifd.hasEdge( ifdNode, ifdNode ) )
                ifd.removeEdges(ifdNode, ifdNode);
        } );
    }

    void removeBidirectionalEdges(){
        ifd.iterator().forEachRemaining( ifdNode -> {
            for (IFDEdgeType ifdEdgeType : IFDEdgeType.values()) {
                ifd.getSuccNodes( ifdNode, ifdEdgeType ).forEach( ifdNodeSucc -> {
                    if( ifd.hasEdge( ifdNodeSucc, ifdNode, ifdEdgeType )  ) {
                        ifd.removeEdge(ifdNode, ifdNodeSucc, ifdEdgeType);
                        ifd.removeEdge(ifdNodeSucc, ifdNode, ifdEdgeType);
                    }


                } );

            }
        } );
    }

    void removeSetterGetterLinks(){
        ifd.iterator().forEachRemaining( ifdNode -> {
            if(ifdNode.getMethodNode().name.startsWith( "is" ) )
                System.out.print("");
            if( ifdNode.isGetter() && ifd.getSuccNodeCount( ifdNode ) == 0 && ifd.getPredNodeCount(ifdNode) > 0 ){
                ifd.getPredNodes(ifdNode).forEach( pred -> {
                    if( pred.isSetter() ) {
                        String methodNodeName = ifdNode.getMethodNode().name;
                        String getterField = methodNodeName.startsWith( "is" ) ?
                                methodNodeName.substring(2) :
                                methodNodeName.substring(3);
                        String setterField = pred.methodNode.name.substring(3);
                        if( getterField.equals( setterField ) )
                            ifd.removeEdges(pred, ifdNode);
                    }
                } );
            }

        } );
    }

    void completeReaderWriter(ClassNode methodNodeClass, MethodNode methodNode, Map<ClassNode, List<FieldNode>> classNodeFieldNodeMap, MethodFieldDependency methodFieldDependency){
        ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode instruction = it.next();
            if (instruction.getOpcode() == Opcodes.GETFIELD || instruction.getOpcode() == Opcodes.PUTFIELD) {
                FieldInsnNode fieldIns = (FieldInsnNode) instruction;
                classNodeFieldNodeMap.forEach( (fieldNodeClass, fieldNodes) -> {
                    fieldNodes.forEach(fieldNode -> {
                        if ( /*(fieldNode.name.equals("subject") || fieldNode.name.equals("loginSucceeded")) &&*/
                                fieldNode.name.equals( fieldIns.name ) && fieldNode.desc.equals( fieldIns.desc ) && fieldIns.owner.equals( fieldNodeClass.name  ) ) {
                            try {
                                if( instruction.getOpcode() == Opcodes.GETFIELD )
                                    methodFieldDependency.addReadDependency( new MethodRepresentation(methodNodeClass, methodNode), new FieldRepresentation( fieldNodeClass, fieldNode ) );
                                else
                                    methodFieldDependency.addWriteDependency( new MethodRepresentation(methodNodeClass, methodNode), new FieldRepresentation( fieldNodeClass, fieldNode ) );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } );

            }
        }
    }


    Map<ClassNode, List<FieldNode>> extractAllClassesFields(JarFile jarFile ) throws IOException {
        Map<ClassNode, List<FieldNode>> allClassesFields = new HashMap<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        List<JarEntry> entryList = new ArrayList<>();
        while (entries.hasMoreElements())
            entryList.add( entries.nextElement() );
        for( JarEntry entry: entryList ) {

            String entryName = entry.getName();
            if (entryName.endsWith(/*"LoginContext.class"*/".class")) {
                InputStream in = jarFile.getInputStream(entry);
                ClassReader reader = new ClassReader(in);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);

                allClassesFields.put( classNode, classNode.fields );

            }
        }
        return allClassesFields;
    }

    public static void main(String[] args) throws IOException {
        String frameworkName = "JAAS";
        String frameworkJarPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";
        String frameworkPackage = "javax/security/auth";


        IFDBuilder ifdBuilder = new IFDBuilder();
        IFD ifd = ifdBuilder.buildIFD( frameworkName, frameworkJarPath, frameworkPackage );
        System.out.println( new IFDVisualizer(ifd).dotOutput() );

    }

}
