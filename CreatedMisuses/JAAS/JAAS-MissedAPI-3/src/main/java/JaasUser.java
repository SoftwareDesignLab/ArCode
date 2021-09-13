import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class JaasUser {
    public static void main(String[] args) {
        JaasUser jaasUser = new JaasUser();
        CallbackHandler callbackHandler = jaasUser.createCallBackHandler();
        try {
            LoginContext loginContext = jaasUser.createLoginContext(callbackHandler);
            jaasUser.login(loginContext);
            jaasUser.printSubject(loginContext);
            jaasUser.logout(loginContext);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public CallbackHandler createCallBackHandler() {
        return new MyCallbackHandler();
    }

    public LoginContext createLoginContext(CallbackHandler callbackHandler) throws LoginException {
        return new LoginContext("", callbackHandler);
    }

    public void login(LoginContext loginContext) throws LoginException {
        // Programmer forgets to implement this method!
    }

    public void printSubject(LoginContext loginContext) {
        System.out.println(loginContext.getSubject());
    }

    public void logout(LoginContext loginContext) throws LoginException {
        loginContext.logout();
    }
}
