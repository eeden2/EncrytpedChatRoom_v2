# EncrytpedChatRoom_v2
Following a Client-Server Modelm this program is a simple chat room that is utilized via sockets and Bufferedwriters. The main idea was to however make the connection secure with the user who trusts the authenticity and location of the server. The server distributes its own public key to the users who then encrypt and send messages to the server. The server then distributes the message to all clients with their own public key that is sent upon intial connection to the server.

Screenshots are below of Active Connections and Successful transmission of data.

Server:
![alt text] (Screenshots/server with multiple users.png)


Client:
![alt text] (Screenshots/in client window.png)
