package org.homunculus.codegen.gradle;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.internal.tasks.IncrementalTask;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.homunculus.codegen.GenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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
                System.out.println(plugin.getClass() + "(" + plugin + ")");
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
                gen.addRecursive(f);
            }
            for (File f : android.getVariantManager().getDefaultConfig().getSourceSet().getResourcesDirectories()) {
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
