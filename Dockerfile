# https://spring.io/guides/gs/spring-boot-docker/

# build the jar file to use
FROM adoptopenjdk/openjdk11:alpine
COPY . /javadocky/
RUN cd /javadocky && ./gradlew assemble

FROM adoptopenjdk/openjdk11:alpine as runtime
RUN jlink \
    --add-modules java.base,java.desktop,java.management,java.xml,java.naming,java.net.http,java.sql \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /jlink && \
    rm -rf ${JAVA_HOME} && \
    mv -f /jlink ${JAVA_HOME}
RUN addgroup user && adduser -D -G user -h /home/user -s /bin/bash user && mkdir /home/user/.javadocky && chown -R user:user /home/user/.javadocky
WORKDIR /home/user
USER user
VOLUME /home/user/.javadocky
COPY --from=0 --chown=user:user /javadocky/build/libs/javadocky-*.jar /home/user/javadocky.jar
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /home/user/javadocky.jar" ]
