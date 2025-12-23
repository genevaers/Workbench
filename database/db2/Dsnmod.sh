#!/bin/bash

str1="TEXT";
str2="NEW TEXT";

# make both strings equal length so don't need to bother with padding

len1=${#str1};
len2=${#str2};

diff=$((len1-len2));

if (( diff > 0)); then
  echo "bigger";
  for (( i=0; i<$diff; i++)); do
    echo "!";
    str2+=" ";
  done
  len2=${#str2};
elif (( diff < 0)); then
  echo "smaller";
  for (( i=$diff; i<0; i++)); do
    echo "!";
    str1+=" ";
  done
  len1=${#str1};
else
  echo "same";
fi

echo "Length 1: $len1, length 2: $len2, difference: $diff";

# perform substitutions
# convert back to EBCDIC
# copy back to MVS

# sed 's/C02888/GEBT/g' "//'NBEESLE.DATA'" > tmp.data;
# iconv -f ISO8859-1 -t IBM-1047 tmp.data > tmp2.data;
# cp tmp2.data "//'NBEESLE.DATA2'"
