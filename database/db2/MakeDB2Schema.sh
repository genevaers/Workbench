#!/bin/bash
# Script to tailor JCL, DDL and SQL statements needed to define DB2 schema and copy to MVS pds(e)'s
# This script is intended to be run after cloning the Workbench repository to USS file system

main() {

echo "Preparing DB2 metadata artifacts for Workbench and exporting these to MVS PDS/E datasets";

# Re-read DB2Schema.profile in case anything changed
source .DB2Schema.profile ;
exitIfError;

# prepare JCL jobs to define DB2 Schema

if [[ -z "$GERS_FROM_PDS_HLQ" ]]; then
  echo "Preparing JCL to define the GenedvaERS schema only";
  GERS_TO_PDS=$GERS_TO_PDS_HLQ'.'$GERS_TO_PDS_MLQ;
  echo "GERS_TO_PDS stem: $GERS_TO_PDS";
else
  echo "Preparing JCL to define the GenedvaERS schema and import data from DB2 export files";
  GERS_TO_PDS=$GERS_TO_PDS_HLQ'.'$GERS_TO_PDS_MLQ;
  echo "GERS_TO_PDS stem: $GERS_TO_PDS";
  GERS_FROM_PDS=$GERS_FROM_PDS_HLQ'.'$GERS_FROM_PDS_MLQ;
  echo "GERS_FROM_PDS stem: $GERS_FROM_PDS";
fi

# determine if user wants to explicitly mentiom DB2 RUNLIB.LOAD in JCL, etc.

if [[ -z "$GERS_DB2_RUN_LIB" ]]; then
  echo "All references to DB2 RUN library in JCL will be suppressed";
  export GERS_INCLUDE_DB2_RUNLIB=N;
fi

# determine name of MVS dataset to contain DDL's

TO_PDSDDL="//'$GERS_TO_PDS.DDL'";
TO_PDSDDL="$TO_PDSDDL";

# determine name of MVS dataset to contain JCL's

TO_PDSJCL="//'$GERS_TO_PDS.JCL'";
TO_PDSJCL="$TO_PDSJCL";

# determine name of MVS dataset to contain SQL, i.e stored procedures

TO_PDSSQL="//'$GERS_TO_PDS.SQL'";
TO_PDSSQL="$TO_PDSSQL";

# echo "Obtaining DB2 metadata artifacts for Workbench and exporting to: $TO_PDSDDL  $TO_PDSJCL  $TO_PDSSQL"

# process DDL's
./Copy2pds.sh . DDL $TO_PDSDDL 1;

# process JCL's
./Copy2pds.sh . JCL $TO_PDSJCL 2;

# process SQL
./Copy2pds.sh StorProc SQL $TO_PDSSQL 0;

echo "DB2 metadata artifacts for Workbench prepared successfully and exported to: $TO_PDSDDL  $TO_PDSJCL  $TO_PDSSQL";

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"