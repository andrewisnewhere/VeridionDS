#FROM openjdk:17-jdk
#
## Create a directory for your application
#WORKDIR /app
#
## Copy the application jar file into the image
#COPY target/VeridionDS-3.1.2.jar /app/VeridionDS.jar
#
## Copy the application.properties file into the image
#COPY src/main/resources/application.properties /app/application.properties
#
## Expose the port your application listens on
#EXPOSE 8080
#
## Command to run your application
#CMD ["java", "-jar", "VeridionDS.jar"]

################### FOR DEBUGGING PURPOSES
FROM openjdk:17-jdk

# Create a directory for your application
WORKDIR /app

# Copy the application jar file into the image
COPY target/VeridionDS-3.1.2.jar /app/VeridionDS.jar

# Copy the application.properties file into the image
COPY src/main/resources/application.properties /app/application.properties

# Expose the port your application listens on
EXPOSE 8080

# Expose the debug port
EXPOSE 5005

# Set JAVA_TOOL_OPTIONS environment variable for debugging
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"

# Command to run your application
CMD ["java", "-jar", "VeridionDS.jar"]
