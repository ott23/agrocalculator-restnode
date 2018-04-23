FROM tomcat:8.0.20-jre8

EXPOSE 8080

RUN rm -fr /usr/local/tomcat/webapps/ROOT
COPY target/restkafka-*.war /usr/local/tomcat/webapps/ROOT.war