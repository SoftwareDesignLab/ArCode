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

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public abstract class MethodPreCondition {
    public enum PreConditionType{EQUAL, GREATER_THAN, LESS_THAN, NOT_EQUAL}
    protected int methodArgumentIndex;
    protected PreConditionType preConditionType;

    public MethodPreCondition(int methodArgumentIndex, PreConditionType preConditionType) {
        this.methodArgumentIndex = methodArgumentIndex;
    }

    public int getMethodArgumentIndex() {
        return methodArgumentIndex;
    }

    public void setMethodArgumentIndex(int methodArgumentIndex) {
        this.methodArgumentIndex = methodArgumentIndex;
    }

    public PreConditionType getPreConditionType() {
        return preConditionType;
    }

    public void setPreConditionType(PreConditionType preConditionType) {
        this.preConditionType = preConditionType;
    }
}
