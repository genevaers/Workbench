#!/bin/bash
# Script to directory contents to MVS pds(e)

# Check if a directory and pattern are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo "Usage: $0 <directory> <suffix> <pds>"
  echo "Example: $0 /u/usr1/sample  jcl  //'SMPL.JCL'"
  exit 1
fi

FROM_DIR="$1";
FROM_SUF="$2";
TO_PDS="$3";

echo "Copying files from directory: $FROM_DIR with suffix: $FROM_SUF to MVS dataset: $TO_PDS"

# Determine directory contents

# echo "$FROM_DIR"/*."$FROM_SUF"

ls "$FROM_DIR"/*."$FROM_SUF" > "$FROM_DIR"/list.tmp

FILE="$FROM_DIR/list.tmp"; # File to parse to get directory contents
if [ ! -f "$FILE" ]; then
  echo "Error: Temporary file '$FILE' not found.";
  exit 1;
fi

while IFS= read -r line; do
  # Process each line (record) here
  # echo "Record: $line";
  # TO_PDS=""""$TO_PDS"""";
  # echo "TO_PDS: $TO_PDS";
  # Find last index of test we're searching for
  echo $line > "$FROM_DIR"/text.tmp;
  staidx=$(awk -F"/" '{print length($0) - length($NF)}' "$FROM_DIR"/"text.tmp" );
  endidx=$(awk -F"." '{print length($0) - length($NF)}' "$FROM_DIR"/"text.tmp" );
  # echo "Staidx: $staidx Endidx: $endidx";

  if [ $staidx -gt 0 ] && [ $endidx -gt $staidx ]; then
    file=$(expr substr "$line" 1 $((endidx-1)) );
    echo "Copying file: $file";
      cp -S d=."$FROM_SUF" "$file"."$FROM_SUF" "$TO_PDS";
    # cp -S d=."$FROM_SUF" "$FROM_DIR"/*."$FROM_SUF" "$TO_PDS";

  fi

done < "$FILE"
