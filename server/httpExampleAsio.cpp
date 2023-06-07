#include <iostream>
#include <string>
#include <asio.hpp>

using namespace asio;
using namespace asio::ip;

// Handles an incoming HTTP request
void handleRequest(tcp::socket& socket)
{
    try
    {
        // Read the request
        streambuf buffer;
        asio::read_until(socket, buffer, "\r\n\r\n");

        // Print the request
        std::istream request_stream(&buffer);
        std::string request;
        std::getline(request_stream, request);
        std::cout << "Received request: " << request << std::endl;

        // Send HTTP response
        std::string response = "HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, World!";
        asio::write(socket, asio::buffer(response));

        // Close the connection
        socket.shutdown(tcp::socket::shutdown_both);
    }
    catch (std::exception& e)
    {
        std::cerr << "Exception: " << e.what() << std::endl;
    }
}

int main()
{
    // Create the I/O context
    asio::io_context io_context;

    // Create the acceptor and bind it to port 8080
    tcp::acceptor acceptor(io_context, tcp::endpoint(tcp::v4(), 8080));

    // Accept and handle incoming connections
    while (true)
    {
        // Create a socket
        tcp::socket socket(io_context);

        // Wait for and accept a connection
        acceptor.accept(socket);

        // Handle the connection
        handleRequest(socket);
    }

    return 0;
}

