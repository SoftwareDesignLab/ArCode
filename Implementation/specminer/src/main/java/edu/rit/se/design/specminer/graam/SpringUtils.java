package edu.rit.se.design.specminer.graam;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import edu.rit.se.design.specminer.analysis.WalaUtils;

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
