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
	JAVA_HOME=$(JAVA_HOME) mvn package

build_release:
	JAVA_HOME=$(JAVA_HOME) mvn install

install:
	JAVA_HOME=$(JAVA_HOME) mvn install

test:
	JAVA_HOME=$(JAVA_HOME) mvn test

build_container_local:
	JAVA_HOME=$(JAVA_HOME) mvn package
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile.test .
	rm target/benchflow-$(REPONAME).jar

test_container_local:
	#TODO

rm_container_local:
	#TODO