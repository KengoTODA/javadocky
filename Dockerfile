# build the jar file to use
FROM eclipse-temurin:17-alpine as JAR
COPY . /javadocky/
WORKDIR /javadocky 
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17-alpine as JLINK
RUN jlink \
    --add-modules java.base,java.desktop,java.management,java.xml,java.naming,java.net.http,java.sql,java.instrument,jdk.unsupported,java.rmi \
    --strip-java-debug-attributes \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /jlink

FROM alpine:3.15
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENV JAVA_HOME=/jvm
ENV PATH=${PATH}:${JAVA_HOME}/bin
RUN apk update && apk upgrade
RUN addgroup user && adduser -D -G user -h /home/user -s /bin/bash user && mkdir /home/user/.javadocky && chown -R user:user /home/user/.javadocky
WORKDIR /app
USER user
VOLUME /app/.javadocky

# https://cloud.google.com/run/docs/tips/java#optimization-compiler
ENV JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# for New Relic
ENV NEW_RELIC_APP_NAME="javadocky"
ENV NEW_RELIC_LOG_FILE_NAME="STDOUT"
RUN cd /home/user/.javadocky && \
    wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && rm newrelic-java.zip

COPY --from=JAR /javadocky/build/libs/javadocky-*.jar /app/javadocky.jar
COPY --from=JLINK /jlink $JAVA_HOME
# TODO: resolve `OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended`
# TODO: better to use `-XX:ArchiveClassesAtExit` option? https://www.morling.dev/blog/smaller-faster-starting-container-images-with-jlink-and-appcds/
RUN java -XX:DumpLoadedClassList=/home/user/classes.lst -jar /app/javadocky.jar --appcds && \
    java -Xshare:dump -XX:SharedClassListFile=/home/user/classes.lst -XX:SharedArchiveFile=/home/user/appcds.jsa --class-path /app/javadocky.jar && \
    rm /home/user/classes.lst

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Xshare:on -XX:SharedArchiveFile=/home/user/appcds.jsa -javaagent:/home/user/.javadocky/newrelic/newrelic.jar -jar /app/javadocky.jar" ]
