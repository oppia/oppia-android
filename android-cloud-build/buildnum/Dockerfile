FROM gcr.io/cloud-builders/gsutil

COPY increment_buildnumber /bin

RUN chmod +x /bin/increment_buildnumber

ENTRYPOINT ["increment_buildnumber"]