package jp.skypencil.javadocky;

import lombok.NonNull;
import reactor.core.publisher.Flux;

/**
 * <p>A repository which handles artifact information.</p>
 * 
 * @author kengo
 */
public interface ArtifactRepository {
    /**
     * @param groupId Target {@code groupId} to list up {@code artifactId}.
     * @return a non-null {@link Flux} of {@code artifactId} belonging to specified {@code groupId}.
     */
    @NonNull
    Flux<String> list(@NonNull String groupId);
}
