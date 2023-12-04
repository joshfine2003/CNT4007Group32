# CNT4007 Group 32
This project implements a P2P file sharing software similar to BitTorrent in Java.

## Requirements
- Java Development Kit (JDK)

## Compilation
```bash
javac peerProcess.java
```

## Sample Execution
```bash
(Common.cfg and PeerInfo.cfg are required)
Device A: java peerProcess 1001
Device B: java peerProcess 1002
Device C: java peerProcess 1003
```

## Team Members
- Laura Chang (laurachang@ufl.edu)
    - Main contributer of peerProcess.java, Peer.java, PeerConnection.java, Logger.java
- Joshua Fine (joshua.fine@ufl.edu)
    - Main contributer of Message.java, MessageHandler.java, and Handshake.java
- Custer Gilchrest (c.gilchrest@ufl.edu)
    - Main contributer of ConfigHandler.java, PeerInfoHandler.java, Helper.java

## Youtube link
- LINK HERE

## Project Overview
The program starts by reading through configuration files to figure out information for each peer. When a peer is initialized, it sends a handshake to all previously initialized peers. After handshaking, peers exchange bitfields and corresponding interested/not interested messages. The peers then enter into a loop of sending each other information, terminating only after all peers have downloaded the entire file.

### Protocols
While exchanging information, peers deal with the following protocols:
1. **Choke/Unchoke**: A peer uploads its pieces to at most k preferred neighbors and 1 optimistically unchoked neighbor. Preferred neighbors are determined by the fastest download rate or randomly chosen if the sending peer has finished downloading. Remaining neighbors are choked.
2. **Interested/Not Interested**: Peers track the downloaded pieces of all other peers and send interested/not interested messages in accordance to whether neighbors have pieces the peer can download.
3. **Request/Piece**: Peers request and send pieces to each other using a random selection strategy. When a piece is downloaded, the peer sends all neighbors a message that it has the piece.
