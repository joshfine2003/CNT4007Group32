@echo off

del *.class
javac ./peerProcess.java

start cmd /k "java peerProcess 1001 > 1001output.txt" 
start cmd /k "java peerProcess 1002 > 1002output.txt"
start cmd /k "java peerProcess 1003 > 1003output.txt"
start cmd /k "java peerProcess 1004 > 1004output.txt"
start cmd /k "java peerProcess 1005 > 1005output.txt"
start cmd /k "java peerProcess 1006 > 1006output.txt"