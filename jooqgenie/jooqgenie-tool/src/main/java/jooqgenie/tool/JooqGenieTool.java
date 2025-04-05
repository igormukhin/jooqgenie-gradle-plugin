package jooqgenie.tool;


import java.nio.file.Path;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.codegen.JavaGenerator;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.jooq.meta.postgres.PostgresDatabase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class JooqGenieTool {

    private static final String POSTGRESQL_DRIVER_CLASS = "org.postgresql.Driver";

    private final Path migrationsDir;
    private final Path outputDir;
    private final String targetPackage;
    private final String databaseImage;

    public static void main(String[] args) throws Exception {
        fromSystemProperties().execute();
    }

    public JooqGenieTool(Path migrationsDir, Path outputDir, String targetPackage, String databaseImage) {
        this.migrationsDir = migrationsDir;
        this.outputDir = outputDir;
        this.targetPackage = targetPackage;
        this.databaseImage = databaseImage;
    }

    public static JooqGenieTool fromSystemProperties() {
        Path migrationsDir = Path.of(System.getProperty("migrationsDir"));
        Path outputDir = Path.of(System.getProperty("outputDir"));
        String targetPackage = System.getProperty("targetPackage");
        String databaseImage = System.getProperty("databaseImage");

        return new JooqGenieTool(migrationsDir, outputDir, targetPackage, databaseImage);
    }

    private void log(String str) {
        System.out.println(str);
    }

    public void execute() throws Exception {
        var image = DockerImageName.parse(databaseImage);
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(image)) {
            log("Starting Postgres container...");
            postgres.start();

            populateDatabase(postgres);

            generateJooqSources(postgres);
        }
    }

    private void populateDatabase(PostgreSQLContainer<?> postgres) {
        log("Executing Flyway scripts...");

        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("filesystem:" + migrationsDir)
                .load();

        flyway.migrate();
    }

    private void generateJooqSources(PostgreSQLContainer<?> postgres) throws Exception {
        log("Generating jOOQ sources...");

        Jdbc jdbc = new Jdbc().withDriver(POSTGRESQL_DRIVER_CLASS)
                .withUrl(postgres.getJdbcUrl())
                .withUser(postgres.getUsername())
                .withPassword(postgres.getPassword());

        Database database = new Database().withName(PostgresDatabase.class.getName()).withInputSchema("public");

        Target target = new Target().withPackageName(targetPackage)
                .withClean(true)
                .withDirectory(outputDir.toString());

        Generate options = new Generate().withDaos(true).withPojos(true);

        Generator generator = new Generator().withName(JavaGenerator.class.getName())
                .withDatabase(database)
                .withGenerate(options)
                .withTarget(target);

        Configuration configuration = new Configuration().withJdbc(jdbc).withGenerator(generator);

        GenerationTool.generate(configuration);
    }

}
