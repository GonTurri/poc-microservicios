# order-service/Dockerfile (compatible con Dokploy sin BuildKit)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# copiar solo lo necesario (context = raíz del repo)
COPY event-dtos ./event-dtos
COPY order-service ./order-service
# Si necesitás otros módulos, copyá también.

# construir e instalar la librería compartida
RUN mvn -B -f event-dtos/pom.xml clean install -DskipTests

# build del order-service
RUN mvn -B -f order-service/pom.xml clean package -DskipTests

# debug temporal (muestra artefactos para ver que existe el jar)
RUN ls -la /build/order-service/target || true
RUN ls -la /root/.m2/repository/org/utn/ba/event-dtos/1.0-SNAPSHOT || true

# Imagen runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# copia el jar (usa patrón SNAPHOT). Ajustá si tu jar tiene otro nombre.
COPY --from=builder /build/order-service/target/*-SNAPSHOT.jar app.jar

EXPOSE 9812
ENTRYPOINT ["java","-jar","/app/app.jar"]
