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

echo "$(date) ${BASH_SOURCE##*/} Examining for null .DATA files from TSO RECEIVE using: $FROM_DIR/$FROM_FILE"
pwd ;

FILE=../"$FROM_DIR"/"$FROM_FILE"; # File to parse to get directory contents
if [ ! -f "$FILE" ]; then
  echo "$(date) ${BASH_SOURCE##*/} Error: Temporary file '$FILE' not found.";
  exit 1;
fi

# Process each file that matches the pattern
while IFS= read -r line; do
  # staidx=$(awk -F"/" '{print length($0) - length($NF)}' ../"$FROM_DIR"/"$FROM_FILE" );
  endidx=$(awk -F"." '{print length($0) - length($NF)}' ../"$FROM_DIR"/"$FROM_FILE" );
  suffix=${line:$endidx:5};
#  echo "Endidx: $endidx END";
  echo "Line: $line";
  echo "Suffix: $suffix";

  if [ $endidx -gt 0 ]; then
    suffix=${line:$endidx:5};
#    echo "Suffix: $suffix";
    file="${line:$staidx}";
    if [ $suffix -eq ".DATA" ]; then
      file="${line:$staidx}";
#      echo "DATA File: $file";
      exit 1;
    fi
    echo "Good line encountered";
  else
    echo "$(date) ${BASH_SOURCE##*/} error encountered in record: $line";
    exit 2;
  fi
  echo "Next record";
done < "$FILE"

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error log $err_log";
    exit 1;
fi

}

main "$@"