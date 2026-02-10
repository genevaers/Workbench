#!/bin/bash
# context sensitive removal of DB2RLIB from JCL member.

main() {

# Check if file name is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <name of file to edit>";
  exit 1;
fi

SOURCE_FILE="$1";
DEST_FILE="$SOURCE_FILE.tmp";

# echo "Input file : $SOURCE_FILE";
# echo "Output file: $DEST_FILE";

lastline="";

# Clear the destination file if it exists, or create a new one
> "$DEST_FILE"

while IFS= read -r line; do
  if [[ "$lastline" == *"RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -"* ]]; then
    # echo "line detected";
    if [[ "$line" == *"LIB('&DB2RLIB.')"* ]]; then
      # echo "DB2RLIB found";
      echo "RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN)" >> "$DEST_FILE";    
    else
      echo "RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) - " >> "$DEST_FILE";    
    fi
  else
    if [[ "$lastline" != "" && "$lastline" != *"DB2RLIB"* ]]; then
      len=${#lastline};
#      if [[ $len > 32 ]]; then
        pre=${lastline:23:4};
        dsn=${lastline:27:8};
        echo "prefix is: $pre , dsn: $dsn , length: $len";
#        pre=$(expr substr "$lastline" 24 4);
#        dsn=$(expr substr "$lastline" 28 4);
        if [[ "$pre" == "DSN=" && $len < 28 ]]; then
          echo "prefix is dsn : $dsn : length $len";
#          if [[ -z "$dsn" ]]; then
#           echo "empty dataset";
#          fi
        fi
#      fi
      echo "$lastline" >> "$DEST_FILE";
    fi
  fi
  lastline=$line;
done < "$SOURCE_FILE"
echo "$lastline" >> "$DEST_FILE"

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"