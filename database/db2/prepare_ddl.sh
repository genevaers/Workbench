#!/bin/sh

sed 's/&$DBUSER./SAFRBLD/g' GVBQDRAL.DDL  > tmp1
sed 's/&$DBNAME./SADBNEIL/g'         tmp1 > tmp2
sed 's/&$DBSG./SASGNEIL/g'           tmp2 > tmp3
sed 's/&$DBSCH./SAFRNEIL/g'          tmp3 > gvbqdral.ddl

# sed 's/&$DBUSER./SAFRBLD/g' GVBQDRAL.DDL > gvbqdral.ddl