# ==========================
# Etapa 1: Build de Maven
# ==========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia TODO el código del monorepo
COPY . .

# 1. Construye e instala 'event-dtos' primero
RUN mvn -f ./event-dtos/pom.xml clean install

# 2. Ahora, construye el 'order-service'
RUN mvn -f ./order-service/pom.xml clean package -DskipTests

# ==========================
# Etapa 2: Imagen final
# ==========================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia solo el JAR compilado de order-service
COPY --from=builder /app/order-service/target/order-service-*.jar app.jar

EXPOSE 9812

ENTRYPOINT ["java", "-jar", "app.jar"]