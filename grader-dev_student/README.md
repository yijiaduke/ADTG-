# ADTG Grader for Student (deprecated: this version uses CI/CD directly on student's gitlab repo)

which does not pull files from student's repo via GitAPI but instead using the files in the repo directly. It needs GitlabToken of the testcase repo which contains the following privileges:
- read_repository
- read_api
and currently need username and password to access to database.
## Overview
This grader repository is created to build a Docker Image that runs auto grader in students' repo. Files from test cases repo can be pulled and executed along with students' code. The result will be reported to the database as well as pushed to the student's repo via API.

## Components
### Docker image:
The Docker image is built from the following Dockerfile, which will be built and pushed to each student's registry, then will be executed by Gitlab CI/CD using the `yml` file below.


```Dockerfile
# Use an official Ubuntu 22.04 as a base image
FROM ubuntu:22.04

# Set environment variables to avoid interactive prompts during installations
ENV DEBIAN_FRONTEND=noninteractive

# Install necessary packages: Java and Gradle dependencies
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk wget unzip && \
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



# Set the working directory
WORKDIR /graderApp

# Copy the current directory contents into the container at /app
COPY . /graderApp
COPY settings.gradle settings.gradle  
# Copy settings.gradle 

# Clean and build the project
RUN gradle clean build
```

### GitlabCI/CD in students' repo: 
```yml
stages:

  - run

# # Run the Java program
run_java_program:
  stage: run
  image: gitlab-registry.oit.duke.edu/jpastorino/adtg-test/repo_yz853:latest
  services:
    - docker:dind
  variables:
    DOCKER_HOST: "tcp://docker:2375"
    DOCKER_TLS_CERTDIR: ""
  script:
    - gradle --version
    - cd /graderApp
    - gradle runJava --args="jpastorino/adtg-test/testcase $CI_PROJECT_URL"  # Execute the gradle task with the arguments, the first argument is the testcase repo, the second is the student's repo url where netid will be retrieved to update the database
```
**Replace repo_yz853 in image the corresponding student netid**

## Set up

This program works on Duke Virtual Machine System with the following configuration:
- Ubuntu 22.04
- Java 17.0.11
- Gradle 8.8
- Docker

**Use Gradle wrapper if your gradle version is incompatible:**
```sh
./gradlew wrapper -gradle-version=8.8
```

## Instruction
<!-- - Change arguments for CI/CD file:
```sh
    make ARG1="testcase" ARG2="student1"
``` -->

- Execute the program:
```sh
    gradle runJava --args="arg1 arg2"
```
where ARG1 is the repo containing the testcases, ARG2 is the repo of the student to be graded. 
Example:
```sh
    gradle runJava --args="jpastorino/adtg-test/testcase jpastorino/adtg-test/repo_yz853"
```
where arg1 is the repo that stores test cases, arg2 is the repo of the student.
- Build docker image:
Run this command in the folder that contains Dockerfile
```sh
    sudo docker build -t grader-image .
```

- Run Docker, execute Gradle on Docker in CI/CD:
```sh
    sudo docker run --rm grader-image gradle runJava --args="jpastorino/adtg-test/testcase $CI_PROJECT_URL"
```
**replace $CI_PROJECT_URL to the student's repo url if you run the program from your local machine (not on Gitlab CI/CD)** 

- Push the Docker Image to student's GitLab registry:
Must run the build command in the folder that contains Dockerfile
```sh
    sudo docker login gitlab-registry.oit.duke.edu

    sudo docker build -t gitlab-registry.oit.duke.edu/jpastorino/adtg-test/repo_yz853 .

    sudo docker push gitlab-registry.oit.duke.edu/jpastorino/adtg-test/repo_yz853
```
**Replace yz_853 with the student's netid**

## Requirement to run Grader on student's repo:
- The student's repo must contain the `.gitlab-ci.yml` file mentioned above with the correct student's netid to access the Gitlab registry.
- The Docker Image must be pushed to the Gitlab Registry.
- Gitlab token is located in the Dockerfile at `app/config.properties`, ensure that this key have the less privileges (read_repository and read_api for the testcase repo).
- Confidential about the database is located in `core` folder in the Docker image.
- This repo contains student's assessments with folders' names are the `assn` in the database, inside the folders are the code files to be executed by `Grader`.
