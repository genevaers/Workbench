//LISTDATA JOB (ACCT),
//          'Identify empties',
//          NOTIFY=NBEESLE,
//          CLASS=A,REGION=0M,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//*
//*************************************************************
//*
//   EXPORT SYMLIST=*
//*
//   SET LOADHLQ=&$LOADHLQ.
//   SET LOADMLQ=&$LOADMLQ.
//*
//********************************************************************
//*   Identify empty .DATA datasets and if found change their DCB
//*   information to be valid for an empty file of type .DATA
//********************************************************************
//*
//EXAMINE  EXEC PGM=BPXBATCH,
//            COND=(4,LT)
//*
//SYSPRINT DD *
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//STDPARM  DD *,SYMBOLS=EXECSYS
sh ;
set -e;
set -o xtrace;
echo;
rm -rf ~/git/public/Workbench/database/db2/tmpinfo;
mkdir ~/git/public/Workbench/database/db2/tmpinfo;
cd ~/git/public/Workbench/database/db2/tmpinfo;
rm -rf datafiles.attr
tsocmd "PROFILE NOPREFIX" ;
tsocmd "LISTDS '&HLQ..&MLQ..CODETABL.DATA';"  > datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..CONTRLRC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..ENVIRON.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..GLOBAFLD.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..GROUPTS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LFPFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOGFILE.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOGIC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOGREC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOGRECIX.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOOKUP.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOOKUPSR.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LOOKUPST.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LRFIELD.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LRFLDATR.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LRFLDIDX.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..LRLFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..MIGRAUDT.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..NEXTKEY.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..PHYSFILE.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPEV.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPJN.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPLF.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPLR.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPPF.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPUE.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPUR.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPVF.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SECGRPVW.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..SERVER.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..USEREXIT.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..USERTS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VDPBATCH.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VDPBTCHV.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VFVASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VIEWCOL.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VIEWFOLD.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VIEWHDFT.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VIEWSRC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VIEWTS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VWCOLSRC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VWLOGDEP.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&HLQ..&MLQ..VWSORTKY.DATA';"  >> datafiles.attr;

./NullFiles.sh tmpinfo datafiles.attr
/*
//
