FROM maven:3.6-jdk-11-openj9 as BUILD

COPY . /app

WORKDIR /app
RUN mvn install

FROM alpine
COPY --from=BUILD /app/target/*.war /app.war

