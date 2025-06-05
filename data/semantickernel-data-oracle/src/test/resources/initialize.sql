-- Exit on any errors
WHENEVER SQLERROR EXIT SQL.SQLCODEAdd commentMore actions

-- Configure the size of the Vector Pool to 1 GiB.
ALTER SYSTEM SET vector_memory_size=1G SCOPE=SPFILE;

sqlplus / as sysdba

SHUTDOWN ABORT;
STARTUP;

exit