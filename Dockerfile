FROM eclipse-temurin:17-jdk

WORKDIR /app

# ✅ bash install
RUN apt-get update && apt-get install -y bash

COPY . .

# ✅ mvnw ko proper run karne ke liye
RUN chmod +x mvnw

# ✅ build
RUN ./mvnw clean package -DskipTests

# ✅ run (IMPORTANT FIX)
CMD ["java", "-jar", "target/tablecure-0.0.1-SNAPSHOT.jar"]