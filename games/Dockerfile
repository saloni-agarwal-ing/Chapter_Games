# Dockerfile for Drop Game WebSocket Server
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY games/core/build/libs/drop-server-1.0.0-all.jar /app/drop-server.jar
EXPOSE 8887
CMD ["java", "-cp", "drop-server.jar", "io.github.Games.Drop.WebGameServer"]
