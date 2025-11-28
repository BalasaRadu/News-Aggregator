JCC = javac
JFLAGS = -g

JVM = java

MAIN = Tema1

CP = .:libs/*

default: build

build:
	$(JCC) $(JFLAGS) -cp "$(CP)" *.java

run:
	$(JVM) -cp "$(CP)" $(MAIN) $(ARGS)

clean:
	$(RM) *.class