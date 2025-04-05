package jooqgenie;


import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class JooqGenieExtension {
    private final DirectoryProperty migrationsDir;
    private final DirectoryProperty outputDir;
    private final Property<String> targetPackage;
    private final Property<String> databaseImage;

    @Inject
    public JooqGenieExtension(ObjectFactory objects) {
        this.migrationsDir = objects.directoryProperty();
        this.outputDir = objects.directoryProperty();
        this.targetPackage = objects.property(String.class);
        this.databaseImage = objects.property(String.class);
    }

    public DirectoryProperty getMigrationsDir() {
        return migrationsDir;
    }

    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    public Property<String> getTargetPackage() {
        return targetPackage;
    }

    public Property<String> getDatabaseImage() {
        return databaseImage;
    }
}