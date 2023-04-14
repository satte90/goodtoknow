FROM docker-prod.dc.teliacompany.net/springfield/java:11-ldap
LABEL com.teliacompany.maintainer="Augustus, https://diva.teliasonera.net/confluence/display/dcvt"

ADD src/docker/start.sh /home/appuser/
RUN chmod +x /home/appuser/start.sh &&\
    chown appuser:appuser /home/appuser/start.sh

ADD target/tiberius-user-server.jar /home/appuser/
RUN chown appuser:appuser /home/appuser/tiberius-user-server.jar

# Run as non-root user for security reasons
USER appuser

EXPOSE 8080
ENTRYPOINT ["/home/appuser/start.sh"]
