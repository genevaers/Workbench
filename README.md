# Workbench 4.20

The workbench is the configuration tool for [GeneavaERS](https://genevaers.org/).  
It is a Java application that can be run on Windows or a Mac.  
The build can easily be updated to run on a Unix system too.

This version of the workbench supports both Db2 and PostgreSQL database connections.

# PreRequisite

You will need to have installed Java and a version of Maven.  
The Maven version used for development was 3.9.4.

Your version of Java must be higher than 1.8.  The development version was 17.0.6.

The final optional step of the build adds a JRE to the distributable zip.
To do this the build script assumes you have [7Zip](https://www.7-zip.org/download.html) installed and available on your path.
If you want to use a different zip utiltity edit the shell script postbuild/postbuild.sh.

# Environment Variables

There are two key environment variables used by the build.
You do not need to set either of them if you simply want a Postgres only version to run with your installed Java.

## GERS_JARS
This environment variable is used to provide the location of additional jars to be added to the build.

In particular to add licensed Db2 driver jars.

Ensure that your system sets the environment varable to the path of jars. 
Or for example within git bash you can enter 

```
export GERS_JARS=~/gersjars
```
## GERS_GRAMMAR

If you wish to point to a custom location for GenevaERS Grammar, a pre-requisite for the Workbench, set this environment variable to the location of your desired repo.

```
export GERS_GRAMMAR="https://github.com/genevaers/Grammar.git"
```

## GERS_PRE_SCRIPT or GERS_POST_SCRIPT

If you wish to execute a custom script prior to or after the build process, set either of these environment variable to the location of your desired scripts.

```
export GERS_PRE_SCRIPT="C:\prescript.sh"
export GERS_POST_SCRIPT="C:\postscript.sh"
```

## GERS_JRE

If you wish to include a runtime JRE into the distributable Windows zip set this environment variable to
the location of your desired JRE. The JRE version should be greater than 8.

A suitable location from which to download a JRE is https://adoptium.net/en-GB/

Assume you downloaded your JRE to a directory 'Users/me/Java' then you could for example enter.

```
export GERS_JRE=/Users/me/Java/OpenJDK17U-jre_x64_windows_hotspot_17.0.7_7/jdk-17.0.7+7-jre
```

# To Build

With both Java and Maven installed enter

```./build.sh```

Once the build has completed successfully to test the workbench;

if on Windows enter   
```./runWorkbench.sh```
    
if on Mac enter  
```./runMacWorkbench.sh```

The distributable zips are located in.

```products/com.ibm.safr.we.product/target/products/```

# License Checking

In order to run the Apache license checker enter

``` mvn validate -Prat```

# PostgreSQL Database Install  
Install PostgreSQL as per its instructions ...  

	Add Environment variable PATH values for both PostgreSQL directories for bin and lib  

On windows the typical location depending on your installed version is 

    C:\Program Files\PostgreSQL\12\bin

Note the admin username and password you create as part of the install to be used later. We used the user *postgres* and the password *postgres*.  The database is intended to be a locally run only.
If you are sharing it then apply some more rigourous security measures :-)

Open a command prompt and run the following to ensure that Postgres and the associated psql are installed.

    $ psql --version
    psql (PostgreSQL) 12.4

The from the cloned directory change to the SQL resources as below.
    cd plugins/genevagui/resources/postgres/

And run psql to create and populate your database. 

    psql -h localhost -p 5432 -U postgres -v dbname=genevaers -v schemaV=gendev -f runall.sql

Explanation... 

    psql is the postgres command line app  
    -h the host ip address  
    -p the port number. 5432 is the default at the install  
    -U postgres is the name of the postgres admin user you created at install. Change the name if you chose something different when you installed PostgreSQL.  
    -v dbname=genevaers This creates a script variable called dbname and assigns it the value “genevaers”. That will be the name of the database.  
    -v schemaV=gendev is a script variable and assigns it the value “gendev”. That will be what the schema is named.  
    You can choose something else if you want.
    -f runall.sql Means run this file. This will create the database called genevaers, create the tables needed, perform an initial set up of those tables, install some stored procedures, and create a GenevaERS user called “postgres”.  

Start the postgres database (see Postgres documentation).  

On a Windows machine, from the command line, use the following script to start the Workbench. 
    
    runworkbench.sh

Many of the values above are used in the GenevaERS Connection Manager.

    - Select "Manage" and enter the following if you used the command above:
      - Database Type: PostgesQL
      - Database Name: genevaers
      - Server: localhost
      - Port: 5432
      - Scheme Name: gendev
      - User ID: postgres
      - Password: postgres
    - Select "Save" and on the GenevaERS Login screen enter:
      - User ID: postgres
      - Password: postgres
    - Click "Login"
    - Click the "Environment" dropdown
      - Select the "Development" environment
    - Click "OK"