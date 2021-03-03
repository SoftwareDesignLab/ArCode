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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rit.se.design.arcode.fspecminer.graam;

/**
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class RMIClasses {

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

}
