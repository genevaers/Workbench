#!/bin/bash

# targetfiles

HLQOLD="C02888.GENERS.D251114U.";
HLQNEW="GEBT.GENERS.D251114U.";

intypes[1]="LOGIC.DATA";
intypes[2]="VIEWCOL.DATA";
intypes[3]="VIEWSRC.DATA";
intypes[4]="VIEWTS.DATA";
intypes[5]="VWCOLSRC.DATA";

str1=$HLQOLD;
str2=$HLQNEW;

echo "HLQOLD: $str1";
echo "HLQNEW: $str2";

# make both strings equal length so don't need to bother with padding

len1=${#str1};
len2=${#str2};

diff=$((len1-len2));

if (( diff > 0)); then
  echo "bigger";
  for (( i=0; i<$diff; i++)); do
    str2+=" ";
  done
  len2=${#str2};
elif (( diff < 0)); then
  echo "smaller";
  for (( i=$diff; i<0; i++)); do
    str1+=" ";
  done
  len1=${#str1};
else
  echo "same";
fi

echo "Length 1: $len1, length 2: $len2, difference: $diff";

for intype in "${intypes[@]}"; do
  infile="//'$HLQNEW$intype'";
  outfile="//'$HLQNEW$(intype)2'";
  echo "File: $infile -- $outfile";
done

# perform substitutions
# convert back to EBCDIC
# copy back to MVS

# sed 's/C02888/GEBT/g' "//'NBEESLE.DATA'" > tmp.data;
# iconv -f ISO8859-1 -t IBM-1047 tmp.data > tmp2.data;
# cp tmp2.data "//'NBEESLE.DATA2'"
