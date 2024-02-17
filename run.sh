#!/bin/bash

exec java -cp ".:dist/*" \
  -Djavax.net.ssl.keyStore=vertisykey.jks \
  -Djavax.net.ssl.keyStorePassword=papapapakaka \
  -Djavax.net.ssl.trustStore=vertisykey.jks \
  -Djavax.net.ssl.trustStorePassword=papapapakaka \
  -Dwzpath=wz\ \
  net.server.Server