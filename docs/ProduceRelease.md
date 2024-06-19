---
layout: page
title: How to make a release
nav_order: 90
---
# GenevaERS Workbench Developer
## Release Procedures 

-----
To create a GenevaERS Release, do the following:
1. Remove the GERS_JARS environment variable from .bashrc
2. Type 
   ```./build.sh GenevaERS Project```
    on the command line
3. In File Explorer navigate to: ```...Workbench\products\com.ibm.safr.we.product\target\products``` to locate the appropriate operating system zip file.
4. Post this file as a release on GitHub.