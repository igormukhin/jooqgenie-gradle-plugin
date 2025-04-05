# jooqgenie-gradle-plugin

An example of a Gradle plugin that generates jOOQ Java classes for a Postgres database schema that was initialized with Flyway.

## How it works

* Starts a Postgres database container.
* Runs Flyway migrations to initialize the database schema.
* Generates jOOQ Java classes from the database schema using the jOOQ code generator.
* Cleans up the database container after the code generation is complete.

## Run

To see it in action, run:

```
./gradlew build
```

The jOOQ sources will be generated in `sample-project/build/generated/sources/jooq`.

## Usage

Full example in `sample-project/`:
```groovy
plugins {
    id 'java'
    id 'jooqGenie'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation libs.jooq
}

jooqGenie {
/*
    Defaults:
    migrationsDir = layout.projectDirectory.dir('src/main/resources/db/migration')
    outputDir = layout.buildDirectory.dir('generated/sources/jooq/java/main')
    databaseImage = 'postgres:latest'
*/
    targetPackage = 'example.jooq'
}
```
