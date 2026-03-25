FROM eclipse-temurin:17
WORKDIR /app
COPY . .
RUN javac StudentApp.java
CMD ["java","StudentApp"]