FROM maven:3.6.3-openjdk-8 as BUILD
COPY gwa-ui gwa-ui

WORKDIR gwa-ui
RUN mvn install

FROM alpine
COPY --from=BUILD /gwa-ui/target/*.war /app.war
