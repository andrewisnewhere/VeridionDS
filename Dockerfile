#WITH DEBUGGING OPTION
FROM openjdk:17-jdk

WORKDIR /app

COPY target/VeridionDS-3.1.2.jar /app/VeridionDS.jar

COPY src/main/resources/application.properties /app/application.properties

EXPOSE 8080

EXPOSE 5005

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"

CMD ["java", "-jar", "VeridionDS.jar"]
