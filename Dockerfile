# --- ETAPA 1: Construcción ---
FROM maven:3.9.6-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copiar solo el pom.xml primero para aprovechar el caché de Docker con las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y construir el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Crear un usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Optimización de la JVM para contenedores
ENV JAVA_OPTS="-XX:+UseParallelGC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Render usa la variable $PORT, por eso usamos ${PORT:-8080} para que funcione en local y en la nube
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
