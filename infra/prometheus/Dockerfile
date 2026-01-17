FROM prom/prometheus:v2.46.0

COPY prometheus.prod.yml /etc/prometheus/prometheus.yml

CMD [ "--config.file=/etc/prometheus/prometheus.yml", \
"--enable-feature=exemplar-storage" ]