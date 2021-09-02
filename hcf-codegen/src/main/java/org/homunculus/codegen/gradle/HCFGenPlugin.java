package org.homunculus.codegen.gradle;



//import com.android.build.gradle.AppPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

import java.io.File;

public class HCFGenPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        File hcfDir = new File(project.getBuildDir(), GenTask.HCF_GEN_DIR);
        if (!hcfDir.mkdirs() && !hcfDir.isDirectory()) {
            throw new RuntimeException("failed to create directory " + hcfDir);
        }

/*
        AppPlugin appPlugin = null;
        for (Plugin plugin : project.getPlugins()) {
            System.out.println(plugin.getClass() + "(" + plugin + ")");
            if (plugin instanceof AppPlugin) {
                appPlugin = (AppPlugin) plugin;
            }
        }


 */
/*
        if (appPlugin == null) {
            throw new RuntimeException("no android AppPlugin found");
        }



 */

       // appPlugin.getVariantManager().getDefaultConfig().getSourceSet().getJava().srcDir(hcfDir);

//        project.afterEvaluate(prj -> {
            createAndroidTasks(project, hcfDir);
//        });

    }

    private void createAndroidTasks(Project project, File hcfDir) {
        project.getTasks().create("genHCF", GenTask.class);

        project.getTasks().getByName("preBuild").dependsOn("genHCF");


    }


}