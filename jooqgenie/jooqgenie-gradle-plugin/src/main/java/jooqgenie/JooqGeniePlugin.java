package jooqgenie;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

public class JooqGeniePlugin implements Plugin<Project> {

    public static final String JOOQ_GENIE_EXTENSION_NAME = "jooqGenie";
    public static final String JOOQ_GENIE_TASK_NAME = "generateJooqClassesFromFlyway";

    @Override
    public void apply(Project project) {
        JooqGenieExtension extension = project.getExtensions().create(JOOQ_GENIE_EXTENSION_NAME, JooqGenieExtension.class);
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        var jooqGenieToolConfig = project.getConfigurations().create("jooqGenieToolRuntime", cfg -> {
            cfg.setCanBeConsumed(false);
            cfg.setCanBeResolved(true);
            cfg.setDescription("Classpath for jOOQ Genie tool");
            cfg.setVisible(false);
            cfg.getDependencies().add(project.getDependencies().create("jooqgenie:jooqgenie-tool:1.0.0"));
        });

        TaskProvider<JooqGenieTask> generateJooqTask = project.getTasks().register(JOOQ_GENIE_TASK_NAME, JooqGenieTask.class, task -> {
            task.setGroup("other");
            task.setDescription("Generates jOOQ classes from Flyway/Postgres schema");

            task.getMigrationsDir()
                    .convention(project.getLayout().getProjectDirectory().dir("src/main/resources/db/migration"));
            if (extension.getMigrationsDir().isPresent()) {
                task.getMigrationsDir().set(extension.getMigrationsDir());
            }

            task.getOutputDir()
                    .convention(project.getLayout().getBuildDirectory().dir("generated/sources/jooq/java/main"));
            if (extension.getOutputDir().isPresent()) {
                task.getOutputDir().set(extension.getOutputDir());
            }

            task.getTargetPackage()
                    .set(extension.getTargetPackage());

            task.getDatabaseImage()
                    .convention("postgres:latest");
            if (extension.getDatabaseImage().isPresent()) {
                task.getDatabaseImage().set(extension.getDatabaseImage());
            }

            task.getRuntimeConfiguration()
                    .setFrom(jooqGenieToolConfig);
        });

        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME,
                compileJavaTask -> compileJavaTask.dependsOn(generateJooqTask));

        javaExtension.getSourceSets().named(SourceSet.MAIN_SOURCE_SET_NAME).configure(
                sourceSet -> sourceSet.getJava().srcDir(generateJooqTask.flatMap(JooqGenieTask::getOutputDir)));
    }

}