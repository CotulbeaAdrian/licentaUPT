#include <iostream>
#include <string>
#include <sstream>
#include <asio.hpp>
#include <mysql_driver.h>
#include <mysql_connection.h>
#include <cppconn/prepared_statement.h>
#include <cstring> // Include the <cstring> header for strlen
#include <iomanip> // Include the <iomanip> header for std::hex and std::isdigit

using namespace asio;
using namespace asio::ip;

// URL decode function
std::string urlDecode(std::string &SRC) {
    std::string ret;
    char ch;
    int i, ii;
    for (i=0; i<SRC.length(); i++) {
        if (SRC[i]=='%') {
            sscanf(SRC.substr(i+1,2).c_str(), "%x", &ii);
            ch=static_cast<char>(ii);
            ret+=ch;
            i=i+2;
        } else {
            ret+=SRC[i];
        }
    }
    return (ret);
}

// Handles an incoming HTTP request
void handleRequest(tcp::socket& socket, const std::string& request, sql::mysql::MySQL_Driver* driver)
{
    std::string response_header;
    std::string response_body;

    // Extract the request method and path
    std::stringstream request_stream(request);
    std::string method, path, http_version;
    request_stream >> method >> path >> http_version;

    // Check the request path and generate the appropriate response



    // ### REGISTER ###



    if (method == "POST" && path == "/register")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t fullNameStart = decoded_line.find("fullName=") + 9; // Find the start position of the fullName and add the length of "fullName="
        size_t fullNameEnd = decoded_line.find("&"); // Find the position of the "&" delimiter
        std::string fullName = decoded_line.substr(fullNameStart, fullNameEnd - fullNameStart); // Extract the fullName substring
        
        size_t emailStart = decoded_line.find("email=") + 6; // Find the start position of the email and add the length of "email="
        size_t emailEnd = decoded_line.find("&", emailStart); // Find the position of the "&" delimiter starting from emailStart       
        std::string email = decoded_line.substr(emailStart, emailEnd - emailStart); // Extract the email substring

        size_t phoneNumberStart = decoded_line.find("phoneNumber=") + 12; // Find the start position of the phoneNumber and add the length of "phoneNumber="
        size_t phoneNumberEnd = decoded_line.find("&", phoneNumberStart); // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string phoneNumber = decoded_line.substr(phoneNumberStart, phoneNumberEnd - phoneNumberStart); // Extract the phoneNumber substring

        size_t passwordStart = decoded_line.find("password=") + 9; // Find the start position of the password and add the length of "password="
        size_t passwordEnd = decoded_line.find("&", passwordStart); // Find the position of the "&" delimiter starting from passwordStart
        std::string password = decoded_line.substr(passwordStart, passwordEnd - passwordStart); // Extract the password substring

        size_t roleStart = decoded_line.find("role=") + 5; // Find the start position of the role and add the length of "role="
        size_t roleEnd = decoded_line.find("&", roleStart); // Find the position of the "&" delimiter starting from roleStart
        std::string role = decoded_line.substr(roleStart, roleEnd - roleStart); // Extract the role substring

        // Check if all required fields are present
        if (!fullName.empty() && !email.empty() && !phoneNumber.empty()  && !password.empty() && !role.empty())
        {
            try {
                // Create a MySQL connection
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                
                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("INSERT INTO users (fullName, email, phoneNumber, password, role) VALUES (?, ?, ?, ?, ?)"));
                pstmt->setString(1, fullName);
                pstmt->setString(2, email);
                pstmt->setString(3, phoneNumber);
                pstmt->setString(4, password);
                pstmt->setString(5, role);

                // Execute the SQL statement
                pstmt->execute();

                // Build the response body
                response_header += "HTTP/1.1 200 OK\r\n";
                response_body = "User registered successfully";

                delete con;
            }
            catch (std::exception& e) {
                response_header += "HTTP/1.1 500 Internal Server Error\r\n";
                std::cerr << "Registration failed: " << e.what() << std::endl;
                response_body = "Registration failed";
            }
        }
        else
        {
            response_header += "HTTP/1.1 400 Bad Request";
            response_body = "Invalid register request";
        }
    }



    // ### LOGIN ###



    else if (method == "POST" && path == "/login")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }
        // URL decode the request body
        std::string decoded_line = urlDecode(line);
        
        size_t emailStart = decoded_line.find("email=") + 6; // Find the start position of the email and add the length of "email="
        size_t emailEnd = decoded_line.find("&"); // Find the position of the "&" delimiter
        std::string email = decoded_line.substr(emailStart, emailEnd - emailStart); // Extract the email substring

        size_t passwordStart = decoded_line.find("password=") + 9;
        size_t passwordEnd = decoded_line.find("&", passwordStart);
        std::string password = decoded_line.substr(passwordStart, passwordEnd - passwordStart);

        // Check if email and password are present
        if (!email.empty() && !password.empty())
        {
            try 
            {
                // Create a MySQL connection
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?"));
                pstmt->setString(1, email);
                pstmt->setString(2, password);

                sql::ResultSet* res = pstmt->executeQuery();

                // Check if a matching user was found
                if (res->next())
                {
                    // Retrieve the user's ID and username
                    int userId = res->getInt("id");
                    std::string fullName = res->getString("fullName");
                    std::string email = res->getString("email");
                    std::string phoneNumber = res->getString("phonenumber");
                    std::string role = res->getString("role");

                    // Build the response body with user and user details information
                    std::ostringstream response_aux;
                    response_aux << "Login successful." << std::endl;
                    response_aux << "id=" << userId << std::endl;
                    response_aux << "fullName=" << fullName << std::endl;
                    response_aux << "email=" << email << std::endl;
                    response_aux << "phoneNumber=" << phoneNumber << std::endl;
                    response_aux << "role=" << role << std::endl;

                    response_header += "HTTP/1.1 200 OK\r\n";
                    response_body = response_aux.str();

                }
                else
                {
                    // User not found or invalid credentials
                    response_header += "HTTP/1.1 401 Unauthorized\r\n";
                    response_body = "Invalid email or password";
                }

                delete res;
                delete con;

            }
            catch (std::exception& e) 
            {
                std::cerr << "Login request failed: " << e.what() << std::endl;
                response_header += "HTTP/1.1 500 Internal Server Error\r\n";
                response_body = "Login request failed";
            }
        }
        else
        {
            response_header += "HTTP/1.1 400 Bad Request";
            response_body = "Invalid login request";
        }
    }

        // ### Update profile ###



    else if (method == "POST" && path == "/updateProfile")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);


        size_t idStart = decoded_line.find("id=") + 3; // Find the start position of the id and add the length of "id="
        size_t idEnd = decoded_line.find("&"); // Find the position of the "&" delimiter
        std::string id = decoded_line.substr(idStart, idEnd - idStart); // Extract the id substring

        size_t fullNameStart = decoded_line.find("fullName=") + 9; // Find the start position of the fullName and add the length of "fullName="
        size_t fullNameEnd = decoded_line.find("&", fullNameStart); // Find the position of the "&" delimiter
        std::string fullName = decoded_line.substr(fullNameStart, fullNameEnd - fullNameStart); // Extract the fullName substring

        size_t phoneNumberStart = decoded_line.find("phoneNumber=") + 12; // Find the start position of the phoneNumber and add the length of "phoneNumber="
        size_t phoneNumberEnd = decoded_line.find("&", phoneNumberStart); // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string phoneNumber = decoded_line.substr(phoneNumberStart, phoneNumberEnd - phoneNumberStart); // Extract the phoneNumber substring

        size_t ageStart = decoded_line.find("age=") + 4; // Find the start position of the age and add the length of "age="
        size_t ageEnd = decoded_line.find("&", ageStart); // Find the position of the "&" delimiter starting from ageStart
        std::string age = decoded_line.substr(ageStart, ageEnd - ageStart); // Extract the age substring

        size_t weightStart = decoded_line.find("weight=") + 7; // Find the start position of the weight and add the length of "weight="
        size_t weightEnd = decoded_line.find("&", weightStart); // Find the position of the "&" delimiter starting from weightStart
        std::string weight = decoded_line.substr(weightStart, weightEnd - weightStart); // Extract the weight substring
        
        size_t genderStart = decoded_line.find("gender=") + 7; // Find the start position of the gender and add the length of "gender="
        size_t genderEnd = decoded_line.find("&", genderStart); // Find the position of the "&" delimiter starting from genderStart       
        std::string gender = decoded_line.substr(genderStart, genderEnd - genderStart); // Extract the gender substring

        // Check if all required fields are present
        if (!id.empty() && !fullName.empty() && !phoneNumber.empty() && !age.empty()  && !weight.empty() && !gender.empty())
        {
            try {
                // Create a MySQL connection
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                
                // Update fullName and phoneNumber in the users table
                std::unique_ptr<sql::PreparedStatement> userStmt(con->prepareStatement("UPDATE users SET fullName = ?, phoneNumber = ? WHERE id = ?"));
                userStmt->setString(1, fullName);
                userStmt->setString(2, phoneNumber);
                userStmt->setString(3, id);
                int userRowsAffected = userStmt->executeUpdate();

                // Update the record in the otherTable table if it exists
                std::unique_ptr<sql::PreparedStatement> userDetailsStmt(con->prepareStatement("UPDATE userDetails SET age = ?, weight = ?, gender = ? , userId = ? WHERE userId = ?"));
                userDetailsStmt->setString(1, age);
                userDetailsStmt->setString(2, weight);
                userDetailsStmt->setString(3, gender);
                userDetailsStmt->setString(4, id);
                userDetailsStmt->setString(5, id);
                int userDetailsRowsAffected = userDetailsStmt->executeUpdate();

                if (userDetailsRowsAffected <= 0) {
                    // Insert the record into the otherTable table if it doesn't exist
                    std::unique_ptr<sql::PreparedStatement> insertStmt(con->prepareStatement("INSERT INTO userDetails (userId, age, weight, gender) VALUES (?, ?, ?, ?)"));
                    insertStmt->setString(1, id);
                    insertStmt->setString(2, age);
                    insertStmt->setString(3, weight);
                    insertStmt->setString(4, gender);
                    insertStmt->executeUpdate();
                }

                // Build the response body
                response_header += "HTTP/1.1 200 OK\r\n";
                response_body = "Profile updated successfully";

                delete con;
            }
            catch (std::exception& e) {
                response_header += "HTTP/1.1 500 Internal Server Error\r\n";
                std::cerr << "Update failed: " << e.what() << std::endl;
                response_body = "Update failed";
            }
        }
        else
        {
            response_header += "HTTP/1.1 400 Bad Request";
            response_body = "Invalid update request";
        }
    }


        // ### Medical Records ###



    else if (method == "POST" && path == "/getMedicalRecordsAsPatient")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }
        // URL decode the request body
        std::string decoded_line = urlDecode(line);
        
        size_t idStart = decoded_line.find("id=") + 3; // Find the start position of the id and add the length of "id="
        std::string id = decoded_line.substr(idStart); // Extract the id substring

        // Check if email and password are present
        if (!id.empty())
        {
            try 
            {
                // Create a MySQL connection
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM medical_records WHERE patientID = ?"));
                pstmt->setString(1, id);

                sql::ResultSet* res = pstmt->executeQuery();

                // Check if a matching user was found
                if(res->next())
                {
                    do
                    {
                        // Retrieve the user's ID and username
                        int id = res->getInt("id");
                        std::string active = res->getString("active");
                        std::string accepted = res->getString("accepted");
                        std::string patientID = res->getString("patientID");
                        std::string doctorID = res->getString("doctorID");
                        std::string symptom = res->getString("symptom");
                        std::string diagnostic = res->getString("diagnostic");
                        std::string medication = res->getString("medication");

                        // Build the response body with user and user details information
                        std::ostringstream response_aux;
                        response_aux << "id=" << id << std::endl;
                        response_aux << "active=" << active << std::endl;
                        response_aux << "accepted=" << accepted << std::endl;
                        response_aux << "patientID=" << patientID << std::endl;
                        response_aux << "doctorID=" << doctorID << std::endl;
                        response_aux << "symptom=" << symptom << std::endl;
                        response_aux << "diagnostic=" << diagnostic << std::endl;
                        response_aux << "medication=" << medication << "&" << std::endl;

                        response_header += "HTTP/1.1 200 OK\r\n";
                        response_body += response_aux.str();
                    } while (res->next());
                }
                else
                {
                    // User not found or invalid credentials
                    response_header += "HTTP/1.1 401 Unauthorized\r\n";
                    response_body = "Invalid email or password";
                }

                delete res;
                delete con;

            }
            catch (std::exception& e) 
            {
                std::cerr << "Data request failed: " << e.what() << std::endl;
                response_header += "HTTP/1.1 500 Internal Server Error\r\n";
                response_body = "Data request failed";
            }
        }
        else
        {
            response_header += "HTTP/1.1 400 Bad Request";
            response_body = "Invalid data request";
        }
    }

    else if (method == "POST" && path == "/getName")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }
        // URL decode the request body
        std::string decoded_line = urlDecode(line);
        
        size_t idStart = decoded_line.find("id=") + 3; // Find the start position of the id and add the length of "id="
        std::string id = decoded_line.substr(idStart); // Extract the id substring

        // Check if email and password are present
        if (!id.empty())
        {
            try 
            {
                // Create a MySQL connection
                sql::Connection* con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT fullName FROM users WHERE id = ?"));
                pstmt->setString(1, id);

                sql::ResultSet* res = pstmt->executeQuery();

                // Check if a matching user was found
                if(res->next())
                {
                    do
                    {
                        // Retrieve the user's ID and username
                        std::string fullName = res->getString("fullName");

                        response_header += "HTTP/1.1 200 OK\r\n";
                        response_body = "fullName=" + fullName + "\n";
                    } while (res->next());
                }
                else
                {
                    // User not found or invalid credentials
                    response_header += "HTTP/1.1 401 Unauthorized\r\n";
                    response_body = "Invalid email or password";
                }

                delete res;
                delete con;

            }
            catch (std::exception& e) 
            {
                std::cerr << "Data request failed: " << e.what() << std::endl;
                response_header += "HTTP/1.1 500 Internal Server Error\r\n";
                response_body = "Data request failed";
            }
        }
        else
        {
            response_header += "HTTP/1.1 400 Bad Request";
            response_body = "Invalid data request";
        }
    }

    // ### UNKNOWN REQUEST ###

    else
    {
        response_body = "Unknown request";
    }

    // Build the HTTP response
    response_header += "Content-Type: text/plain\r\n";
    response_header += "Content-Length: " + std::to_string(response_body.length()) + "\r\n";
    response_header += "Connection: close\r\n\r\n";

    std::string response = response_header + response_body;

    // Send the response
    asio::write(socket, asio::buffer(response));

    // Close the socket
    socket.close();
}



// Handles incoming connections
void startServer(asio::io_context& io_context, short port, sql::mysql::MySQL_Driver* driver)
{
    tcp::acceptor acceptor(io_context, tcp::endpoint(tcp::v4(), port));

    while (true)
    {
        tcp::socket socket(io_context);
        acceptor.accept(socket);

        std::string request;

        // Read the request
        char data[1024];
        size_t bytes_read = socket.read_some(asio::buffer(data, sizeof(data)));

        // Append the received data to the request string
        request.append(data, data + bytes_read);

        // Handle the request
        handleRequest(socket, request, driver);
    }
}

int main()
{
    try
    {
        // Initialize the MySQL driver
        sql::mysql::MySQL_Driver* driver;
        driver = sql::mysql::get_mysql_driver_instance();

        // Create an ASIO io_context
        asio::io_context io_context;

        // Start the server on port 8080
        startServer(io_context, 8080, driver);
    }
    catch (std::exception& e)
    {
        std::cerr << "Server error: " << e.what() << std::endl;
    }

    return 0;
}
