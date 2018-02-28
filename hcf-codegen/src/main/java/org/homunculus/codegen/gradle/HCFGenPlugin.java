package org.homunculus.codegen.gradle;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.AndroidBasePlugin;
import com.android.build.gradle.internal.scope.VariantScope;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.Map.Entry;

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

        appPlugin.getVariantManager().getDefaultConfig().getSourceSet().getJava().srcDir(hcfDir);

//        project.afterEvaluate(prj -> {
            createAndroidTasks(project, hcfDir);
//        });

    }

    private void createAndroidTasks(Project project, File hcfDir) {
        project.getTasks().create("genHCF", GenTask.class);

        project.getTasks().getByName("preBuild").dependsOn("genHCF");


    }


}