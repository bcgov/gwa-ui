FROM maven:3.6.3-openjdk-8 as BUILD

COPY . /app

WORKDIR /app
RUN mvn install

FROM alpine
COPY --from=BUILD /app/target/*.war /app.war

