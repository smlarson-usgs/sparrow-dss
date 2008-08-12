Date: 2008-08-12
Author: I-Lin Kuo
Author contact info: ilinkuo@usgs.gov, ikuoikuo@gmail.com

The three sql files have been checked into subversion for convenience. These are generated files, generated from the sources in STREAM_NETWORK and SPARROW_DSS, and so would not normally be checked in. 

They should be run in the order:

Stream_network.sql
grants.sql
sparrow_dss.sql

NOTE: Some of the index creation lines have been commented out as they result in errors because the script creation via JDeveloper does not generate correct "create index" statements for spatial indices. A bug has been filed with Oracle -- see http://forums.oracle.com/forums/thread.jspa?threadID=693231&tstart=15.




