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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecAPICallNode implements FSpecAPINode {
    String className;
    String methodSignature;
    boolean staticMethod;
    boolean publicMethod;

    public FSpecAPICallNode(String className, String methodSignature, boolean isStaticMethod, boolean isPublicMethod) {
        this.className = className;
        this.methodSignature = methodSignature;
        this.staticMethod = isStaticMethod;
        this.publicMethod = isPublicMethod;
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
        String[] candidateArguments = methodSignature.substring( methodSignature.indexOf("(") + 1, methodSignature.indexOf( ")" ) ).split(";");
        for (String candidateArgument : candidateArguments) {
            if( candidateArgument.length() > 0 )
                argumentTypes.add( candidateArgument );
        }
        return argumentTypes;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public String getMethodName(){
/*        if( methodSignature.indexOf("(") < 2)
            return "";*/
        return methodSignature.substring(0, methodSignature.indexOf("("));
    }

    public String getReturnType(){
        return methodSignature.substring( methodSignature.indexOf( ")" ) + 1).replaceAll(";", "");
    }

    public String getSimpleReturnType(){
        boolean isArray = getReturnType().startsWith("[");
        return getReturnType().replaceAll(".*\\/", "") + (isArray? "[]" : "");
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }

    public boolean isPublicMethod() {
        return publicMethod;
    }

    @Override
    public String getTitle() {
        return className + "." + methodSignature;
    }

    @Override
    public FSpecAPICallNode clone() {
        return new FSpecAPICallNode( className,  methodSignature,  staticMethod,  publicMethod);
    }
}
