@echo off
@title Vertisy World
set CLASSPATH=.;dist\*
java -Djavax.net.ssl.keyStore=vertisykey.jks -Djavax.net.ssl.keyStorePassword=papapapakaka -Djavax.net.ssl.trustStore=vertisykey.jks -Djavax.net.ssl.trustStorePassword=papapapakaka -Dwzpath=wz\ net.world.WorldServer 0
pause