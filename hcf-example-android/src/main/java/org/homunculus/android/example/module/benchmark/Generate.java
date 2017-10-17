package org.homunculus.android.example.module.benchmark;

import java.io.File;
import java.nio.file.Files;

public class Generate {

    public static void main(String... args) throws Exception {
        File targetDir = new File(new File("").getAbsolutePath(), "hcf-example-android/src/main/java/org/homunculus/android/example/module/benchmark/");
        p("generate in " + targetDir.getAbsolutePath());
        int MAX_CTRS = 100;
        int MAX_METHS = 5;
        for (int i = 0; i < MAX_CTRS; i++) {
            File ctrFile = new File(targetDir, "Controller" + i + ".java");
            Files.write(ctrFile.toPath(), genController(i, MAX_METHS).getBytes());
        }

        File rgFile = new File(targetDir, "Register.java");
        Files.write(rgFile.toPath(), genRegister(MAX_CTRS).getBytes());

    }

    static String genRegister(int no) {
        StringBuilder sb = new StringBuilder();
        sb.append("package org.homunculus.android.example.module.benchmark;\n\n");
        sb.append("import org.homunculusframework.factory.container.Configuration;\n");
        sb.append("\n");
        sb.append("public class Register{\n");
        sb.append("   public static void register(Configuration cfg){\n");
        for (int i = 0; i < no; i++) {
            sb.append("     cfg.add(Controller" + i + ".class);\n");
        }
        sb.append("   }\n");
        sb.append("}\n");
        return sb.toString();
    }

    static String genController(int no, int meths) {
        StringBuilder sb = new StringBuilder();
        sb.append("package org.homunculus.android.example.module.benchmark;\n\n");
        sb.append("import org.springframework.stereotype.Controller;\n");
        sb.append("import org.homunculusframework.navigation.ModelAndView;\n");
        sb.append("import org.springframework.web.bind.annotation.RequestParam;\n");
        sb.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("\n");
        sb.append("@Controller(\"ctr" + no + "\")\n");
        sb.append("public class Controller" + no + " {\n");
        int maxImports = Math.min(5, no);
        for (int i = 0; i < maxImports; i++) {
            sb.append("   @Autowired\n");
            sb.append("   private Controller" + no + " controller" + i + ";\n");
        }
        for (int i = 0; i < meths; i++) {
            sb.append("   @RequestMapping(\"myBackendJob/" + i + "\")\n");
            sb.append("   public ModelAndView doBackendJob" + i + "(@RequestParam(\"param0\") String abc, @RequestParam(\"param1\") String def){\n");
            sb.append("     return new ModelAndView(\"undefined\");\n");
            sb.append("   }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    static void p(String s) {
        System.out.println(s);
    }
}
