//NULLDATA JOB (ACCT),
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
//*************************************************************
//*   Delete any temporary copies that might remain
//*************************************************************
//*
//DELFILE  EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *,SYMBOLS=EXECSYS
 DELETE  &LOADHLQ..&LOADMLQ..CODETABL.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..CONTRLRC.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..ENVIRON.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..GLOBAFLD.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..GROUPTS.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LFPFASS.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOGFILE.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOGIC.DATA.X    PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOGREC.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOGRECIX.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOOKUP.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOOKUPSR.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LOOKUPST.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LRFIELD.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LRFLDATR.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LRFLDIDX.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..LRLFASS.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..MIGRAUDT.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..NEXTKEY.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..PHYSFILE.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPEV.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPJN.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPLF.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPLR.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPPF.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPUE.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPUR.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPVF.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SECGRPVW.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..SERVER.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..USEREXIT.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..USERTS.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VDPBATCH.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VDPBTCHV.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VFVASS.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VIEWCOL.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VIEWFOLD.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VIEWHDFT.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VIEWSRC.DATA.X  PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VIEWTS.DATA.X   PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VWCOLSRC.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VWLOGDEP.DATA.X PURGE
 DELETE  &LOADHLQ..&LOADMLQ..VWSORTKY.DATA.X PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
/*
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
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..CODETABL.DATA';" > datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..CONTRLRC.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..ENVIRON.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..GLOBAFLD.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..GROUPTS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LFPFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOGFILE.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOGIC.DATA';"    >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOGREC.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOGRECIX.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOOKUP.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOOKUPSR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LOOKUPST.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LRFIELD.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LRFLDATR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LRFLDIDX.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..LRLFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..MIGRAUDT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..NEXTKEY.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..PHYSFILE.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPEV.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPJN.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPLF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPLR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPPF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPUE.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPUR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPVF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SECGRPVW.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..SERVER.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..USEREXIT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..USERTS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VDPBATCH.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VDPBTCHV.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VFVASS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VIEWCOL.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VIEWFOLD.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VIEWHDFT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VIEWSRC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VIEWTS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VWCOLSRC.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VWLOGDEP.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&LOADMLQ..VWSORTKY.DATA';" >> datafiles.attr;

cd ../SH ;
pwd ;
./NullFiles.sh tmpinfo datafiles.attr ;
/*
//
