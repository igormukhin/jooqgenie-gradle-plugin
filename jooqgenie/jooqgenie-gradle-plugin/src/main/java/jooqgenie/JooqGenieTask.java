package jooqgenie;


import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public class JooqGenieTask extends DefaultTask {

    private final DirectoryProperty migrationsDir;
    private final DirectoryProperty outputDir;
    private final Property<String> targetPackage;
    private final Property<String> databaseImage;
    private final ConfigurableFileCollection runtimeConfiguration;

    private final ExecOperations execOperations;

    @Inject
    public JooqGenieTask(ExecOperations execOperations) {
        this.execOperations = execOperations;

        var objects = getProject().getObjects();
        migrationsDir = objects.directoryProperty();
        outputDir = objects.directoryProperty();
        targetPackage = objects.property(String.class);
        databaseImage = objects.property(String.class);
        runtimeConfiguration = objects.fileCollection();
    }

    @InputDirectory
    public DirectoryProperty getMigrationsDir() {
        return migrationsDir;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @Input
    public Property<String> getTargetPackage() {
        return targetPackage;
    }

    @Input
    public Property<String> getDatabaseImage() {
        return databaseImage;
    }

    @InputFiles
    public ConfigurableFileCollection getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    @TaskAction
    public void execute() {
        var bufferOut = new ByteArrayOutputStream();

        var result = execOperations.javaexec(spec -> {
            spec.setClasspath(runtimeConfiguration);
            spec.getMainClass().set("jooqgenie.tool.JooqGenieTool");
            spec.systemProperty("migrationsDir", migrationsDir.get().getAsFile().getAbsolutePath());
            spec.systemProperty("outputDir", outputDir.get().getAsFile().getAbsolutePath());
            spec.systemProperty("targetPackage", targetPackage.get());
            spec.systemProperty("databaseImage", databaseImage.get());
            spec.setIgnoreExitValue(true);
            spec.setStandardOutput(bufferOut);

        });

        if (getLogger().isInfoEnabled()) {
            getLogger().info(bufferOut.toString());
        }

        if (result.getExitValue() != 0) {
            throw new GradleException("JooqGenieTool execution failed with exit code: " + result.getExitValue());
        }
    }

}
