package me.hydos.unluac.decompile;

public class AssertionManager {

    //static only
    private AssertionManager() {
    }

    public static boolean assertCritical(boolean condition, String message) {
        if (condition) {
            // okay
        } else {
            critical(message);
        }
        return condition;
    }

    public static void critical(String message) {
        throw new IllegalStateException(message);
    }

}
