package ch.ethz.jadabs.im.db.auto;

/** Class _Account was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _Account extends org.objectstyle.cayenne.CayenneDataObject {

    public static final String EMAIL_PROPERTY = "email";
    public static final String MOBILE_PHONE_PROPERTY = "mobilePhone";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String PREF_PROPERTY = "pref";
    public static final String SYSTEM_EMAIL_PROPERTY = "systemEmail";
    public static final String USERNAME_PROPERTY = "username";

    public static final String SYSTEM_EMAIL_PK_COLUMN = "system_email";

    public void setEmail(String email) {
        writeProperty("email", email);
    }
    public String getEmail() {
        return (String)readProperty("email");
    }
    
    
    public void setMobilePhone(String mobilePhone) {
        writeProperty("mobilePhone", mobilePhone);
    }
    public String getMobilePhone() {
        return (String)readProperty("mobilePhone");
    }
    
    
    public void setPassword(String password) {
        writeProperty("password", password);
    }
    public String getPassword() {
        return (String)readProperty("password");
    }
    
    
    public void setPref(Integer pref) {
        writeProperty("pref", pref);
    }
    public Integer getPref() {
        return (Integer)readProperty("pref");
    }
    
    
    public void setSystemEmail(String systemEmail) {
        writeProperty("systemEmail", systemEmail);
    }
    public String getSystemEmail() {
        return (String)readProperty("systemEmail");
    }
    
    
    public void setUsername(String username) {
        writeProperty("username", username);
    }
    public String getUsername() {
        return (String)readProperty("username");
    }
    
    
}
