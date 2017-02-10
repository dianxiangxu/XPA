default:
	mvn package
fat-jar:
	mvn package assembly:single
clean:
	mvn clean
javadoc:
	mvn javadoc:javadoc
