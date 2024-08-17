
FROM maven:3.5-jdk-8-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY target .
# Build a release artifact.
#RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/partner-matching-program-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod-docker"]
#CMD ["/bin/bash"]