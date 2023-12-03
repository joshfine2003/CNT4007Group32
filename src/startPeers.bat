@echo off

del *.class
javac ./peerProcess.java

start cmd /k "java peerProcess 1001"
start cmd /k "java peerProcess 1002"
start cmd /k "java peerProcess 1003"
start cmd /k "java peerProcess 1004"
start cmd /k "java peerProcess 1005"
start cmd /k "java peerProcess 1006"
start cmd /k "java peerProcess 1007"
start cmd /k "java peerProcess 1008"
start cmd /k "java peerProcess 1009"
