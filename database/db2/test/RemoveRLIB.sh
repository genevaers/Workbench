#!/bin/bash

main() {

FILE_LIST="jcllist";
DEST_FILE="out.txt";

# list of JCL files
ls *.JCL > "$FILE_LIST";

while IFS= read -r line; do
  echo "Processing file: $line";
  ./editRLIB.sh "$line"; 
done < "$FILE_LIST"

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"