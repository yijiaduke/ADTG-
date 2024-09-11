# Use an official Ubuntu 22.04 as a base image
FROM ubuntu:22.04

# Set environment variables to avoid interactive prompts during installations
ENV DEBIAN_FRONTEND=noninteractive

# Install necessary packages: Java and Gradle dependencies
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk wget unzip python3-pip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.8-bin.zip -P /tmp && \
    unzip /tmp/gradle-8.8-bin.zip -d /opt && \
    ln -s /opt/gradle-8.8/bin/gradle /usr/bin/gradle

# Verify installation
RUN gradle --version && java --version

RUN pip3 install pytest


# Set the working directory
WORKDIR /graderApp

# Copy the current directory contents into the container at /app
COPY . /graderApp
COPY settings.gradle settings.gradle  
# Copy settings.gradle 

# # Set up Docker volumes
# VOLUME /graderApp/volume/data
# VOLUME /graderApp/volume/resources


# Clean and build the project
RUN gradle clean build

# Set the default command to run the application
CMD ["gradle", "run"]
