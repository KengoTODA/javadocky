package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalStorageVersionRepositoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        Path root = folder.newFolder("javadocky").toPath();
        LocalStorageVersionRepository repository = new LocalStorageVersionRepository(root);
        assertFalse(repository.findLatest("g", "a").block().isPresent());

        File groupDir = new File(root.toFile(), "g");
        File artifactDir = new File(groupDir, "a");
        assertTrue(artifactDir.mkdirs());
        assertTrue(new File(artifactDir, "1.0.1").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.0.1")));
        assertTrue(new File(artifactDir, "1.0.0").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.0.1")));
        assertTrue(new File(artifactDir, "1.0.2").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.0.2")));
        assertTrue(new File(artifactDir, "1.1.0").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.1.0")));
        assertTrue(new File(artifactDir, "1.1.0-SNAPSHOT").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.1.0")));
        assertTrue(new File(artifactDir, "1.1.0-sp1").mkdir());
        assertThat(repository.findLatest("g", "a").block().get(),
                is(new DefaultArtifactVersion("1.1.0-sp1")));
    }
}
