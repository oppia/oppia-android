FROM gcr.io/cloud-builders/gcloud-slim

COPY deploy_to_gcs /bin

RUN chmod +x /bin/deploy_to_gcs

ENTRYPOINT ["deploy_to_gcs"]