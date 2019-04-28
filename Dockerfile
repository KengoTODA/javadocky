# https://spring.io/guides/gs/spring-boot-docker/

FROM openjdk:8-alpine
ADD . /javadocky/
RUN cd /javadocky && ./gradlew assemble

FROM openjdk:8-jre-alpine
RUN addgroup user && adduser -D -G user -h /home/user -s /bin/bash user && mkdir /home/user/.javadocky && chown -R user:user /home/user/.javadocky
WORKDIR /home/user
USER user
VOLUME /home/user/.javadocky
COPY --from=0 /javadocky/build/libs/javadocky-*.jar /home/user/javadocky.jar
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /home/user/javadocky.jar" ]
