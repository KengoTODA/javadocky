package jp.skypencil.javadocky;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.springframework.lang.NonNull;

public class ArtifactName {
  @NotNull
  @Pattern(regexp = "^[a-z0-9\\.]*$")
  private final String groupId;

  @NotNull
  @Pattern(regexp = "^[a-z0-9\\.]*$")
  private final String artifactId;

  @Pattern(regexp = "^[a-z0-9\\.]*$")
  private final String version;

  public ArtifactName(@NonNull String groupId, @NonNull String artifactId) {
    this(groupId, artifactId, null);
  }

  public ArtifactName(
      @NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  @NonNull
  public String getGroupId() {
    return groupId;
  }

  @NonNull
  public String getArtifactId() {
    return artifactId;
  }

  @NonNull
  public Optional<String> getVersion() {
    return Optional.ofNullable(version);
  }

  @Override
  public String toString() {
    if (version == null) {
      return String.format("%s:%s", groupId, artifactId);
    } else {
      return String.format("%s:%s:%s", groupId, artifactId, version);
    }
  }
}
