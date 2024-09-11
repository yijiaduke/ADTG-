# ADTG Grader

## Overview
This grader repository is created to run on the server. Files from test cases repo can be pulled and executed along with students' code. The result will be reported to the database.
## Components

## Set up

(This program works on Duke Virtual Machine System with the following configuration:
- Ubuntu 22.04
- Java 17.0.11
- Gradle Version 8.1+
- Docker) 
## Intruction


- Clone this project from gitlab, change the branch to `dev` for production
- Run this program:
```sh
gradle run
```
- Or (if you use a Gradle wrapper):
```sh
./gradlew run
```

## Create Docker and Docker Volume
- Create docker volume:
```sh
docker volume create graderVolume
```
- Save database configuration at the volume under the folder `resources`.
- Build the image:
```sh
docker build -t grader-app .
```
- Mount the image to the docker volume 
```sh
docker run -d   --name grader-app   -v graderVolume:/graderApp/volume   grader-app:latest
```
- Run the program in bash:
```sh
docker run -it --rm -v graderVolume:/graderApp/volume grader-app bash
```
- Run the program directly (gradle run)
```sh
docker run -v graderVolume:/graderApp/volume grader-app 
```


## Requirements and how the program works:
- When the program is running, it will check for any grading request from the database (with "PENDING" status).
- If there is any grading task (in `PENDING` status), the status will be changed to "IN PROGRESS", grader will be executed by cloning the test files from the course's gitlab repo and pulling the necessary files from the requesting student's repo.
- The program will execute the script from the file (e.g. `test.sh`) in the test repo. This file is defined in the `adtg.assessment` database on `test_cmd` column.
- The test repo must contain a `requiredFiles.txt` file, which lists all files to be pulled from students' repo.
- Grade is determined by executing the command file, and final grades will be calculated with penalty applied, which defined by `adtg.category` database table as a String form (e.g. `$@grade * (@hour / 100))$`)
- The term (e.g. `f24`) which defined by starting date of a section will determine the student's repo to pull files from (e.g. `f24_ece590_netId`)
- After the having the grade and output log, it will be updated to the database and status of the grade request will be changed to "DONE"
## What's next?
