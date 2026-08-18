package kr.ac.knue.cms.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TechnologyStackArchitectureTest {
    private static final Path PROJECT_ROOT = Path.of("");
    private static final Path SOURCE_ROOT = PROJECT_ROOT.resolve("src/main/java");

    @Test
    void backend_stack_is_java_17_spring_boot_33_mybatis_and_executable_boot_jar() throws IOException {
        String pom = Files.readString(PROJECT_ROOT.resolve("pom.xml"), StandardCharsets.UTF_8);

        assertThat(pom).contains("<java.version>17</java.version>");
        assertThat(pom).contains("<version>3.3.5</version>");
        assertThat(pom).contains("<artifactId>mybatis-spring-boot-starter</artifactId>");
        assertThat(pom).contains("<artifactId>postgresql</artifactId>");
        assertThat(pom).contains("<artifactId>spring-boot-maven-plugin</artifactId>");
        assertThat(pom).contains("<goal>repackage</goal>");
    }

    @Test
    void forbidden_reactive_and_jpa_persistence_dependencies_are_not_used() throws IOException {
        String pom = Files.readString(PROJECT_ROOT.resolve("pom.xml"), StandardCharsets.UTF_8);
        List<String> forbiddenDependencies = List.of(
            "spring-boot-starter-data-jpa",
            "spring-boot-starter-data-r2dbc",
            "hibernate-core",
            "r2dbc-postgresql",
            "spring-data-r2dbc"
        );

        assertThat(pom).doesNotContain(forbiddenDependencies.toArray(String[]::new));
        assertThat(allProductionSource()).doesNotContain(
            "JpaRepository",
            "EntityManager",
            "jakarta.persistence",
            "javax.persistence",
            "R2dbcEntityTemplate",
            "ReactiveCrudRepository",
            "reactor.core.publisher.Mono",
            "reactor.core.publisher.Flux"
        );
    }

    @Test
    void production_persistence_adapters_are_blocking_mybatis_mappers() throws IOException {
        String source = allProductionSource();

        assertThat(source).contains("org.apache.ibatis.annotations.Mapper");
        assertThat(source).contains("@Mapper");
        assertThat(source).contains("@Select");
        assertThat(source).doesNotContain("suspend fun", "CompletableFuture<", "Mono<", "Flux<");
    }

    private String allProductionSource() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                builder.append(Files.readString(path, StandardCharsets.UTF_8)).append('\n');
            }
        }
        return builder.toString();
    }
}
