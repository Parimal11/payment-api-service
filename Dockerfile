FROM eclipse-temurin:17
COPY build/libs/payment-api-service-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]