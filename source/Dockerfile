FROM openjdk:21-jdk-slim

WORKDIR /app

COPY did-verifier-server/gradlew .
COPY did-verifier-server/gradle gradle
COPY did-verifier-server/build.gradle .
COPY did-verifier-server/settings.gradle .
COPY did-verifier-server/libs libs
COPY did-verifier-admin /did-verifier-admin
COPY did-verifier-server/src src

RUN chmod +x ./gradlew

ENV GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
RUN ./gradlew bootJar --no-daemon --max-workers=1

CMD ["java", "-jar", "/app/build/libs/did-verifier-server-2.0.0.jar"]