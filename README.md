# GenevaERS-WorkBench  
To build  
:gear:  

## Hardware and software requirements on Windows systems  

The original code was developed via... 

[An install of **AdoptOpenJDK Java 8 for 32-bit**](https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=openj9)

[An install of **Gradle v6.6.1**](https://gradle.org/releases/)

[An install of the latest **PostgreSQL**](https://www.postgresql.org/download/)

[An install of **Eclipse Luna IDE for Java Developers 32-bit**](https://www.eclipse.org/downloads/packages/release/luna/sr2)

<<<<<<< HEAD
Any JDK Java 8 will do and any subsequent Eclipse will also suffice. Although we have seen some SWT issues with the latest release.
The code is not limited to 32 bit.
=======
Any Java 8 will do and any Eclipse will also suffice. Although we have seen some SWT issues with the latest release.
>>>>>>> 193daf06c3442ce83e5c66a615e5f8e9ad804acc

Clone the GenevaERS-WorkBench repository from github somewhere on your machine. We Cloned it under *youruser*  
directory in Windows.     


## Installation, configuration and Build on Windows
Instructions for installation and configuration of software and building of GenevaERS-WorkBench.


### AdoptOpenJDK 
Download the .zip file NOT the .msi.  
Extract the downloaded zip somewhere onto you machine.  
Add PATH entry to point to the bin directory of the unzipped JDK. 


### Gradle 
Download the complete file NOT the binary-only.
Extract the downloaded zip somewhere onto you machine.  


### GenevaERS-WorkBench
Open a git Bash window and run commands where you have Cloned the GenevaERS-WorkBench to ...  

    cd WorkBench\sycadas  
    gradle buildAndCopySycadas 
    gradle eclipse 


:thinking:  

### PostgreSQL Database Install  
Install PostgreSQL as per its instructions ...  

	Add Environment variable PATH values for both PostgreSQL directories for bin and lib  

Note the admin username and password you create as part of the install to be used later. We used the user *postgres*  
and the password *postgres*.   

Open a command prompt and run the following...  

C:\Users\youruser> cd WorkBench\Java_GUI\resources  
C:\Users\youruser\WorkBench\Java_GUI\resources> psql -h localhost -p 5432 -U postgres -v dbname=genevaers -v schemaV=gendev -f runall.sql


    Explanation... 
        Psql is the postgres command line app  
        -h the host ip address  
        -p the port number. 5432 is the default at the install  
        -U postgres is the name of the postgres admin user you created at install. Change the name if you chose something different when you installed PostgreSQL.  
        -v dbname=genevaers This creates a script variable called dbname and assigns it the value “genevaers”. That will be the name of the database.  
        -v schemaV=gendev is a script variable and assigns it the value “gendev”. That will be what the schema is named.  
        You can choose something else if you want.
        -f runall.sql Means run this file. This will create the database called genevaers, create the tables needed, perform an initial set up of those tables, install some stored procedures, and create a GenevaERS user called “postgres”.  



### Eclipse
The workbench is an Eclipse RCP Application.  

Extract the Eclipse Luna IDE for Java Developers 32-bit somewhere onto your machine. We unzipped it under *youruser*  
directory in Windows.     

Open Eclipse and create a new workspace.  
Import the projects under the Cloned “Workbench” directory you put on your machine...    

	File>Import  
	Select General>“Existing Projects into Workspace” press NEXT.  
	Press Browse for Select Root Directory and find the Cloned "WorkBench" directory and select it.  
	Select Option "Search for nested projects".  
	Press Finish.  

    If you have any errors in the Problems tab. Select all the Package and run Project>Clean to rebuild all projects.
   

:confused:  


## Run the GenevaERS WorkBench from Eclipse
Select the GenevaERS Package from the Package Explorer tab and right click on it and select "Run As> Eclpise Application".  
You will get a window that says **OPEN MAINFRAME PROJECT GenevaERS - The Single-Pass Optimization Engine** <img src ="Java_GUI/splash.bmp">  with a popup **GenevaERS Login window**.
In the GenevaERS Login you need to fill out information to connect to the postgres DB you installed...  
```  
    -    Connection Name is anything you want it to be.
    -    Database Name *genevaers* is what we just created.
    -    Server and port are to connect to Postgres... 
         -	    Server is *localhost*
         -	    Port is *5432*  
    -    Schema Name *gendev* is what we just created.  
    -    User id to connect to the database is *postgres* and password *postgres* or whatever you used when installing the PostgreSQL database.  
    -    Press Save.  
```
You will now have a window to put the WorkBench Login User ID. The default ID at installation is *ADMIN* with NO password. Enter these and press the Login button.
