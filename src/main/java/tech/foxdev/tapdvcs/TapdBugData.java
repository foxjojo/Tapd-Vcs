package tech.foxdev.tapdvcs;

public class TapdBugData {
    public String DisplayName;
    public String Url;
    public String ID;
    public String CreateName;

    public enum Status {
        New,
        Accept,
        ReOpen
    }

    public Status CurStatus;
    public String OwnName;
}
