package org.homunculus.codegen.gradle;




import android.content.pm.ComponentInfo;

import com.android.build.gradle.internal.DefaultConfigData;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.plugins.AppPlugin;
import com.android.build.gradle.internal.plugins.LibraryPlugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.io.File;

public class HCFGenPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        File hcfDir = new File(project.getBuildDir(), GenTask.HCF_GEN_DIR);
        if (!hcfDir.mkdirs() && !hcfDir.isDirectory()) {
            throw new RuntimeException("failed to create directory " + hcfDir);
        }

        AppPlugin appPlugin = null;
        for (Plugin plugin : project.getPlugins()) {
            System.out.println(plugin.getClass() + "(" + plugin + ")");
            if (plugin instanceof AppPlugin) {
                appPlugin = (AppPlugin) plugin;
            }
        }



        if (appPlugin == null) {
            throw new RuntimeException("no android AppPlugin found");
        }


        appPlugin.getVariantInputModel().getDefaultConfigData().getSourceSet().getJava().srcDir(hcfDir);

        LoggerFactory.getLogger("TEST").info(hcfDir.toString());

        //appPlugin.getVariantManager().getDefaultConfig().getSourceSet().getJava().srcDir(hcfDir);

//        project.afterEvaluate(prj -> {
            createAndroidTasks(project, hcfDir);
//        });

    }

    private void createAndroidTasks(Project project, File hcfDir) {
        project.getTasks().create("genHCF", GenTask.class);

        project.getTasks().getByName("preBuild").dependsOn("genHCF");


    }


}