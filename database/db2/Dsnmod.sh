#!/bin/bash

# targetfiles

HLQOLD="C02888.GENERS.D251114U.";
HLQNEW="GEBT.GENERS.D251114U.";

intype[1]="LOGIC.DATA";
intype[2]="VIEWCOL.DATA";
intype[3]="VIEWSRC.DATA";
intype[4]="VIEWTS.DATA";
intype[5]="VWCOLSRC.DATA";

str1="TEXT";
str2="NEW TEXT";

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

# infile="$HLQOLD$intype[$i]" 

for (( i=1; i<6; i++)); do
  infile=$(intype[i]);
  echo "Infile: $infile";
done

# perform substitutions
# convert back to EBCDIC
# copy back to MVS

# sed 's/C02888/GEBT/g' "//'NBEESLE.DATA'" > tmp.data;
# iconv -f ISO8859-1 -t IBM-1047 tmp.data > tmp2.data;
# cp tmp2.data "//'NBEESLE.DATA2'"
