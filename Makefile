REPONAME = experiments-manager
DOCKERIMAGENAME = benchflow/$(REPONAME)

DBNAME = mysql

VERSION = dev
JAVA_VERSION_FOR_COMPILATION = java-8-oracle 
JAVA_HOME := `update-java-alternatives -l | cut -d' ' -f3 | grep $(JAVA_VERSION_FOR_COMPILATION)`"/jre"
UNAME = $(shell uname)

find_java:
ifeq ($(UNAME), Darwin)
	$(eval JAVA_HOME := $(shell /usr/libexec/java_home))
endif

.PHONY: all build_release 

all: build_release

clean:
	mvn clean

build: find_java
	JAVA_HOME=$(JAVA_HOME) mvn -U package

build_release: find_java
	JAVA_HOME=$(JAVA_HOME) mvn -U install

install: find_java
	JAVA_HOME=$(JAVA_HOME) mvn -U install

test: find_java
	JAVA_HOME=$(JAVA_HOME) mvn -U test

build_container_local: find_java
	JAVA_HOME=$(JAVA_HOME) mvn -U package
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile.test .
	rm target/benchflow-$(REPONAME).jar

test_container_local: find_java
    docker run -p $(DB_PORT):3306 --name $(DBNAME) -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_USER=root -d mysql:latest
    docker run -ti --rm -e "ENVCONSUL_CONSUL=$(ENVCONSUL_CONSUL)" \
	-e "FABAN_ADDRESS=$(FABAN_ADDRESS)" -e "DRIVERS_MAKER_ADDRESS=$(DRIVERS_MAKER_ADDRESS)" \
	-e "DB_USER=$(DB_USER)" -e "DB_PASSWORD=$(DB_PASSWORD)" -e "DB_HOST=$(DB_HOST)" \
	-e "DB_PORT=$(DB_PORT)" -e "DB_NAME=$(DB_NAME)" \
	-p 8080:8080 --link=$(DBNAME) --name $(REPONAME) $(DOCKERIMAGENAME):$(VERSION)

rm_container_local:
    docker rm -f -v $(DBNAME)
	docker rm -f -v $(REPONAME)
