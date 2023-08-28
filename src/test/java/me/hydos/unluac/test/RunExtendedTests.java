package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RunExtendedTests {

    private static void gatherTests(Path base, Path folder, List<TestFile> files) throws IOException {
        for (var file : Files.newDirectoryStream(folder, "*.lua")) {
            var relative = base.relativize(file).toString();
            files.add(new TestFile(relative.substring(0, relative.length() - 4)));
        }
        for (var dir : Files.newDirectoryStream(folder)) {
            if (Files.isDirectory(dir)) {
                gatherTests(base, dir, files);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        var fs = FileSystems.getDefault();
        var luatest = fs.getPath(args[0]);
        var report = new TestReport();
        var config = new Configuration();
        for (var version = 0x50; version <= 0x54; version++) {
            var spec = new LuaSpec(version);
            var uspec = new UnluacSpec();
            System.out.println(spec.id());
            for (var subfolder : Files.newDirectoryStream(luatest)) {
                if (Files.isDirectory(subfolder) && spec.compatible(subfolder.getFileName().toString())) {
                    List<TestFile> files = new ArrayList<>();
                    gatherTests(subfolder, subfolder, files);
                    var suite = new TestSuite(subfolder.getFileName().toString(), subfolder + File.separator, files.toArray(new TestFile[0]));
                    System.out.print("\t" + subfolder.getFileName().toString());
                    suite.run(spec, uspec, report, config);
                    System.out.println();
                }
            }
        }
        report.report(System.out);
    }

}
