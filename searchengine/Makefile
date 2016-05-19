SRC = ir/*.java
LIB = ./target/dependency/json-simple-1.1.1.jar
BIN = ./target/classes/

all:
	mvn clean install

gui:
	java -cp .:$(LIB):$(BIN) ir.SearchGUI -d ./myFiles

web:
	heroku local

clean:
	rm -f ir/*.class
