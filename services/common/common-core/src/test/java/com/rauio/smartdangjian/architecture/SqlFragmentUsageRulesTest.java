package com.rauio.smartdangjian.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SqlFragmentUsageRulesTest {

    private static final List<String> ALLOWED_SQL_FRAGMENT_LINES = List.of(
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:42",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:48",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:54",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:66",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:67",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:73",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:74",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:80",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/support/DefaultLikeTargetGateway.java:81",
            "services/social/src/main/java/com/rauio/smartdangjian/server/social/service/CommentService.java:74",
            "services/course/src/main/java/com/rauio/smartdangjian/server/course/api/CourseQueryFacadeImpl.java:79",
            "services/course/src/main/java/com/rauio/smartdangjian/server/course/service/course/CourseService.java:249",
            "services/learning/src/main/java/com/rauio/smartdangjian/server/learning/service/UserLearningRecordService.java:221",
            "services/learning/src/main/java/com/rauio/smartdangjian/server/learning/service/UserLearningRecordService.java:243");

    @Test
    @DisplayName("MyBatis SQL fragment APIs must stay on the reviewed allowlist")
    void sqlFragmentApiUsageIsAllowlisted() throws IOException {
        Path root = projectRoot();
        List<String> actual = Files.walk(root.resolve("services"))
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> path.toString().contains("/src/main/java/"))
                .filter(path -> !path.toString().contains("/build/"))
                .flatMap(path -> matchingLines(root, path).stream())
                .sorted()
                .toList();

        assertThat(actual)
                .containsExactlyElementsOf(
                        ALLOWED_SQL_FRAGMENT_LINES.stream().sorted().toList());
    }

    private static List<String> matchingLines(Path root, Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            String relativePath = root.relativize(path).toString();
            return java.util.stream.IntStream.range(0, lines.size())
                    .filter(index -> containsSqlFragmentApi(lines.get(index)))
                    .mapToObj(index -> relativePath + ":" + (index + 1))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private static boolean containsSqlFragmentApi(String line) {
        return line.contains(".last(") || line.contains(".apply(") || line.contains(".setSql(");
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Cannot locate project root");
        }
        return current;
    }
}
