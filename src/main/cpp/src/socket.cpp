/*
 * socket.cpp
 *
 *  Created on: 7 Jul 2017
 *      Author: alex
 */

#include "socket.h"

namespace astox {

using namespace std;


unsigned int AbstractServer::getMaxClients() const {
    return maxClients;
}


AbstractServer &AbstractServer::setMaxClients(unsigned int m) {
    maxClients = m;
    return *this;
}


unsigned int AbstractServer::getPort() const {
    return port;
}


AbstractServer & AbstractServer::setPort(unsigned int p) {
    port = p;
    return *this;
}




void TCPSocketServer::start_server(){
    int wsaresult, i = 1;
    stx_socket_init();

    int final_port = getPort();
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(final_port);

    // Create a SOCKET for connecting to server
    master_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if(stx_socket_is_invalid(master_socket))
    {
        stx_exit_socket_failed();
    }

    wsaresult = setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&i, sizeof(i));

    //exit on setsockopt failure
    if(stx_sock_result_has_error(wsaresult))
    {
        stx_exit_socket_failed();
    }

    //Binding part
    wsaresult = bind(master_socket, (sockaddr*)&server, sizeof(server));

    //close socket on bind failed
    if(stx_sock_result_has_error(wsaresult))
    {
        stx_exit_socket_error(master_socket); //close socket
    }

    // Setup the TCP listening socket
    wsaresult = listen(master_socket, 5);

    //exit on listen failed
    if(stx_sock_result_has_error(wsaresult))
    {
        stx_exit_socket_failed();
    }


    //make it non blocking
    //ioctlsocket(master_socket, FIONBIO, &b);
    bool blocking = stx_set_socket_blocking_enabled(master_socket, true);

    //close socket on ioctl failed
    if(stx_socket_is_invalid(master_socket)){
        stx_exit_socket_error(master_socket); //close socket
    }

}





bool addSocket(SocketState states[], STX_SOCKET &socket, int what, int max_sockets)
{
	for (int i = 0; i < max_sockets; i++)
	{
		if (states[i].recv == EMPTY)
		{
			states[i].socket = socket;
			states[i].recv = what;
			states[i].send = IDLE;
			return (true);
		}
	}
	return (false);
}


void acceptConnection(int index, SocketState states[], int max_sockets){
    STX_SOCKET socket = states[index].socket;
	struct sockaddr_in from;		// Address of sending partner
	int fromLen = sizeof(from);

	STX_SOCKET msgSocket;

	#ifdef ASTOX_OS_WINDOWS
        msgSocket = accept(socket, (struct sockaddr *)&from, &fromLen);
	#else
        msgSocket = accept(socket, (struct sockaddr *)&from, (socklen_t*)&fromLen);
	#endif




	if(stx_socket_is_invalid(msgSocket)){
        stx_exit_socket_failed();
        return;
	}

	unsigned long flag = 1;
	if(!stx_set_socket_blocking_enabled(msgSocket, true)){
		stx_socket_ioctl_err();
	}


	if (addSocket(states, msgSocket, RECEIVE, max_sockets) == false)
	{
		cout<<"\t\tToo many connections, dropped!\n";
		stx_socket_close(msgSocket);
	}
	return;
}


void sendMessage(int index, SocketState states[], int max_sockets){
    cout << "SEND MESSAGE " << endl;
    send(states[index].socket, TEST_HTTP_MSG, strlen(TEST_HTTP_MSG), 0);
    states[index].send = IDLE;
}

void receiveMessage(int index, SocketState states[], int max_sockets){


	STX_SOCKET msgSocket = states[index].socket;

	//TODO read all
	char buffer[1025];  //data buffer of 1K
	int valread = recv( msgSocket , buffer, 1025, 0);

	if(valread > 0)
	{
		cout << "RECEIVED: " << endl;
		cout << buffer << endl;
	}

	states[index].send = SEND;


	cout << " RECEIVED DONE " << endl;

	sendMessage(index, states, max_sockets);
}





void TCPSocketServer::start(){
    //define clients
    int max_connections = getMaxClients();


    SocketState socketClients[max_connections];

    cout << "Server starting..." << endl;
    //start the server and do basic tcp setup ------------------
    start_server();
    addSocket(socketClients, master_socket, LISTEN, max_connections);
    //set of socket descriptors

    //the main loop
    while (active) {
        cout << " LOOP INIT " << endl;

        fd_set waitRecv;
        fd_set waitSend;
            //clear the socket set
        FD_ZERO(&waitRecv);
        FD_ZERO(&waitSend);

        int max_fd = master_socket;


        for (int i = 0; i < max_connections; i++)
		{
			if ((socketClients[i].recv == LISTEN) || (socketClients[i].recv == RECEIVE)) {
                FD_SET(socketClients[i].socket, &waitRecv);
			}

			if(socketClients[i].socket > max_fd){
				max_fd = socketClients[i].socket;
			}
		}

		for (int i = 0; i < max_connections; i++)
		{
			if (socketClients[i].send == SEND){
                FD_SET(socketClients[i].socket, &waitSend);
			}

			if(socketClients[i].socket > max_fd){
				//max_fd = socketClients[i].socket;
			}

		}

        //wait for an activity on one of the sockets , timeout is NULL ,
        //so wait indefinitely
        select_activity = select( max_fd + 1 , &waitRecv , &waitSend , NULL , NULL);

        if (select_activity <= 0) {
//            printf("select() returned with error %d\n", WSAGetLastError());
            printf("select() returned with error");

            exit(-3);
            return;
        }
           // cou  cout << " SECOND ITERATION DONE " << endl;t << "SELECT ACTIVITY SUCCESS " << select_activity << endl;

        cout << " LOOP SELECT DONE select_activity =  " << select_activity << endl;


        for(int j = 0; j < max_connections && select_activity > 0; j++)
        {
            if (FD_ISSET(socketClients[j].socket, &waitRecv))
			{
				select_activity--;
				switch (socketClients[j].recv)
				{
				case LISTEN:
				     cout << " listen" << j << endl;
					acceptConnection(j, socketClients, max_connections);
					break;

				case RECEIVE:
				    cout << " receive" << j << endl;
					receiveMessage(j, socketClients, max_connections);
					break;
				}
			}
        }

        //select_activity = 1;


        cout << "SECOND ITERATION max_connections = "<< max_connections << " select_activity=" << select_activity  << endl;

        for (int i = 0; i < max_connections && select_activity > 0; i++)
		{
        	cout << "socketClients[i].send = " << socketClients[i].send << endl;
			if (FD_ISSET(socketClients[i].socket, &waitSend))
			{
				select_activity--;
				switch (socketClients[i].send)
				{
				case SEND:
					sendMessage(i, socketClients, max_connections);
					break;
				}
			}
		}

         cout << " SECOND ITERATION DONE " << endl;

    }//end while

    // Closing connections and Winsock.
	cout << "Time Server: Closing Connection.\n";
	stx_socket_close(master_socket);
	stx_socket_cleanup();

}//end start

}

//http://www2.mta.ac.il/~hbinsky/intro%20comp%20comm/TCP%20server%20NonBlocking.cpp



