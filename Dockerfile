# https://spring.io/guides/gs/spring-boot-docker/

# build the jar file to use
FROM adoptopenjdk/openjdk11:alpine
COPY . /javadocky/
RUN cd /javadocky && ./gradlew assemble

FROM amazoncorretto:11 as jlink
RUN jlink \
    --add-modules java.base,java.desktop,java.management,java.xml,java.naming,java.net.http,java.sql \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /jlink

FROM amazonlinux:2
RUN mkdir -p /javadocky/.javadocky
WORKDIR /javadocky
ENV JAVA_HOME=/opt/jre
ENV PATH=${PATH}:${JAVA_HOME}/bin
VOLUME /javadocky/.javadocky
COPY --from=0 /javadocky/build/libs/javadocky-*.jar /javadocky/javadocky.jar
COPY --from=1 /jlink $JAVA_HOME
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /javadocky/javadocky.jar" ]
