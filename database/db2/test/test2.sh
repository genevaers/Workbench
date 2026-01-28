#!/bin/bash

SOURCE_FILE="file.txt";
DEST_FILE="out.txt";
lastline="";

# Clear the destination file if it exists, or create a new one
> "$DEST_FILE"

while IFS= read -r line; do
  # Example condition: Check if the line contains "process" or starts with "user:"
  # if [[ "$line" == *"DB2RLIB"* ]] || [[ "$line" == user:* ]]; then
  if [[ "$lastline" == *"RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -"* ]]; then
    echo "line detected";
    if [[ "$line" == *"LIB('&DB2RLIB.')"* ]]; then
      echo "DB2RLIB found";
      echo "RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN)" >> "$DEST_FILE";    
    else
      echo "RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) - " >> "$DEST_FILE";    
    fi
  else
    if [[ "$lastline" != "" && "$lastline" != *"LIB('&DB2RLIB.')"* ]]; then
      echo "$lastline" >> "$DEST_FILE";
    fi
  fi
  lastline=$line;
done < "$SOURCE_FILE"
echo "$lastline" >> "$DEST_FILE"
