package org.homunculus.codegen.gradle;

import com.android.build.gradle.AppPlugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.tasks.TaskAction;
import org.homunculus.codegen.GenProject;

import java.io.File;

/**
 * Created by Torben Schinke on 27.02.18.
 */

public class GenTask extends DefaultTask {

    public final static String HCF_GEN_DIR = "/generated/source/hcf/src/main/java";

    @TaskAction
    void doFullTaskAction() throws Exception {
        try {
            AppPlugin android = null;
            for (Plugin plugin : getProject().getPlugins()) {
                if (plugin instanceof AppPlugin) {
                    android = (AppPlugin) plugin;
                    break;
                }
            }

            File hcfDir = new File(getProject().getBuildDir(), HCF_GEN_DIR);
            GenProject gen = new GenProject();
            gen.setProjectRoot(getProject().getProjectDir());
            gen.setManifestPackage(android.getVariantManager().getDefaultConfig().getProductFlavor().getApplicationId());

            for (File f : android.getVariantManager().getDefaultConfig().getSourceSet().getJavaDirectories()) {
                if (f.getAbsolutePath().startsWith(hcfDir.getAbsolutePath())){
                    continue;
                }
                gen.addRecursive(f);
            }
//            for (File f : android.getVariantManager().getDefaultConfig().getSourceSet().getResourcesDirectories()) {
//                gen.addRecursive(f);
//            }
            for (File f : android.getVariantManager().getDefaultConfig().getSourceSet().getResDirectories()) {
                gen.addRecursive(f);
            }

            gen.clearDir(hcfDir);
            hcfDir.mkdirs();

            gen.generate();
            gen.emitGeneratedClass(hcfDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
