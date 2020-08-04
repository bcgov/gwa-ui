FROM maven:3.6.3-openjdk-8 as BUILD
COPY src gwa-ui/src
COPY angular gwa-ui/angular
COPY pom.xml gwa-ui/pom.xml

WORKDIR gwa-ui
RUN mvn install

FROM alpine
COPY --from=BUILD /gwa-ui/target/*.war /app.war
