FROM openjdk:8-jre-alpine
RUN mkdir -p /sidekick-broker
ARG JAR_FILE
ADD ${JAR_FILE} sidekick-broker/
WORKDIR sidekick-broker/
EXPOSE 7777 8080
ENTRYPOINT java -jar sidekick-broker-app.jar
