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

/**
 *
 * @author Joanna C/ S/ Santos <jds5109@rit/edu>
 */
public class JAASClasses {

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
            "Ljavax/security/sasl/SaslServerFactory"
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
