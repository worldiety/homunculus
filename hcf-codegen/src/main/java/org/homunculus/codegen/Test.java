package org.homunculus.codegen;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class Test {

    public static void main(String... args) throws Exception {
        /*File projectDir = new File(getCwd().getParentFile(), "hcf-example-android/src/main");
        LoggerFactory.getLogger(Test.class).info("dir={}", projectDir);
        GenProject project = new GenProject();
        project.setProjectRoot(getCwd().getParentFile());
        project.addRecursive(projectDir);
        project.setManifestPackage("org.homunculus.android.example");
        project.generate();


        File outDir = new File(getCwd().getParentFile(), "hcf-example-android/build/generated/source/hcf");
        delete(outDir);
        outDir.mkdirs();
        project.emitGeneratedClass(outDir);

         */
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete() && f.exists())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    static File getCwd() {
        return new File("").getAbsoluteFile();
    }
}
