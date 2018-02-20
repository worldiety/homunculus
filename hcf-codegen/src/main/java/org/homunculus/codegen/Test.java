package org.homunculus.codegen;

import org.homunculus.codegen.generator.GenerateAutoDiscovery;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class Test {

    public static void main(String... args) throws Exception {
        File projectDir = new File(getCwd().getParentFile(), "hcf-example-android/src/main/java");
        LoggerFactory.getLogger(Test.class).info("dir={}", projectDir);
        Project project = new Project();
        project.addRecursive(projectDir);
        project.generate();


        File outDir = new File(getCwd().getParentFile(), "hcf-example-android/build/generated/source/hcf");
        delete(outDir);
        outDir.mkdirs();
        project.emitGeneratedClass(outDir);
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    static File getCwd() {
        return new File("").getAbsoluteFile();
    }
}
