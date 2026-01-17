FROM grafana/grafana:10.1.0

ENV GF_AUTH_ANONYMOUS_ENABLED=true
ENV GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
ENV GF_AUTH_DISABLE_LOGIN_FORM=true

COPY datasource.yml /etc/grafana/provisioning/datasources/datasources.yml