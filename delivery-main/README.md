# delivery

## Overview

DELIVERY wont check whether the student has passed the pre-requisite, it will only check the start-date. 

### Delivery Statuses on the DB

1. You will create the repository, send inivitation to students and send the initial assessments to the repository if the status in delivery table is 'INIT'. After creating the repository successfully, the status will turn into 'COMPLETED';

2. You will send the sepecific assessment to the student if the status in delivery table is 'DELIVER';
After sending each assessment successfully to the repository, the status will turn into 'COMPLETED';

Each delivery of assessment will add a new record to adtg.assn_deadline;

### Failure cases testing including:

1. no enrollment of the student under the status of INIT

2. the start date does not come

3. no repository for the student under the status of DELIVER

4. when the program meets any technical errors during delivering, like connection and API broken, the status will turn to 'ERROR' and the error will print to the screen.

These cases above will give ERROR Status in adtg.delivery table, please manually change the status if you want to redo the process.

### Cases that will directly change the status to COMPLETED without sending new assessment:

1. already sent the assessment to student under the status of DELIVER

2. Repository has already created under the status of INIT

## Warning 

1. please make sure DB table users, course, section, enrollmemt, category, assessment, delivery contains the data you need
2. As for 'gitlab_group' col in the section table, please only store the title after '[duke.edu](https://gitlab.oit.duke.edu/)' in gitlab. 
For example, if the url is 'https://gitlab.oit.duke.edu/jpastorino/adtg-test', please only store 'jpastorino/adtg-test'. 



## Installation
1. install gradle

2. JDK over 17

3. POSTGRE DB in your machine

## Set up in vcm-41845.vm.duke.edu Server 
1. Log in to the ${netid}@vcm-41845.vm.duke.edu, replace netid with yours

2. make sure /srv/delivery is empty, if not, clean up the folder

3. git clone the delivery package 
```
cd /srv/delivery
git clone https://gitlab.oit.duke.edu/jpastorino/adtg/delivery.git
```
type the username and corresponding password in gitlab step by step

4. (Optional) if other branches are not deleted, 
```
cd delivery
git switch re-construct
```
If the main branch has already been merged, skip the step.

5. git clone the core package
```
git clone https://gitlab.oit.duke.edu/jpastorino/adtg/core.git
cd core
git switch mergeBranch
```

6. change the JDK from 17 to 21
```
cd /srv/delivery/delivery
#or
cd /srv/delivery/
```
*depends on location of git, this should be the main git root*

Open the file 'build.gradle'

replace Line 13 from 'languageVersion = JavaLanguageVersion.of(17)' to 'languageVersion = JavaLanguageVersion.of(21)'

```
cd /srv/delivery/delivery/core
#or
cd /srv/delivery/core
```
Open the file 'build.gradle'

replace Line 12 from 'languageVersion = JavaLanguageVersion.of(17)' to 'languageVersion = JavaLanguageVersion.of(21)'


7. Add database password
```
cd /srv/delivery/delivery/core
```
Open the file 'db.properties' under relavtive path 'src/main/resources/db.properties'

replace the password after 'db.password' to yours. For exampe, if the database password is 'abcdefg', the configuration should be 'db.password = acbdefg'



### Setting CRON
- the `delivery.sh` will run the delivery program, and should be scheduled on cron to run every desired interval (usually 5min).
- make sure all files belong to `adtg`
    - run `chown -R adtg:adtg /srv/delivery`
- make sure the `delivery.sh` owner is `root:adtg` and only the `adtg` group has execution permission `rw-r-xr--`
    - run `chmod 654 delivery.sh`
    - run `chown root:adtg delivery.sh`


### Build and Run 
- Build and run the project. 
- Make sure you have already inserted your command(INIT/DELIVER) to database table adtg.delivery before running.

#### Option 1(Recommendation)
This option wont automatically build the project 
```
gradle run
```

#### Option 2
```
./gralew build
./gradlew run
```

---
---
---
---

## Set Up in your own machine
1. connect to a Duke domain network (VPN or Duke VM)

2. git clone the project delivery. As for the core folder, git clone the project core
by executing

#### Option 1
```
git clone git@gitlab.oit.duke.edu:jpastorino/adtg/delivery.git
cd delivery
git clone git@gitlab.oit.duke.edu:jpastorino/adtg/core.git
```

#### Option 2
```
git clone https://gitlab.oit.duke.edu/jpastorino/adtg/delivery.git
cd delivery
git clone git@gitlab.oit.duke.edu:jpastorino/adtg/core.git
```

3. (Optional) if other branches are not deleted, 
```
cd ${path}/delivery
git switch re-construct
```
If the main branch has already been merged, skip the step.

4. 
```
cd ${path}/delivery/core
git switch mergeBranch
```


5. Add database password
```
cd /srv/delivery/delivery/core
```
Open the file 'db.properties' under relavtive path 'src/main/resources/db.properties'

replace the password after 'db.password' to yours. For exampe, if the database password is 'abcdefg', the configuration should be 'db.password = acbdefg'



6. Build and run the project. Make sure you have already inserted your command(INIT/DELIVER) to database table adtg.delivery before running.

#### Option 1(Recommendation)
This option wont automatically build the project 
```
gradle run
```

#### Option 2
```
./gralew build
./gradlew run
```
