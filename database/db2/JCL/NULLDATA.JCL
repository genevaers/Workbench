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
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..CODETABL.DATA';" > datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..CONTRLRC.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..ENVIRON.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..GLOBAFLD.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..GROUPTS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LFPFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOGFILE.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOGIC.DATA';"    >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOGREC.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOGRECIX.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOOKUP.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOOKUPSR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LOOKUPST.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LRFIELD.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LRFLDATR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LRFLDIDX.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..LRLFASS.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..MIGRAUDT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..NEXTKEY.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..PHYSFILE.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPEV.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPJN.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPLF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPLR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPPF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPUE.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPUR.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPVF.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SECGRPVW.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..SERVER.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..USEREXIT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..USERTS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VDPBATCH.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VDPBTCHV.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VFVASS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VIEWCOL.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VIEWFOLD.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VIEWHDFT.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VIEWSRC.DATA';"  >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VIEWTS.DATA';"   >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VWCOLSRC.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VWLOGDEP.DATA';" >> datafiles.attr;
tsocmd "LISTDS '&LOADHLQ..&&LOADMLQ..VWSORTKY.DATA';" >> datafiles.attr;

cd ../SH
./NullFiles.sh tmpinfo datafiles.attr
/*
//
