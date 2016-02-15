REPONAME = experiments-manager
DOCKERIMAGENAME = benchflow/$(REPONAME)
VERSION = dev
JAVA_VERSION_FOR_COMPILATION = java-8-oracle 
JAVA_HOME := `update-java-alternatives -l | cut -d' ' -f3 | grep $(JAVA_VERSION_FOR_COMPILATION)`"/jre"

.PHONY: all build_release 

all: build_release

clean:
	mvn clean

build:
	JAVA_HOME=$(JAVA_HOME) mvn -U package

build_release:
	JAVA_HOME=$(JAVA_HOME) mvn -U install

install:
	JAVA_HOME=$(JAVA_HOME) mvn -U install

test:
	JAVA_HOME=$(JAVA_HOME) mvn -U test

build_container_local:
	JAVA_HOME=$(JAVA_HOME) mvn -U package
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile.test .
	rm target/benchflow-$(REPONAME).jar

test_container_local:
	docker run -ti --rm -e "MINIO_ADDRESS=$(MINIO_ADDRESS)" -e "MINIO_ACCESS_KEY=$(MINIO_ACCESS_KEY)" \
	-e "MINIO_SECRET_KEY=$(MINIO_SECRET_KEY)" -e "ENVCONSUL_CONSUL=$(ENVCONSUL_CONSUL)" \
	-e "FABAN_ADDRESS=$(FABAN_ADDRESS)" -e DRIVERS_MAKER_ADDRESS=$(DRIVERS_MAKER_ADDRESS) \
	-p 8080:8080 --name $(REPONAME) $(DOCKERIMAGENAME):$(VERSION)


rm_container_local:
	docker rm -f -v $(REPONAME)
