# https://spring.io/guides/gs/spring-boot-docker/

FROM openjdk:8-alpine
VOLUME /tmp
ADD build/libs/javadocky-*.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-Dio.netty.buffer.bytebuf.checkAccessible=false"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
