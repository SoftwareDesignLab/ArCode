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
 * This class has utility methods to aid the identification of relevant/irrelevant nodes in a statement graph associated with the Spring
 * Framework.
 *
 * @author Joanna, Ali Shokri (as8308@rit.edu)
 */
public class SpringUtils extends FrameworkUtils{
    private static final String SPRING_AUTH_MANAGER_BUILDER = "org/springframework/security/config/annotation/authentication/builders/AuthenticationManagerBuilder";
    private static final String METHOD_CONFIG_AUTH_PROVIDER = "authenticationProvider";

    public static final List<String> SPRING_CLASSES = Arrays.asList(
            "Lorg/springframework/security/authentication/AuthenticationManager",
            "Lorg/springframework/security/core/Authentication",
            "Lorg/springframework/security/config/annotation/authentication/builders/AuthenticationManagerBuilder",
            "Lorg/springframework/security/authentication/AuthenticationProvider",
            "Lorg/springframework/security/authentication/ProviderManager",
            "Lorg/springframework/security/core/context/SecurityContext",
            "Lorg/springframework/security/core/userdetails/UserDetails",
            "Lorg/springframework/security/core/userdetails/UserDetailsService",
            "Lorg/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter",
            "Lorg/springframework/security/authentication/UsernamePasswordAuthenticationToken",
            "Lorg/springframework/security/config/annotation/web/configuration/WebSecurityConfigurerAdapter");

    public SpringUtils(boolean considerOverloadedMethodsAsTheSame) {
        super(considerOverloadedMethodsAsTheSame);
    }

    @Override
    public boolean isFrameworkEntrypoint(Entrypoint entrypoint) {
        return WalaUtils.isMainMethod(entrypoint) ||
                WalaUtils.invokesMethod(entrypoint, SPRING_AUTH_MANAGER_BUILDER ,
                new String[]{METHOD_CONFIG_AUTH_PROVIDER});
    }

    public List<String> getFrameworkClasses(){
        return SPRING_CLASSES;
    }
}
