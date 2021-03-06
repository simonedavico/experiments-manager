FROM benchflow/base-images:dns-envconsul-java8_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV EXPERIMENTS_MANAGER_VERSION v-dev

# Get benchflow-experiments-manager
RUN wget -q --no-check-certificate -O /app/benchflow-experiments-manager.jar https://github.com/benchflow/experiments-manager/releases/download/$EXPERIMENTS_MANAGER_VERSION/benchflow-experiments-manager.jar

COPY configuration.yml /app/

COPY ./services/300-experiments-manager.conf /apps/chaperone.d/300-experiments-manager.conf
 
EXPOSE 8080
