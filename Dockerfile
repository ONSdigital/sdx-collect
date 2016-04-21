FROM java

# Add the build artifacts
WORKDIR /usr/src
#ADD git_commit_id /usr/src/
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/
ADD ./src/main/resources/ons-cacert.txt /usr/src/

EXPOSE 8080

RUN echo $JAVA_HOME/jre/lib/security/cacerts
RUN keytool -import -noprompt -trustcacerts -file /usr/src/ons-cacert.txt -alias ons -storepass changeit \
      -keystore $JAVA_HOME/jre/lib/security/cacerts

# Set the entry point
ENTRYPOINT java -Xmx4094m \
          -Drestolino.packageprefix=com.github.onsdigital.perkin.api \
          -jar target/*-jar-with-dependencies.jar
