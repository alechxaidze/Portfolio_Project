package model;

public enum EventType {
    CRASH("Market Crash"),
    HACK("Security Hack"),
    SCANDAL("Corporate Scandal"),
    COURT("Court Decision"),
    EARNINGS("Earnings Report"),
    DIVIDEND("Dividend Announcement"),
    SPLIT("Stock Split"),
    MERGER("Merger/Acquisition"),
    OTHER("Other Event");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
