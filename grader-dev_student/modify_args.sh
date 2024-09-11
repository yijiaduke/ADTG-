#!/bin/bash

# File path to the .gitlab-ci.yml file
YML_FILE=".gitlab-ci.yml"

# Check if two arguments are passed
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <arg1> <arg2>"
    exit 1
fi

# Extract the arguments
ARG1=$1
ARG2=$2

# Use sed to replace the args value in the .gitlab-ci.yml file
sed -i.bak -E "s/(--args=\")[^\"]*(\")/\\1${ARG1} ${ARG2}\\2/" $YML_FILE

echo "Updated the args to \"$ARG1 $ARG2\" in $YML_FILE"
