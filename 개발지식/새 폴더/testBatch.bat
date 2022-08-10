@echo off
set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_45"
set DAEMON_NAME="sendBatch"
set DAEMON_HOME="D:\imAgent"

echo ------------------------------------------------------------
echo -- receiveBatch V1.0
echo -- Update Date : 2020-02-04
echo ------------------------------------------------------------

set LIB_DIR=%DAEMON_HOME%\lib

set CLASSPATH_DIR=%LIB_DIR%\commons\commons-codec-1.9.jar;%LIB_DIR%\commons\commons-lang-2.4.jar;%LIB_DIR%\cipher\bcprov-jdk15on-148.jar;%LIB_DIR%\commons\commons-logging-1.2.jar;%LIB_DIR%\db\jdbc.mysql\mysql-connector-java-5.1.22-bin.jar;%LIB_DIR%\db\jdbc.mssql\msbase.jar;%LIB_DIR%\db\jdbc.mssql\mssqlserver.jar;%LIB_DIR%\db\jdbc.mssql\msutil.jar;%LIB_DIR%\db\jdbc.oracle\ojdbc7.jar;%LIB_DIR%\db\jdbc.tibero\tibero6-jdbc.jar;%LIB_DIR%\file\activation.jar;%LIB_DIR%\file\cos.ja;%LIB_DIR%\ftp\commons-net-1.4.1.jar;%LIB_DIR%\ftp\jakarta-oro-2.0.8.jar;%LIB_DIR%\httpclient\httpclient-4.5.1.jar;%LIB_DIR%\httpclient\httpclient-cache-4.5.1.jar;%LIB_DIR%\httpclient\httpclient-win-4.5.1.jar;%LIB_DIR%\httpclient\httpcore-4.4.3.jar;%LIB_DIR%\httpclient\httpmime-4.5.1.jar;%LIB_DIR%\mail\activation.jar;%LIB_DIR%\mail\javax.mail-1.5.5.jar;%LIB_DIR%\mail\javax.mail-api-1.5.5.jar;%LIB_DIR%\securus-ciim-agent-1.0.jar;%DAEMON_HOME%\classes

%JAVA_HOME%\bin\java -DagentName=%DAEMON_NAME% -showversion -Xms128m -Xmx1000m -classpath %CLASSPATH_DIR% com.securus.adaptor.WorkBatch %DAEMON_HOME% test

