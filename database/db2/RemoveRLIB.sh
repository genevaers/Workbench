#!/bin/bash
# remove DB2RLIB from JCL in context sensistive manner

main() {

FILE_LIST="prep/jcllist";

# Clear the destination file if it exists, or create a new one
> "$FILE_LIST"

# list of JCL files
ls *.JCL > "$FILE_LIST";

while IFS= read -r line; do
  echo "Processing file: $line";
  ./editRLIB.sh "$line";
    exitIfError;
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