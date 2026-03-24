FROM eclipse-temurin:17-jdk

WORKDIR /app

# 👇 ADD THIS LINE
RUN apt-get update && apt-get install -y bash

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/*.jar"]