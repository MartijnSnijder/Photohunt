package snijder.martijn.photohunt.models;


public class User {

    private String name;
    private String email;
    private String facebookID;
    private String unique_id;
    private String password;
    private String old_password;
    private String new_password;
    private String code;
    private String hunt;

    public String getHunt() {return hunt;}

    public void setHunt(String hunt) {this.hunt = hunt;}

    public String getName() {return name;}

    public String getEmail() { return email;}

    public String getUnique_id() {return unique_id; }

    public String getFacebookID() {return facebookID; }

    public String getPassword() {return password; }

    public void setName(String name) { this.name = name;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOld_password(String old_password) {
        this.old_password = old_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFacebookID(String facebookID) { this.facebookID = facebookID; }



}
