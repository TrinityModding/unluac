package me.hydos.unluac.test.legacy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TestReport {

    private final List<String> failedTests = new ArrayList<>();
    private final List<String> skippedTests = new ArrayList<>();
    private int passed = 0;
    private int failed = 0;
    private int skipped = 0;

    public void report(PrintStream out) {
        if (failed == 0 && skipped == 0) {
            out.println("All tests passed!");
        } else {
            for (var failed : failedTests)
                out.println("Failed: " + failed);

            for (var skipped : skippedTests) {
                out.println("Skipped: " + skipped);
            }
            out.println("Failed " + failed + " of " + (failed + passed) + " tests, skipped " + skipped + " tests.");
        }
    }

    public void result(String test, TestResult result) {
        switch (result) {
            case OK -> passed++;
            case FAILED -> {
                failedTests.add(test);
                failed++;
            }
            case SKIPPED -> {
                skippedTests.add(test);
                skipped++;
            }
            default -> throw new IllegalStateException();
        }
    }

}
