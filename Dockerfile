# Étape de construction
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

# Copie des fichiers nécessaires pour télécharger les dépendances en cache
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Téléchargement des dépendances sans exécuter le code
RUN ./mvnw dependency:go-offline -B

# Copie du reste de l'application
COPY src ./src

# Construction de l'application
RUN ./mvnw clean package -DskipTests

# Étape d'exécution
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copie du fichier JAR généré à partir de l'étape de construction
COPY --from=builder /app/target/*.jar app.jar

# Exposition du port 8091, comme spécifié dans application.properties
EXPOSE 8091

# Commande d'exécution de l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
