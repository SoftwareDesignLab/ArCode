package edu.rit.se.design.specminer.graam;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import edu.rit.se.design.specminer.analysis.WalaUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This class has utility methods to aid the identification of relevant/irrelevant nodes in a statement graph.
 *
 * @author Joanna, Ali Shokri (as8308@rit.edu)
 *
 */
public class JAASUtils extends FrameworkUtils{
    private static final String JAAS_LOGIN_CONTEXT = "javax/security/auth/login/LoginContext";

    public JAASUtils(boolean considerOverloadedMethodsAsTheSame) {
        super(considerOverloadedMethodsAsTheSame);
    }

/*    @Override
    public boolean isFrameworkEntrypoint(EntrypointInfo entrypointInfo) {
        return WalaUtils.isMainMethod(entrypointInfo) ||
                WalaUtils.methodInstantiatesClass(entrypointInfo, JAAS_LOGIN_CONTEXT);
    }*/

    @Override
    public boolean isFrameworkEntrypoint(Entrypoint entrypoint) {
        return WalaUtils.isMainMethod(entrypoint) ||
                WalaUtils.methodInstantiatesClass(entrypoint, JAAS_LOGIN_CONTEXT);
    }

    public List<String> getFrameworkClasses(){
        return Arrays.asList( NON_EXCEPTION_JAAS_CLASSES ); /*FRAMEWORK_CLASSES;*/
    }

    public static final String NON_EXCEPTION_JAAS_CLASSES_STRING =
            "Ljavax/security/auth/AuthPermission" + "|" +
                    "Ljavax/security/auth/callback/Callback" + "|" +
                    "Ljavax/security/auth/callback/CallbackHandler" + "|" +
                    "Ljavax/security/auth/callback/ChoiceCallback" + "|" +
                    "Ljavax/security/auth/callback/ConfirmationCallback" + "|" +
                    "Ljavax/security/auth/callback/LanguageCallback" + "|" +
                    "Ljavax/security/auth/callback/NameCallback" + "|" +
                    "Ljavax/security/auth/callback/PasswordCallback" + "|" +
                    "Ljavax/security/auth/callback/TextInputCallback" + "|" +
                    "Ljavax/security/auth/callback/TextOutputCallback" + "|" +
                    "Ljavax/security/auth/Destroyable" + "|" +
                    "Ljavax/security/auth/kerberos/DelegationPermission" + "|" +
                    "Ljavax/security/auth/kerberos/KerberosKey" + "|" +
                    "Ljavax/security/auth/kerberos/KerberosPrincipal" + "|" +
                    "Ljavax/security/auth/kerberos/KerberosTicket" + "|" +
                    "Ljavax/security/auth/kerberos/KeyTab" + "|" +
                    "Ljavax/security/auth/kerberos/ServicePermission" + "|" +
                    "Ljavax/security/auth/login/AppConfigurationEntry" + "|" +
                    "Ljavax/security/auth/login/AppConfigurationEntry/LoginModuleControlFlag" + "|" +
                    "Ljavax/security/auth/login/Configuration" + "|" +
                    "Ljavax/security/auth/login/Configuration/Parameters" + "|" +
                    "Ljavax/security/auth/login/ConfigurationSpi" + "|" +
                    "Ljavax/security/auth/login/LoginContext" + "|" +
                    "Ljavax/security/auth/Policy" + "|" +
                    "Ljavax/security/auth/PrivateCredentialPermission" + "|" +
                    "Ljavax/security/auth/Refreshable" + "|" +
                    "Ljavax/security/auth/spi/LoginModule" + "|" +
                    "Ljavax/security/auth/Subject" + "|" +
                    "Ljavax/security/auth/SubjectDomainCombiner" + "|" +
                    "Ljavax/security/auth/x500/X500Principal" + "|" +
                    "Ljavax/security/auth/x500/X500PrivateCredential" + "|" +
                    "Ljavax/security/cert/Certificate" + "|" +
                    "Ljavax/security/cert/X509Certificate" + "|" +
                    "Ljavax/security/sasl/AuthorizeCallback" + "|" +
                    "Ljavax/security/sasl/RealmCallback" + "|" +
                    "Ljavax/security/sasl/RealmChoiceCallback" + "|" +
                    "Ljavax/security/sasl/Sasl" + "|" +
                    "Ljavax/security/sasl/SaslClient" + "|" +
                    "Ljavax/security/sasl/SaslClientFactory" + "|" +
                    "Ljavax/security/sasl/SaslServer" + "|" +
                    "Ljavax/security/sasl/SaslServerFactory" + "|" +
                    "Ljava/security/AccessControlContext" + "|" +
                    "Ljava/security/AccessController" + "|" +
                    "Ljava/security/PrivilegedAction" + "|" +
                    "java/security/Principal" + "|" +
                    "MyCallbackHandler";

    public static final String[] NON_EXCEPTION_JAAS_CLASSES = new String[]{
            "Ljavax/security/auth/AuthPermission",
            "Ljavax/security/auth/callback/Callback",
            "Ljavax/security/auth/callback/CallbackHandler",
            "Ljavax/security/auth/callback/ChoiceCallback",
            "Ljavax/security/auth/callback/ConfirmationCallback",
            "Ljavax/security/auth/callback/LanguageCallback",
            "Ljavax/security/auth/callback/NameCallback",
            "Ljavax/security/auth/callback/PasswordCallback",
            "Ljavax/security/auth/callback/TextInputCallback",
            "Ljavax/security/auth/callback/TextOutputCallback",
            "Ljavax/security/auth/Destroyable",
            "Ljavax/security/auth/kerberos/DelegationPermission",
            "Ljavax/security/auth/kerberos/KerberosKey",
            "Ljavax/security/auth/kerberos/KerberosPrincipal",
            "Ljavax/security/auth/kerberos/KerberosTicket",
            "Ljavax/security/auth/kerberos/KeyTab",
            "Ljavax/security/auth/kerberos/ServicePermission",
            "Ljavax/security/auth/login/AppConfigurationEntry",
            "Ljavax/security/auth/login/AppConfigurationEntry/LoginModuleControlFlag",
            "Ljavax/security/auth/login/Configuration",
            "Ljavax/security/auth/login/Configuration/Parameters",
            "Ljavax/security/auth/login/ConfigurationSpi",
            "Ljavax/security/auth/login/LoginContext",
            "Ljavax/security/auth/Policy",
            "Ljavax/security/auth/PrivateCredentialPermission",
            "Ljavax/security/auth/Refreshable",
            "Ljavax/security/auth/spi/LoginModule",
            "Ljavax/security/auth/Subject",
            "Ljavax/security/auth/SubjectDomainCombiner",
            "Ljavax/security/auth/x500/X500Principal",
            "Ljavax/security/auth/x500/X500PrivateCredential",
            "Ljavax/security/cert/Certificate",
            "Ljavax/security/cert/X509Certificate",
            "Ljavax/security/sasl/AuthorizeCallback",
            "Ljavax/security/sasl/RealmCallback",
            "Ljavax/security/sasl/RealmChoiceCallback",
            "Ljavax/security/sasl/Sasl",
            "Ljavax/security/sasl/SaslClient",
            "Ljavax/security/sasl/SaslClientFactory",
            "Ljavax/security/sasl/SaslServer",
            "Ljavax/security/sasl/SaslServerFactory",
            "Ljava/security/AccessControlContext",
            "Ljava/security/AccessController",
            "Ljava/security/PrivilegedAction",
            "Ljava/security/Principal",
            "Ljavax.security.auth.Debug",
            "Ljavax.security.auth.login.Debug"
    };

    public static final String[] ALL_JAAS_CLASSES = new String[]{
            "Ljavax/security/auth/AuthPermission",
            "Ljavax/security/auth/callback/Callback",
            "Ljavax/security/auth/callback/CallbackHandler",
            "Ljavax/security/auth/callback/ChoiceCallback",
            "Ljavax/security/auth/callback/ConfirmationCallback",
            "Ljavax/security/auth/callback/LanguageCallback",
            "Ljavax/security/auth/callback/NameCallback",
            "Ljavax/security/auth/callback/PasswordCallback",
            "Ljavax/security/auth/callback/TextInputCallback",
            "Ljavax/security/auth/callback/TextOutputCallback",
            "Ljavax/security/auth/callback/UnsupportedCallbackException",
            "Ljavax/security/auth/Destroyable",
            "Ljavax/security/auth/DestroyFailedException",
            "Ljavax/security/auth/kerberos/DelegationPermission",
            "Ljavax/security/auth/kerberos/KerberosKey",
            "Ljavax/security/auth/kerberos/KerberosPrincipal",
            "Ljavax/security/auth/kerberos/KerberosTicket",
            "Ljavax/security/auth/kerberos/KeyTab",
            "Ljavax/security/auth/kerberos/ServicePermission",
            "Ljavax/security/auth/login/AccountException",
            "Ljavax/security/auth/login/AccountExpiredException",
            "Ljavax/security/auth/login/AccountLockedException",
            "Ljavax/security/auth/login/AccountNotFoundException",
            "Ljavax/security/auth/login/AppConfigurationEntry",
            "Ljavax/security/auth/login/AppConfigurationEntry/LoginModuleControlFlag",
            "Ljavax/security/auth/login/Configuration",
            "Ljavax/security/auth/login/Configuration/Parameters",
            "Ljavax/security/auth/login/ConfigurationSpi",
            "Ljavax/security/auth/login/CredentialException",
            "Ljavax/security/auth/login/CredentialExpiredException",
            "Ljavax/security/auth/login/CredentialNotFoundException",
            "Ljavax/security/auth/login/FailedLoginException",
            "Ljavax/security/auth/login/LoginContext",
            "Ljavax/security/auth/login/LoginException",
            "Ljavax/security/auth/Policy",
            "Ljavax/security/auth/PrivateCredentialPermission",
            "Ljavax/security/auth/Refreshable",
            "Ljavax/security/auth/RefreshFailedException",
            "Ljavax/security/auth/spi/LoginModule",
            "Ljavax/security/auth/Subject",
            "Ljavax/security/auth/SubjectDomainCombiner",
            "Ljavax/security/auth/x500/X500Principal",
            "Ljavax/security/auth/x500/X500PrivateCredential",
            "Ljavax/security/cert/Certificate",
            "Ljavax/security/cert/CertificateEncodingException",
            "Ljavax/security/cert/CertificateException",
            "Ljavax/security/cert/CertificateExpiredException",
            "Ljavax/security/cert/CertificateNotYetValidException",
            "Ljavax/security/cert/CertificateParsingException",
            "Ljavax/security/cert/X509Certificate",
            "Ljavax/security/sasl/AuthenticationException",
            "Ljavax/security/sasl/AuthorizeCallback",
            "Ljavax/security/sasl/RealmCallback",
            "Ljavax/security/sasl/RealmChoiceCallback",
            "Ljavax/security/sasl/Sasl",
            "Ljavax/security/sasl/SaslClient",
            "Ljavax/security/sasl/SaslClientFactory",
            "Ljavax/security/sasl/SaslException",
            "Ljavax/security/sasl/SaslServer",
            "Ljavax/security/sasl/SaslServerFactory"
    };
}
