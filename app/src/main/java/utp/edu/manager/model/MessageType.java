package utp.edu.manager.model;

public enum MessageType {

    RECEIVED(0),
    SENT(1);

    private final int value;

    MessageType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
