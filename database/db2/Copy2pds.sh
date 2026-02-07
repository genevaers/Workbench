#!/bin/bash
# Script to directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'"

main() {

# Check if a directory and pattern are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
  echo "Usage: $0 <directory> <suffix> <pds> <substitutions required>";
  echo "Example: $0 /u/usr1/sample  jcl  //'SMPL.JCL' [0|1]";
  exit 1;
fi

FROM_DIR="$1";
FROM_SUF="$2";
TO_PDS="$3";
SYM="$4";

echo "Preparing files from directory: $FROM_DIR with suffix: $FROM_SUF and copying to MVS dataset: $TO_PDS"

# Remove data preparation directory and create fresh
rm -Rf "$FROM_DIR"/prep;
mkdir "$FROM_DIR"/prep;

# Remove temp directory and create fresh if JCL
if [ 2 -eq "$SYM" ]; then
  rm -Rf "temp";
  mkdir "temp";

fi

# Determine directory contents
# echo "$FROM_DIR"/*."$FROM_SUF"

ls "$FROM_DIR"/*."$FROM_SUF" > "$FROM_DIR"/prep/list.tmp
FILE="$FROM_DIR/prep/list.tmp"; # File to parse to get directory contents
if [ ! -f "$FILE" ]; then
  echo "Error: Temporary file '$FILE' not found.";
  exit 1;
fi

# Process each file that matches the pattern
while IFS= read -r line; do
  echo $line > "$FROM_DIR"/prep/text.tmp;
  staidx=$(awk -F"/" '{print length($0) - length($NF)}' "$FROM_DIR"/"prep/text.tmp" );
  endidx=$(awk -F"." '{print length($0) - length($NF)}' "$FROM_DIR"/"prep/text.tmp" );
  # echo "Staidx: $staidx Endidx: $endidx Line: $line";

  if [ $staidx -gt 0 ] && [ $endidx -gt $staidx ]; then
    file="${line:$staidx}";
    if [ 1 -eq "$SYM" ]; then
      echo "Performing DDL substitutions and copying file: $file";
      . ./prepare_ddl.sh "$file";
    else
      if [ 2 -eq "$SYM" ]; then
        echo "Performing JCL substitutions and copying file: $file";
        . ./prepare_jcl.sh "$file";
      else
        echo "Copying file: $file";
      fi
    fi
  else
    echo "Copying file: $file";
  fi
done < "$FILE"

# Copy the processed files from preparation directory to MVS PDSE
if [ 1 -eq "$SYM" ] || [ 2 -eq "$SYM" ]; then
  cp -S d=."$FROM_SUF" "$FROM_DIR"/prep/*."$FROM_SUF" "$TO_PDS";
else
  cp -S d=."$FROM_SUF" "$FROM_DIR"/*."$FROM_SUF" "$TO_PDS";
fi
}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"