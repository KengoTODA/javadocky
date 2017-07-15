package jp.skypencil.javadocky;

import java.io.File;
import java.nio.file.Path;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
class LocalStorageArtifactRepository implements ArtifactRepository {
    @NonNull
    private final Path root;

    @Override
    public Flux<String> list(String groupId) {
        File groupDir = root.resolve(groupId + "/").toFile();
        if (!groupDir.isDirectory()) {
            return Flux.empty();
        }
        return Flux.fromArray(groupDir.listFiles(File::isDirectory)).map(File::getName);
    }
}
