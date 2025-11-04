# ==========================
# Etapa 1: Build de Maven
# ==========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia TODO el código del monorepo al contenedor
# Esto es necesario para que Maven pueda encontrar el módulo 'event-dtos'
COPY . .

# 1. Construye e instala 'event-dtos' en el repositorio local de Maven
RUN mvn -f ./event-dtos/pom.xml clean install

# 2. Ahora, construye el 'cart-service'
# Maven podrá encontrar la dependencia 'event-dtos' que acaba de instalar
RUN mvn -f ./cart-service/pom.xml clean package -DskipTests

# ==========================
# Etapa 2: Imagen final
# ==========================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia solo el JAR compilado de cart-service desde la etapa anterior
COPY --from=builder /app/cart-service/target/cart-service-*.jar app.jar

# Expón el puerto 9813 (definido en tu properties)
EXPOSE 9813

ENTRYPOINT ["java", "-jar", "app.jar"]