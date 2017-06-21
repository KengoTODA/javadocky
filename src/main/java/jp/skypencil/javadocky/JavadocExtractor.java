package jp.skypencil.javadocky;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * This class is responsible to download javadoc.jar and unzip its contents onto {@link Storage}.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
class JavadocExtractor {
    @NonNull
    private final JavadocDownloader downloader;

    @NonNull
    private final Storage storage;

    Mono<Void> extract(String groupId, String artifactId, String version) {
        AtomicInteger count = new AtomicInteger(0);
        return downloader.download(groupId, artifactId, version).flatMapMany(downloaded -> {
                if (!downloaded.isPresent()) {
                    String message = String.format("Javadoc.jar not found for %s:%s:%s", groupId, artifactId, version);
                    return Mono.error(new IllegalArgumentException(message));
                }
                return unzip(downloaded.get());
            }).flatMap(nameAndData -> {
                // FIXME this flatMap makes Flux not terminated
                String name = nameAndData.getT1();
                log.info("Count: {}, name: {}", count.incrementAndGet(), name);
                Flux<ByteBuffer> data = nameAndData.getT2();
                return storage.write(groupId, artifactId, version, name, data);
            })
            .doFinally(signal -> {
                log.info("Closed, signal is {}", signal);
            })
            .count()
            .ofType(Void.class)
            .doFinally(signal -> {
                log.info("Closed, signal is {}", signal);
            });
    }

    Flux<Tuple2<String, Flux<ByteBuffer>>> unzip(File file) {
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException e) {
            return Flux.error(e);
        }
        return Flux.<Tuple2<String, Flux<ByteBuffer>>>from(subscriber -> {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                try {
                    InputStream input = zip.getInputStream(entry);
                    subscriber.onNext(handleEntry(entry, input));
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                ++count;
            }
            log.warn("COUNT {}", count);
            subscriber.onComplete();
        }).doFinally(signal -> {
            try {
                zip.close();
            } catch (IOException e) {
                log.warn("Failed to close ZIP input stream", e);
            }
        });
    }

    private Tuple2<String, Flux<ByteBuffer>> handleEntry(ZipEntry entry, InputStream input) throws IOException {
        String name = entry.getName();
        // FIXME We're using blocking I/O and on-memory storage, try to find how to handle ZIP entry based on Reactor
        ByteBuffer byteBuffer;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            byteBuffer = ByteBuffer.wrap(output.toByteArray());
        } finally {
            input.close();
        }

        return Tuples.of(name, Flux.just(byteBuffer));
    }
}
