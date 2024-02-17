@echo off
@title Vertisy
set CLASSPATH=.;dist\*
java -Djavax.net.ssl.keyStore=vertisykey.jks -Djavax.net.ssl.keyStorePassword=papapapakaka -Djavax.net.ssl.trustStore=vertisykey.jks -Djavax.net.ssl.trustStorePassword=papapapakaka -Dwzpath=wz\ net.server.Server
pause