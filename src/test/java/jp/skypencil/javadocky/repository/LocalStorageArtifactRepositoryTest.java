package jp.skypencil.javadocky.repository;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import reactor.test.StepVerifier;

public class LocalStorageArtifactRepositoryTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testNoDir() throws IOException {
    File root = folder.newFolder();
    LocalStorageArtifactRepository repo = new LocalStorageArtifactRepository(root.toPath());
    StepVerifier.create(repo.list("foo")).expectComplete().verify();
  }

  @Test
  public void testEmpty() throws IOException {
    File root = folder.newFolder();
    LocalStorageArtifactRepository repo = new LocalStorageArtifactRepository(root.toPath());
    assertTrue(new File(root, "foo").mkdir());
    StepVerifier.create(repo.list("foo")).expectComplete().verify();
  }

  @Test
  public void test() throws IOException {
    File root = folder.newFolder();
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
