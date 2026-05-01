#!/bin/bash

# Script to copy directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh DDL DDL "//'GEBT.RTC23321.DDL'" 1
# Invoked like: ./Copy2pds.sh JCL JCL "//'GEBT.RTC23321.JCL'" 2
# Invoked like: ./Copy2pds.sh StorProc SQL "//'GEBT.RTC23321.SQL'" 0


main() {

# Check if a directory and pattern are provided
if [ -z "$1" ] || [ -z "$2" ] ; then
  echo "Usage: $0 <directory> <file>";
  echo "Example: $0 /u/usr1/git/Workbench/database/db2/tmpinfo/datafiles.attr";
  exit 1;
fi

FROM_DIR="$1";
FROM_FILE="$2";
endidx=0;
lastseq=0;

echo "$(date) ${BASH_SOURCE##*/} Examining for null .DATA files from TSO RECEIVE using: $FROM_DIR/$FROM_FILE";
pwd ;

FILE=../"$FROM_DIR"/"$FROM_FILE"; # File to parse to get directory contents
if [ ! -f "$FILE" ]; then
  echo "$(date) ${BASH_SOURCE##*/} Error: Temporary file '$FILE' not found.";
  exit 1;
fi

flag=0;
dotdata=".DATA";
lastseq=999;

# Process each file that matches the pattern
while IFS= read -r line; do
  echo "LINE: $line Lastseq: $lastseq";
  if [[ "$lastseq" == 0 ]]; then
    # this should be next after file name
    if [[ "$line" == *"--RECFM-LRECL-BLKSIZE-DSORG"* ]]; then
      lastseq=$((lastseq + 1));
    else
      echo "$(date) ${BASH_SOURCE##*/} *** Expected --RECFM-LRECL-BLKSIZE-DSORG not found: terminating";
      exit 2; 
    fi
  else
    if [[ "$lastseq" == 1 ]]; then
      # this could be next after --RECFM-LRECL-BLKSIZE-DSORG if file was empty
      if [[ "$line" == *"  **    **    **      PS"* ]]; then
        echo "Empty file found: $file";
        # other stuff related to finding an empty file
        # such as erasing the file and reallocating it as empty file with correct attributes
      fi
      lastseq=$((lastseq + 1));
    else
      # all other cases
      if [[ "$line" == *"$dotdata" ]]; then
        flag=1;
        lastseq=0;
        echo "The string ends with $dotdata. means start of sequence";
        file="${line:0}";
        echo "DATA File: $file";
      else
        lastseq=$((lastseq+1));
      fi
    fi
  fi
  echo "Next record";
done < "$FILE"

if [[ "$flag" -ne 1 ]]; then
  echo "$(date) ${BASH_SOURCE##*/} *** No $suffix files found in dataset attribute list stored in $FILE. See error log $err_log";
  cat $FILE > $err_log;
fi
}

exitIfError() {

if [ $? != 0 ]; then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error log $err_log";
    exit 1;
fi

}

main "$@"