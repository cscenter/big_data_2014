CSC Big data homeworks

Uses: java 7, maven.

Task1:

FileServer - server for files replica.
MetadataServer - master server.

Usage: FileServer [localServerPort] [masterPort] MetadataServer [localServerPort]

Reminder for me: mvn package java -cp BigDataj-1.0-SNAPSHOT-jar-with-dependencies.jar ru.hw1.file.FileServer java -cp BigDataj-1.0-SNAPSHOT-jar-with-dependencies.jar ru.hw1.metadata.MetaServer