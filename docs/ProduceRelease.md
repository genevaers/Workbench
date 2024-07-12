---
layout: page
title: How to make a release
nav_order: 90
---
# GenevaERS Workbench Developer
## Release Procedures 

-----
To create a public (Postgres) GenevaERS Release, do the following:
1. Change the Release Candidate (RC)value in [UUtilities.java](..\plugins\genevagui\src\com\ibm\safr\we\ui\utilities\UIUtilities.java) file.
2. If build is a Version change  (i.e., 4.21.0) 
   1. Search for Version value (4.21.0) and change in every pom.xml file
   2. And change the Version also in [rcp.product](..\products\com.ibm.safr.we.product\com.ibm.safr.we.rcp.product) 
   3. Change in [Manifest.MF](..\plugins\genevagui\META-INF\MANIFEST.MF)
3. Before running build, remove the GERS_JARS environment variable from .bashrc
4. Type 
   ```./build.sh GenevaERS_Project```
    on the command line
5. In File Explorer navigate to: ```...Workbench\products\com.ibm.safr.we.product\target\products``` to locate the appropriate operating system zip file.
6. Post this file as a release on GitHub.
   1. Create Release
   2. Create Tag as part of release with WE_4.21.0_RC1 format
   3. Upload zip file from above.
7. Create the same tags in the following repos.  
   1. Grammar Repo
   2. Run-Control-Apps
      1. If there are no stand-alone executables to post as Releases, create only tags (in VS Code, not on web as it has no Tag only creation options)
      2. Use VS Code Shift-Control-P and search for `git create tag`
      3. Enter tag name and description
      4. Use VS Code Shift-Control-p and search for `git push tag`
      5. Select remote repo
8. Old Release Candidates that are superseded can be deleted.