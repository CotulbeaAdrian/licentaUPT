#include <iostream>
#include <string>
#include <sstream>
#include <asio.hpp>
#include <mysql_driver.h>
#include <mysql_connection.h>
#include <cppconn/prepared_statement.h>
#include <cstring> // Include the <cstring> header for strlen

using namespace asio;
using namespace asio::ip;

// Handles an incoming HTTP request
void handleRequest(tcp::socket& socket, const std::string& request, sql::mysql::MySQL_Driver* driver)
{
    std::string response_body;

    // Extract the request method and path
    std::stringstream request_stream(request);
    std::string method, path, http_version;
    request_stream >> method >> path >> http_version;
    
    // Check the request path and generate the appropriate response

//  ### REGISTER ###

    if (method == "POST" && path == "/register")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching to the last line which contains the request body
        }

        size_t usernameStart = line.find("username=") + 9; // Find the start position of the username and add the length of "username="
        size_t usernameEnd = line.find("&"); // Find the position of the "&" delimiter
        std::string username = line.substr(usernameStart, usernameEnd - usernameStart); // Extract the username substring

        size_t passwordStart = line.find("password=") + 9; // Find the start position of the password and add the length of "password="
        size_t passwordEnd = line.find("&", passwordStart); // Find the position of the "&" delimiter starting from passwordStart
        std::string password = line.substr(passwordStart, passwordEnd - passwordStart); // Extract the password substring

        size_t emailStart = line.find("email=") + 6; // Find the start position of the email and add the length of "email="
        std::string email = line.substr(emailStart); // Extract the email substring
        
        // Check if all required fields are present
        if (!username.empty() && !password.empty() && !email.empty())
        {
            try {
                // Establish a connection to the MySQL database
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin"); // Use the container name as the hostname
                con->setSchema("medbuddy");

                // Perform registration logic
                sql::PreparedStatement* stmt = con->prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
                stmt->setString(1, username);
                stmt->setString(2, password);
                stmt->setString(3, email);
                stmt->executeUpdate();
                delete stmt;

                response_body = "HTTP/1.1 200 OK\r\nContent-Length: 20\r\n\r\nRegistration success";

                delete con;
            } catch (sql::SQLException& e) {
                response_body = "HTTP/1.1 500 Internal Server Error\r\nContent-Length: " + std::to_string(strlen(e.what())) + "\r\n\r\n" + e.what();
            }
        }
        else
        {
            response_body = "HTTP/1.1 400 Bad Request\r\nContent-Length: 12\r\n\r\nInvalid data";
        }
    }

//  ### LOGIN ###

    else if (method == "POST" && path == "/login")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching to the last line which contains the request body
        }

        size_t usernameStart = line.find("username=") + 9; // Find the start position of the username and add the length of "username="
        size_t usernameEnd = line.find("&"); // Find the position of the "&" delimiter
        std::string username = line.substr(usernameStart, usernameEnd - usernameStart); // Extract the username substring

        size_t passwordStart = line.find("password=") + 9; // Find the start position of the password and add the length of "password="
        size_t passwordEnd = line.find("&", passwordStart); // Find the position of the "&" delimiter starting from passwordStart
        std::string password = line.substr(passwordStart, passwordEnd - passwordStart); // Extract the password substring

        // Check if both username and password are present
        if (!username.empty() && !password.empty())
        {
            try {
                // Establish a connection to the MySQL database
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin"); // Use the container name as the hostname
                con->setSchema("medbuddy");

                // Perform login logic
                sql::PreparedStatement* stmt = con->prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?");
                stmt->setString(1, username);
                stmt->setString(2, password);
                sql::ResultSet* res = stmt->executeQuery();

                if (res->next())
                {
                    int userId = res->getInt("id");
                    response_body = "HTTP/1.1 200 OK\r\nContent-Length: " + std::to_string(22 + std::to_string(userId).length()) + "\r\n\r\nLogin success\nUser ID:" + std::to_string(userId);
                }
                else
                {
                    response_body = "HTTP/1.1 401 Unauthorized\r\nContent-Length: 16\r\n\r\nLogin failed";
                }

                delete res;
                delete stmt;
                delete con;
            }
            catch (sql::SQLException& e) {
                response_body = "HTTP/1.1 500 Internal Server Error\r\nContent-Length: " + std::to_string(strlen(e.what())) + "\r\n\r\n" + e.what();
            }
        }
        else
        {
            response_body = "HTTP/1.1 400 Bad Request\r\nContent-Length: 12\r\n\r\nInvalid data";
        }
    }

//  ### UPDATE USER DETAILS ###

//  ###  ###

    else
    {
        response_body = "HTTP/1.1 404 Not Found\r\nContent-Length: 9\r\n\r\nNot Found";
    }

    // Send HTTP response
    asio::write(socket, asio::buffer(response_body));
    // Close the connection
    socket.shutdown(tcp::socket::shutdown_both);
}

int main()
{
    // Create the I/O context
    asio::io_context io_context;

    // Create the acceptor and bind it to port 8080
    tcp::acceptor acceptor(io_context, tcp::endpoint(tcp::v4(), 8080));

    // Create MySQL driver
    sql::mysql::MySQL_Driver* driver;
    driver = sql::mysql::get_mysql_driver_instance();

    // Accept and handle incoming connections
    while (true)
    {
        // Create a socket
        tcp::socket socket(io_context);

        // Wait for and accept a connection
        acceptor.accept(socket);

        // Read the request
        asio::streambuf buffer;
        asio::read_until(socket, buffer, "\r\n\r\n");

        // Extract the request string
        std::string request = asio::buffer_cast<const char*>(buffer.data());

        // Handle the request
        handleRequest(socket, request, driver);
    }

    return 0;
}
