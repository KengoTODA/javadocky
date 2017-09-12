package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
class LocalStorageArtifactRepository implements ArtifactRepository {
    @NonNull
    private final Path root;

    @Override
    @NonNull
    public Flux<String> list(@NonNull String groupId) {
        File groupDir = root.resolve(groupId + "/").toFile();
        if (!groupDir.isDirectory()) {
            return Flux.empty();
        }
        File[] directories = groupDir.listFiles(File::isDirectory);
        if (directories == null) {
            return Flux.error(new IOException("Unknown error occured during listing directory"));
        } else {
            return Flux.fromArray(directories).map(File::getName).sort();
        }
    }
}
