#!/bin/sh

member = "GVBQDRAL.DDL";
sed 's/&$DBUSER./SAFRBLD/g' $member > tmp1
sed 's/&$DBNAME./SADBNEIL/g'   tmp1 > tmp2
sed 's/&$DBSG./SASGNEIL/g'     tmp2 > tmp3
sed 's/&$DBSCH./SAFRNEIL/g'    tmp3 > prep/$member

# sed 's/&$DBUSER./SAFRBLD/g' GVBQDRAL.DDL > gvbqdral.ddl