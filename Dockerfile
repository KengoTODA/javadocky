# build the jar file to use
FROM eclipse-temurin:17-alpine
COPY . /javadocky/
RUN cd /javadocky && ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17-alpine
RUN apk update && apk upgrade
RUN addgroup user && adduser -D -G user -h /home/user -s /bin/bash user && mkdir /home/user/.javadocky && chown -R user:user /home/user/.javadocky
WORKDIR /app
USER user
VOLUME /app/.javadocky

# for New Relic
ENV NEW_RELIC_APP_NAME="javadocky"
ENV NEW_RELIC_LOG_FILE_NAME="STDOUT"
RUN cd /home/user/.javadocky && \
    wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && rm newrelic-java.zip

COPY --from=0 /javadocky/build/libs/javadocky-*.jar /app/javadocky.jar
RUN java -XX:DumpLoadedClassList=/home/user/classes.lst -jar /app/javadocky.jar --appcds && \
    java -Xshare:dump -XX:SharedClassListFile=/home/user/classes.lst -XX:SharedArchiveFile=/home/user/appcds.jsa --class-path /app/javadocky.jar && \
    rm /home/user/classes.lst

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Xshare:on -XX:SharedArchiveFile=/home/user/appcds.jsa -javaagent:/home/user/.javadocky/newrelic/newrelic.jar -jar /app/javadocky.jar" ]
