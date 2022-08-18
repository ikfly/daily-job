FROM openjdk:8-jre-alpine
VOLUME /tmp
ADD *.jar app.jar
RUN echo "Asia/Shanghai" > /etc/timezone
EXPOSE 8888
ENTRYPOINT java -jar /app.jar