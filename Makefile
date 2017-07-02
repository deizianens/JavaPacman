all:
	javac */*.java
clean:
	rm -rf */*.class
run:
	java main.Main
