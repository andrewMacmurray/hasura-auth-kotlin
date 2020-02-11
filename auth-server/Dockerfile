FROM adoptopenjdk:12-jre-hotspot

COPY ./build/libs/auth-server-0.0.1-SNAPSHOT.jar server.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "server.jar"]
