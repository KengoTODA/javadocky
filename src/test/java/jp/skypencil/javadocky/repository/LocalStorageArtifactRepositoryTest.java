package jp.skypencil.javadocky.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;

class LocalStorageArtifactRepositoryTest {
  @Test
  void testNoDir(@TempDir File root) throws IOException {
    LocalStorageArtifactRepository repo = new LocalStorageArtifactRepository(root.toPath());
    StepVerifier.create(repo.list("foo")).expectComplete().verify();
  }

  @Test
  void testEmpty(@TempDir File root) throws IOException {
    LocalStorageArtifactRepository repo = new LocalStorageArtifactRepository(root.toPath());
    assertTrue(new File(root, "foo").mkdir());
    StepVerifier.create(repo.list("foo")).expectComplete().verify();
  }

  @Test
  void test(@TempDir File root) throws IOException {
    LocalStorageArtifactRepository repo = new LocalStorageArtifactRepository(root.toPath());
    File groupDir = new File(root, "group");
    assertTrue(groupDir.mkdir());
    assertTrue(new File(groupDir, "artifact-1").mkdir());
    assertTrue(new File(groupDir, "artifact-2").mkdir());
    StepVerifier.create(repo.list("group"))
        .expectNext("artifact-1")
        .expectNext("artifact-2")
        .expectComplete()
        .verify();
  }
}
