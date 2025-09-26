#!/bin/bash
# Script to directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'"

main() {

# Check if a directory and pattern are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
  echo "Usage: $0 <directory> <suffix> <pds>";
  echo "Example: $0 /u/usr1/sample  jcl  //'SMPL.JCL' [Y|N]";
  exit 1;
fi

FROM_DIR="$1";
FROM_SUF="$2";
TO_PDS="$3";
SYM="$4";

# Variables chosen by user
export GERS_DBUSER=SAFRBLD
export GERS_DBNAME=SADBNEIL
export GERS_DBSG=SASGNEIL
export GERS_DBSCH=SAFRNEIL
export GERS_DBSUB=DM13

echo "Preparing files from directory: $FROM_DIR with suffix: $FROM_SUF and copying to MVS dataset: $TO_PDS"

# Remove data preparation directory and create fresh
rm -Rf prep;
mkdir prep;

# Determine directory contents
# echo "$FROM_DIR"/*."$FROM_SUF"

ls "$FROM_DIR"/*."$FROM_SUF" > "$FROM_DIR"/list.tmp
FILE="$FROM_DIR/list.tmp"; # File to parse to get directory contents
if [ ! -f "$FILE" ]; then
  echo "Error: Temporary file '$FILE' not found.";
  exit 1;
fi

# Process each file that matches the pattern
while IFS= read -r line; do
  echo $line > "$FROM_DIR"/text.tmp;
  staidx=$(awk -F"/" '{print length($0) - length($NF)}' "$FROM_DIR"/"text.tmp" );
  endidx=$(awk -F"." '{print length($0) - length($NF)}' "$FROM_DIR"/"text.tmp" );
  # echo "Staidx: $staidx Endidx: $endidx Line: $line";

  if [ $staidx -gt 0 ] && [ $endidx -gt $staidx ]; then
    file="${line:$staidx}"
    echo "Preparing file: $file";
    if [ 1 -eq "$SYM" ]; then
      ./prepare_ddl.sh "$file";
    fi
  fi

done < "$FILE"

# Copy the processed files from preparation directory to MVS PDSE
cp -S d=."$FROM_SUF" "$FROM_DIR"/prep/*."$FROM_SUF" "$TO_PDS";

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"