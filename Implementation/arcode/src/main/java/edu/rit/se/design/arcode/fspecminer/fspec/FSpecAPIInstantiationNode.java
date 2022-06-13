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

package edu.rit.se.design.arcode.fspecminer.fspec;

import edu.rit.se.design.arcode.fspecminer.graam.MethodContextInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecAPIInstantiationNode implements FSpecAPINode {
    String className;
    String constructorSignature;
    boolean isAbstractOrInterface;
    boolean isPublicConstructor;
    MethodContextInfo methodContextInfo;

    public FSpecAPIInstantiationNode(String className, String constructorSignature, boolean isAbstractOrInterface, boolean isPublicConstructor, MethodContextInfo methodContextInfo) {
        this.className = className;
        this.constructorSignature = constructorSignature;
        this.isAbstractOrInterface = isAbstractOrInterface;
        this.isPublicConstructor = isPublicConstructor;
        this.methodContextInfo = methodContextInfo;
    }

    public String getFullClassName() {
        return className;
    }

    @Override
    public String getSimpleClassName() {
        return getFullClassName().replaceAll(".*\\/", "");
    }

    @Override
    public List<String> getArgumentTypes() {
        List<String> argumentTypes = new ArrayList<>();
        String[] candidateArguments = constructorSignature.substring( constructorSignature.indexOf("(") + 1, constructorSignature.indexOf( ")" ) ).split(";");
        for (String candidateArgument : candidateArguments) {
            if( candidateArgument.length() > 0 )
                argumentTypes.add( candidateArgument );
        }
        return argumentTypes;    }

    @Override
    public MethodContextInfo getMethodContextInfo() {
        return methodContextInfo;
    }

    public String getConstructorSignature() {
        return constructorSignature;
    }

    @Override
    public String getTitle() {
        return "Init " + className + constructorSignature.replaceAll("<init>", "");
    }

    @Override
    public FSpecAPIInstantiationNode clone() {
        return new FSpecAPIInstantiationNode( className, constructorSignature, isAbstractOrInterface, isPublicConstructor, methodContextInfo.clone() );
    }

    public boolean isAbstractOrInterface(){
        return isAbstractOrInterface;
    }

    public boolean isPublicConstructor() {
        return isPublicConstructor;
    }
}
