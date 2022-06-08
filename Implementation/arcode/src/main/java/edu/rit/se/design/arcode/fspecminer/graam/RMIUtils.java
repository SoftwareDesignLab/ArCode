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

package edu.rit.se.design.arcode.fspecminer.graam;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import edu.rit.se.design.arcode.fspecminer.analysis.WalaUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This class has utility methods to aid the identification of relevant/irrelevant nodes in a statement graph.
 *
 * @author Joanna, Ali Shokri (as8308@rit.edu)
 */
public class RMIUtils extends FrameworkUtils{
    private static final String RMI_LOCATE_REGISTRY = "java/rmi/registry/LocateRegistry";
    private static final String METHOD_GET_REGISTRY = "getRegistry";
    private static final String METHOD_CREATE_REGISTRY = "createRegistry";

    public static final String[] NON_EXCEPTION_RMI_CLASSES = new String[]{
            "Ljava/rmi/activation/Activatable",
            "Ljava/rmi/activation/ActivationDesc",
            "Ljava/rmi/activation/ActivationGroup",
            "Ljava/rmi/activation/ActivationGroup_Stub",
            "Ljava/rmi/activation/ActivationGroupDesc/CommandEnvironment",
            "Ljava/rmi/activation/ActivationGroupDesc",
            "Ljava/rmi/activation/ActivationGroupID",
            "Ljava/rmi/activation/ActivationID",
            "Ljava/rmi/activation/ActivationInstantiator",
            "Ljava/rmi/activation/ActivationMonitor",
            "Ljava/rmi/activation/ActivationSystem",
            "Ljava/rmi/activation/Activator",
            "Ljava/rmi/dgc/DGC",
            "Ljava/rmi/dgc/Lease",
            "Ljava/rmi/dgc/VMID",
            "Ljava/rmi/MarshalledObject",
            "Ljava/rmi/Naming",
            "Ljava/rmi/registry/LocateRegistry",
            "Ljava/rmi/registry/Registry",
            "Ljava/rmi/registry/RegistryHandler",
            "Ljava/rmi/Remote",
            "Ljava/rmi/RMISecurityManager",
            "Ljava/rmi/server/LoaderHandler",
            "Ljava/rmi/server/LogStream",
            "Ljava/rmi/server/ObjID",
            "Ljava/rmi/server/Operation",
            "Ljava/rmi/server/RemoteCall",
            "Ljava/rmi/server/RemoteObject",
            "Ljava/rmi/server/RemoteObjectInvocationHandler",
            "Ljava/rmi/server/RemoteRef",
            "Ljava/rmi/server/RemoteServer",
            "Ljava/rmi/server/RemoteStub",
            "Ljava/rmi/server/RMIClassLoader",
            "Ljava/rmi/server/RMIClassLoaderSpi",
            "Ljava/rmi/server/RMIClientSocketFactory",
            "Ljava/rmi/server/RMIFailureHandler",
            "Ljava/rmi/server/RMIServerSocketFactory",
            "Ljava/rmi/server/RMISocketFactory",
            "Ljava/rmi/server/ServerRef",
            "Ljava/rmi/server/Skeleton",
            "Ljava/rmi/server/UID",
            "Ljava/rmi/server/UnicastRemoteObject",
            "Ljava/rmi/server/Unreferenced",
            "Ljava/rmi/ServerError",};

    public static final String[] ALL_RMI_CLASSES = new String[]{
            "Ljava/rmi/AccessException",
            "Ljava/rmi/activation/Activatable",
            "Ljava/rmi/activation/ActivateFailedException",
            "Ljava/rmi/activation/ActivationDesc",
            "Ljava/rmi/activation/ActivationException",
            "Ljava/rmi/activation/ActivationGroup",
            "Ljava/rmi/activation/ActivationGroup_Stub",
            "Ljava/rmi/activation/ActivationGroupDesc/CommandEnvironment",
            "Ljava/rmi/activation/ActivationGroupDesc",
            "Ljava/rmi/activation/ActivationGroupID",
            "Ljava/rmi/activation/ActivationID",
            "Ljava/rmi/activation/ActivationInstantiator",
            "Ljava/rmi/activation/ActivationMonitor",
            "Ljava/rmi/activation/ActivationSystem",
            "Ljava/rmi/activation/Activator",
            "Ljava/rmi/activation/UnknownGroupException",
            "Ljava/rmi/activation/UnknownObjectException",
            "Ljava/rmi/AlreadyBoundException",
            "Ljava/rmi/ConnectException",
            "Ljava/rmi/ConnectIOException",
            "Ljava/rmi/dgc/DGC",
            "Ljava/rmi/dgc/Lease",
            "Ljava/rmi/dgc/VMID",
            "Ljava/rmi/MarshalException",
            "Ljava/rmi/MarshalledObject",
            "Ljava/rmi/Naming",
            "Ljava/rmi/NoSuchObjectException",
            "Ljava/rmi/NotBoundException",
            "Ljava/rmi/registry/LocateRegistry",
            "Ljava/rmi/registry/Registry",
            "Ljava/rmi/registry/RegistryHandler",
            "Ljava/rmi/Remote",
            "Ljava/rmi/RemoteException",
            "Ljava/rmi/RMISecurityException",
            "Ljava/rmi/RMISecurityManager",
            "Ljava/rmi/server/ExportException",
            "Ljava/rmi/server/LoaderHandler",
            "Ljava/rmi/server/LogStream",
            "Ljava/rmi/server/ObjID",
            "Ljava/rmi/server/Operation",
            "Ljava/rmi/server/RemoteCall",
            "Ljava/rmi/server/RemoteObject",
            "Ljava/rmi/server/RemoteObjectInvocationHandler",
            "Ljava/rmi/server/RemoteRef",
            "Ljava/rmi/server/RemoteServer",
            "Ljava/rmi/server/RemoteStub",
            "Ljava/rmi/server/RMIClassLoader",
            "Ljava/rmi/server/RMIClassLoaderSpi",
            "Ljava/rmi/server/RMIClientSocketFactory",
            "Ljava/rmi/server/RMIFailureHandler",
            "Ljava/rmi/server/RMIServerSocketFactory",
            "Ljava/rmi/server/RMISocketFactory",
            "Ljava/rmi/server/ServerCloneException",
            "Ljava/rmi/server/ServerNotActiveException",
            "Ljava/rmi/server/ServerRef",
            "Ljava/rmi/server/Skeleton",
            "Ljava/rmi/server/SkeletonMismatchException",
            "Ljava/rmi/server/SkeletonNotFoundException",
            "Ljava/rmi/server/SocketSecurityException",
            "Ljava/rmi/server/UID",
            "Ljava/rmi/server/UnicastRemoteObject",
            "Ljava/rmi/server/Unreferenced",
            "Ljava/rmi/ServerError",
            "Ljava/rmi/ServerException",
            "Ljava/rmi/ServerRuntimeException",
            "Ljava/rmi/StubNotFoundException",
            "Ljava/rmi/UnexpectedException",
            "Ljava/rmi/UnknownHostException",
            "Ljava/rmi/UnmarshalException"
    };


    public RMIUtils(boolean considerOverloadedMethodsAsTheSame) {
        super(considerOverloadedMethodsAsTheSame);
    }

/*    @Override
    public boolean isFrameworkEntrypoint(EntrypointInfo entrypointInfo) {
        return WalaUtils.isMainMethod(entrypointInfo) ||
                WalaUtils.invokesMethod(entrypointInfo, RMI_LOCATE_REGISTRY,
                new String[]{METHOD_CREATE_REGISTRY, METHOD_GET_REGISTRY});    }*/

    @Override
    public boolean isFrameworkEntrypoint(Entrypoint entrypoint) {
        return WalaUtils.isMainMethod(entrypoint) ||
                WalaUtils.invokesMethod(entrypoint, RMI_LOCATE_REGISTRY,
                        new String[]{METHOD_CREATE_REGISTRY, METHOD_GET_REGISTRY});    }

    public List<String> getFrameworkClasses(){
        return Arrays.asList( NON_EXCEPTION_RMI_CLASSES );
    }

}
