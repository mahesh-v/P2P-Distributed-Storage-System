**************************************************************
P2P Distributed Storage System

**************************************************************

CS 6378: Advanced Operating Systems
Programming Project 2

The value used for time out by default is 500ms per hop count. This can be modified at run time.

USAGE:
The program is in CLI and offers to following functionalities:

1. Search
Syntax: search<tab><search_phrase>
The <tab> is the \t character and the search_phrase can either be the file name, or the key words associated.
The program by default searches with hop count 1, 2, 4, 8 and 16 until it finds a result.

2. Download file (to be executed after search)
Syntax: get <number>
When a search result is displayed, each result is indexed by a number, starting from 1. The user can then choose which result he/she is interested in, and have the file downloaded from there.

3. List neighbors
Syntax: neighbors
This will list out all neighbors to this node

4. Index
Syntax: index
This will display a list of files contained by the current node.

5. Quit
Syntax: quit
To gracefully exit the system, ensuring no node is left disconnected.
