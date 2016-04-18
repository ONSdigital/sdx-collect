FROM java

# Add the build artifacts
WORKDIR /usr/src
#ADD git_commit_id /usr/src/
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/
ADD ./src/main/resources/ons-root.cer /usr/src/
ADD ./src/main/resources/ons-intermediate-01.cer /usr/src/

EXPOSE 8080

<<<<<<< fcbd5d446aead303ec2fdbdcdf84983979325635
<<<<<<< a1fc0e127bd6659603ef04ea1e4139dce98f0c1c
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-root.cer -alias ons-root -storepass changeit \
      -keystore $JAVA_HOME/jre/lib/security/cacerts
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-intermediate-01.cer -alias ons-intermediate-01 -storepass changeit \
=======
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-cacert.txt -alias ons -storepass changeit \
>>>>>>> Removed unnecessary line.
=======
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-root.cer -alias ons-root -storepass changeit \
      -keystore $JAVA_HOME/jre/lib/security/cacerts
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-intermediate-01.cer -alias ons-intermediate-01 -storepass changeit \
>>>>>>> Split out root and intermediate certificates to see if we can validate with the server.
      -keystore $JAVA_HOME/jre/lib/security/cacerts

#

# Set the entry point
ENTRYPOINT java -Xmx4094m \
          -Drestolino.packageprefix=com.github.onsdigital.perkin.api \
          -jar target/*-jar-with-dependencies.jar
