# https://spring.io/guides/gs/spring-boot-docker/

FROM openjdk:9-alpine
RUN addgroup user && adduser -D -G user -h /home/user -s /bin/bash user && mkdir /home/user/.javadocky && chown -R user:user /home/user/.javadocky
WORKDIR /home/user
USER user
VOLUME /home/user/.javadocky
ADD build/libs/javadocky-*.jar /home/user/javadocky.jar
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /home/user/javadocky.jar" ]
