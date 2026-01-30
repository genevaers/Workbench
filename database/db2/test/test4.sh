#!/bin/bash

FILE_LIST="jcllist";
DEST_FILE="out.txt";

# list of JCL files
ls *.JCL > "$FILE_LIST";

while IFS= read -r line; do
  echo "Processing file: $line";
  ./test3.sh "$line"; 
done < "$FILE_LIST"
