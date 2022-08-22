FROM openjdk:8-jre-alpine
RUN mkdir -p /sidekick-api
ARG JAR_FILE
ADD ${JAR_FILE} sidekick-api/
WORKDIR sidekick-api/
EXPOSE 8084
ENTRYPOINT java -jar sidekick-api.jar
