# Developer Notes

This documents the steps to establish a development environment.  At present it does not cover:
- Git, to replicate the repo
- Postgres or DB2 for the database

This document was created on a Mac, but attempts to be inclusive of multiple platforms.

## Eclipse
1. Download "Eclipse IDE for RCP and RAP Developers"
2. Build the project with Maven (mvn install) to generated required jar files
3. When opening Eclipse and asked for a workspace, choose a DIFFERENT workspace than the GIT repository for the code.
4. Import Project:  Select the local Git WB420 Repo and the plugin/genevagui and the plugin/nebulagrid directory
5. Go to buildpath/libraries and add external jars, and then find jar files generated by step 2.  They are contained in
...wb420/products/com.ibm.safr.we.product/target/products/wb/macosx/cocoa/x86_64/Eclipse.app/Contents/Eclipse/plugins
6. Add Jars button as well, expand GenevaERS and expand lib directory, and select all Jars in that directory
7. The build process in Eclipse should build the project correctly with no errors

## Debug configurations
1. Select debug config
2. Select "Eclipse application" and then select "new" button (top left hand corner) to make a new configuration.
3. Select "run a product" and select GenevaERS from drop down box
4. Select Java 1.8 for execution environment
5. Perhaps allow incoming connections

## Junit test configuration in Eclipse
1. In build path, added test directory to source folder
2. In build path, added the wb420/plugins/genevagui/bin as an External Class Folder.