#include <iostream>
#include <string>
#include <sstream>
#include <asio.hpp>
#include <mysql_driver.h>
#include <mysql_connection.h>
#include <cppconn/prepared_statement.h>

using namespace asio;
using namespace asio::ip;

std::string success = "HTTP/1.1 200 OK\r\n";
std::string unauthorized = "HTTP/1.1 401 Unauthorized\r\n";
std::string internalError = "HTTP/1.1 500 Internal Server Error\r\n";
std::string badRequest = "HTTP/1.1 400 Bad Request";

/*
    List of the request handlers:
    1. /register
    2. /login
    3. /updateProfile
    4. /updateDoctor
    5. /getMedicalRecordsAsPatient
    6. /getMedicalRecordsAsDoctor
    7. /getMedicalRecords
    8. /getAppointments
    9. /createAppointment
    10. /acceptAppointment
    11. /deleteAppointment
    12. /getName
    13. /createRequest
    14. /acceptRequest
    15. /declineRequest
    16. /sendMessage
    17. /getMessages
    18. /endMedication
    19. /editMedication
    20. /getPatientDetails
    21. /getSpecialty

    This list was written in order to find them easier with CTRL + F.

*/

// URL decode function
// This function wasn't written by me, it was provided from Stackoverflow.
std::string urlDecode(std::string &SRC)
{
    std::string ret;
    char ch;
    int i, ii;
    for (i = 0; i < SRC.length(); i++)
    {
        if (SRC[i] == '%')
        {
            sscanf(SRC.substr(i + 1, 2).c_str(), "%x", &ii);
            ch = static_cast<char>(ii);
            ret += ch;
            i = i + 2;
        }
        else
        {
            ret += SRC[i];
        }
    }
    return (ret);
}

// Handles an incoming HTTP request
void handleRequest(tcp::socket &socket, const std::string &request, sql::mysql::MySQL_Driver *driver)
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

        size_t fullNameStart = decoded_line.find("fullName=") + 9;                              // Find the start position of the fullName and add the length of "fullName="
        size_t fullNameEnd = decoded_line.find("&");                                            // Find the position of the "&" delimiter
        std::string fullName = decoded_line.substr(fullNameStart, fullNameEnd - fullNameStart); // Extract the fullName substring

        size_t emailStart = decoded_line.find("email=") + 6;                        // Find the start position of the email and add the length of "email="
        size_t emailEnd = decoded_line.find("&", emailStart);                       // Find the position of the "&" delimiter starting from emailStart
        std::string email = decoded_line.substr(emailStart, emailEnd - emailStart); // Extract the email substring

        size_t phoneNumberStart = decoded_line.find("phoneNumber=") + 12;                                   // Find the start position of the phoneNumber and add the length of "phoneNumber="
        size_t phoneNumberEnd = decoded_line.find("&", phoneNumberStart);                                   // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string phoneNumber = decoded_line.substr(phoneNumberStart, phoneNumberEnd - phoneNumberStart); // Extract the phoneNumber substring

        size_t passwordStart = decoded_line.find("password=") + 9;                              // Find the start position of the password and add the length of "password="
        size_t passwordEnd = decoded_line.find("&", passwordStart);                             // Find the position of the "&" delimiter starting from passwordStart
        std::string password = decoded_line.substr(passwordStart, passwordEnd - passwordStart); // Extract the password substring

        size_t roleStart = decoded_line.find("role=") + 5;                      // Find the start position of the role and add the length of "role="
        size_t roleEnd = decoded_line.find("&", roleStart);                     // Find the position of the "&" delimiter starting from roleStart
        std::string role = decoded_line.substr(roleStart, roleEnd - roleStart); // Extract the role substring

        if (!fullName.empty() && !email.empty() && !phoneNumber.empty() && !password.empty() && !role.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
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
                response_header += success;
                response_body = "User registered successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Registration failed: " << e.what() << std::endl;
                response_body = "Registration failed";
            }
        }
        else
        {
            response_header += badRequest;
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

        size_t emailStart = decoded_line.find("email=") + 6;                        // Find the start position of the email and add the length of "email="
        size_t emailEnd = decoded_line.find("&");                                   // Find the position of the "&" delimiter
        std::string email = decoded_line.substr(emailStart, emailEnd - emailStart); // Extract the email substring

        size_t passwordStart = decoded_line.find("password=") + 9;
        size_t passwordEnd = decoded_line.find("&", passwordStart);
        std::string password = decoded_line.substr(passwordStart, passwordEnd - passwordStart);

        if (!email.empty() && !password.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?"));
                pstmt->setString(1, email);
                pstmt->setString(2, password);

                sql::ResultSet *res = pstmt->executeQuery();

                // Check if a matching user was found
                if (res->next())
                {
                    int userId = res->getInt("id");
                    std::string fullName = res->getString("fullName");
                    std::string email = res->getString("email");
                    std::string phoneNumber = res->getString("phonenumber");
                    std::string role = res->getString("role");

                    // Build the response body
                    std::ostringstream response_aux;
                    response_aux << "Login successful." << std::endl;
                    response_aux << "id=" << userId << std::endl;
                    response_aux << "fullName=" << fullName << std::endl;
                    response_aux << "email=" << email << std::endl;
                    response_aux << "phoneNumber=" << phoneNumber << std::endl;
                    response_aux << "role=" << role << std::endl;

                    response_header += success;
                    response_body = response_aux.str();
                }
                else
                {
                    // User not found or invalid credentials
                    response_header += unauthorized;
                    response_body = "Invalid email or password";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Login request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Login request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid login request";
        }
    }

    // ### Update patient profile ###

    else if (method == "POST" && path == "/updateProfile")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t idStart = decoded_line.find("id=") + 3;                  // Find the start position of the id and add the length of "id="
        size_t idEnd = decoded_line.find("&");                          // Find the position of the "&" delimiter
        std::string id = decoded_line.substr(idStart, idEnd - idStart); // Extract the id substring

        size_t fullNameStart = decoded_line.find("fullName=") + 9;                              // Find the start position of the fullName and add the length of "fullName="
        size_t fullNameEnd = decoded_line.find("&", fullNameStart);                             // Find the position of the "&" delimiter
        std::string fullName = decoded_line.substr(fullNameStart, fullNameEnd - fullNameStart); // Extract the fullName substring

        size_t phoneNumberStart = decoded_line.find("phoneNumber=") + 12;                                   // Find the start position of the phoneNumber and add the length of "phoneNumber="
        size_t phoneNumberEnd = decoded_line.find("&", phoneNumberStart);                                   // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string phoneNumber = decoded_line.substr(phoneNumberStart, phoneNumberEnd - phoneNumberStart); // Extract the phoneNumber substring

        size_t ageStart = decoded_line.find("age=") + 4;                    // Find the start position of the age and add the length of "age="
        size_t ageEnd = decoded_line.find("&", ageStart);                   // Find the position of the "&" delimiter starting from ageStart
        std::string age = decoded_line.substr(ageStart, ageEnd - ageStart); // Extract the age substring

        size_t weightStart = decoded_line.find("weight=") + 7;                          // Find the start position of the weight and add the length of "weight="
        size_t weightEnd = decoded_line.find("&", weightStart);                         // Find the position of the "&" delimiter starting from weightStart
        std::string weight = decoded_line.substr(weightStart, weightEnd - weightStart); // Extract the weight substring

        size_t genderStart = decoded_line.find("gender=") + 7;                          // Find the start position of the gender and add the length of "gender="
        size_t genderEnd = decoded_line.find("&", genderStart);                         // Find the position of the "&" delimiter starting from genderStart
        std::string gender = decoded_line.substr(genderStart, genderEnd - genderStart); // Extract the gender substring

        if (!id.empty() && !fullName.empty() && !phoneNumber.empty() && !age.empty() && !weight.empty() && !gender.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Update fullName and phoneNumber in the users table
                std::unique_ptr<sql::PreparedStatement> userStmt(con->prepareStatement("UPDATE users SET fullName = ?, phoneNumber = ? WHERE id = ?"));
                userStmt->setString(1, fullName);
                userStmt->setString(2, phoneNumber);
                userStmt->setString(3, id);
                int userRowsAffected = userStmt->executeUpdate();

                // Update the record in the userDetails table if it exists
                std::unique_ptr<sql::PreparedStatement> userDetailsStmt(con->prepareStatement("UPDATE userDetails SET age = ?, weight = ?, gender = ? , userId = ? WHERE userId = ?"));
                userDetailsStmt->setString(1, age);
                userDetailsStmt->setString(2, weight);
                userDetailsStmt->setString(3, gender);
                userDetailsStmt->setString(4, id);
                userDetailsStmt->setString(5, id);
                int userDetailsRowsAffected = userDetailsStmt->executeUpdate();

                if (userDetailsRowsAffected <= 0)
                {
                    // Insert the record into the userDetails table if it doesn't exist
                    std::unique_ptr<sql::PreparedStatement> insertStmt(con->prepareStatement("INSERT INTO userDetails (userId, age, weight, gender) VALUES (?, ?, ?, ?)"));
                    insertStmt->setString(1, id);
                    insertStmt->setString(2, age);
                    insertStmt->setString(3, weight);
                    insertStmt->setString(4, gender);
                    insertStmt->executeUpdate();
                }

                // Build the response body
                response_header += success;
                response_body = "Profile updated successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Update failed: " << e.what() << std::endl;
                response_body = "Update failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid update request";
        }
    }

    // ### Update doctor profile ###

    else if (method == "POST" && path == "/updateDoctor")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t idStart = decoded_line.find("id=") + 3;                  // Find the start position of the id and add the length of "id="
        size_t idEnd = decoded_line.find("&");                          // Find the position of the "&" delimiter
        std::string id = decoded_line.substr(idStart, idEnd - idStart); // Extract the id substring

        size_t fullNameStart = decoded_line.find("fullName=") + 9;                              // Find the start position of the fullName and add the length of "fullName="
        size_t fullNameEnd = decoded_line.find("&", fullNameStart);                             // Find the position of the "&" delimiter
        std::string fullName = decoded_line.substr(fullNameStart, fullNameEnd - fullNameStart); // Extract the fullName substring

        size_t phoneNumberStart = decoded_line.find("phoneNumber=") + 12;                                   // Find the start position of the phoneNumber and add the length of "phoneNumber="
        size_t phoneNumberEnd = decoded_line.find("&", phoneNumberStart);                                   // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string phoneNumber = decoded_line.substr(phoneNumberStart, phoneNumberEnd - phoneNumberStart); // Extract the phoneNumber substring

        size_t specialtyStart = decoded_line.find("specialty=") + 10;                               // Find the start position of the specialty and add the length of "specialty="
        size_t specialtyEnd = decoded_line.find("&", specialtyStart);                               // Find the position of the "&" delimiter starting from specialtyStart
        std::string specialty = decoded_line.substr(specialtyStart, specialtyEnd - specialtyStart); // Extract the specialty substring

        if (!id.empty() && !fullName.empty() && !phoneNumber.empty() && !specialty.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Update fullName and phoneNumber in the users table
                std::unique_ptr<sql::PreparedStatement> userStmt(con->prepareStatement("UPDATE users SET fullName = ?, phoneNumber = ? WHERE id = ?"));
                userStmt->setString(1, fullName);
                userStmt->setString(2, phoneNumber);
                userStmt->setString(3, id);
                int userRowsAffected = userStmt->executeUpdate();

                // Update the record in the doctorSpecialty table if it exists
                std::unique_ptr<sql::PreparedStatement> doctorStmt(con->prepareStatement("UPDATE doctorSpecialty SET doctorSpecialty = ? WHERE doctorID = ?"));
                doctorStmt->setString(1, specialty);
                doctorStmt->setString(2, id);
                int userDetailsRowsAffected = doctorStmt->executeUpdate();

                if (userDetailsRowsAffected <= 0)
                {
                    // Insert the record into the doctorSpecialty table if it doesn't exist
                    std::unique_ptr<sql::PreparedStatement> insertStmt(con->prepareStatement("INSERT INTO doctorSpecialty (doctorID, doctorSpecialty) VALUES (?, ?)"));
                    insertStmt->setString(1, id);
                    insertStmt->setString(2, specialty);
                    insertStmt->executeUpdate();
                }

                // Build the response body
                response_header += success;
                response_body = "Profile updated successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Update failed: " << e.what() << std::endl;
                response_body = "Update failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid update request";
        }
    }

    // ### Medical Records as patient ###

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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM medical_records WHERE patientID = ?"));
                pstmt->setString(1, id);

                sql::ResultSet *res = pstmt->executeQuery();

                if (res->next())
                {
                    do
                    {
                        int id = res->getInt("id");
                        std::string active = res->getString("active");
                        std::string accepted = res->getString("accepted");
                        std::string patientID = res->getString("patientID");
                        std::string doctorID = res->getString("doctorID");
                        std::string symptom = res->getString("symptom");
                        std::string diagnostic = res->getString("diagnostic");
                        std::string medication = res->getString("medication");
                        std::string specialty = res->getString("specialty");

                        // Build the response body
                        std::ostringstream response_aux;
                        response_aux << "id=" << id << std::endl;
                        response_aux << "active=" << active << std::endl;
                        response_aux << "accepted=" << accepted << std::endl;
                        response_aux << "patientID=" << patientID << std::endl;
                        response_aux << "doctorID=" << doctorID << std::endl;
                        response_aux << "symptom=" << symptom << std::endl;
                        response_aux << "diagnostic=" << diagnostic << std::endl;
                        response_aux << "medication=" << medication << std::endl;
                        response_aux << "specialty=" << specialty << "&" << std::endl;

                        response_header = success;
                        response_body += response_aux.str();
                    } while (res->next());
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid id";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Data request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Data request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid data request";
        }
    }

    // ### Medical Records as doctor ###

    else if (method == "POST" && path == "/getMedicalRecordsAsDoctor")
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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM medical_records WHERE doctorID = ?"));
                pstmt->setString(1, id);

                sql::ResultSet *res = pstmt->executeQuery();

                if (res->next())
                {
                    do
                    {
                        int id = res->getInt("id");
                        std::string active = res->getString("active");
                        std::string accepted = res->getString("accepted");
                        std::string patientID = res->getString("patientID");
                        std::string doctorID = res->getString("doctorID");
                        std::string symptom = res->getString("symptom");
                        std::string diagnostic = res->getString("diagnostic");
                        std::string medication = res->getString("medication");
                        std::string specialty = res->getString("specialty");

                        // Build the response body
                        std::ostringstream response_aux;
                        response_aux << "id=" << id << std::endl;
                        response_aux << "active=" << active << std::endl;
                        response_aux << "accepted=" << accepted << std::endl;
                        response_aux << "patientID=" << patientID << std::endl;
                        response_aux << "doctorID=" << doctorID << std::endl;
                        response_aux << "symptom=" << symptom << std::endl;
                        response_aux << "diagnostic=" << diagnostic << std::endl;
                        response_aux << "medication=" << medication << std::endl;
                        response_aux << "specialty=" << specialty << "&" << std::endl;

                        response_header = success;
                        response_body += response_aux.str();
                    } while (res->next());
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid id";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Data request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Data request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid data request";
        }
    }

    // ### Medical Records ###

    else if (method == "POST" && path == "/getMedicalRecords")
    {

        try
        {
            // Create a MySQL connection
            sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
            con->setSchema("medbuddy");
            // Prepare the SQL statement
            std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM medical_records"));

            sql::ResultSet *res = pstmt->executeQuery();

            if (res->next())
            {
                do
                {
                    int id = res->getInt("id");
                    std::string active = res->getString("active");
                    std::string accepted = res->getString("accepted");
                    std::string patientID = res->getString("patientID");
                    std::string doctorID = res->getString("doctorID");
                    std::string symptom = res->getString("symptom");
                    std::string diagnostic = res->getString("diagnostic");
                    std::string medication = res->getString("medication");
                    std::string specialty = res->getString("specialty");

                    // Build the response body
                    std::ostringstream response_aux;
                    response_aux << "id=" << id << std::endl;
                    response_aux << "active=" << active << std::endl;
                    response_aux << "accepted=" << accepted << std::endl;
                    response_aux << "patientID=" << patientID << std::endl;
                    response_aux << "doctorID=" << doctorID << std::endl;
                    response_aux << "symptom=" << symptom << std::endl;
                    response_aux << "diagnostic=" << diagnostic << std::endl;
                    response_aux << "medication=" << medication << std::endl;
                    response_aux << "specialty=" << specialty << "&" << std::endl;

                    response_header = success;
                    response_body += response_aux.str();
                } while (res->next());
            }

            delete res;
            delete con;
        }
        catch (std::exception &e)
        {
            std::cerr << "Data request failed: " << e.what() << std::endl;
            response_header += internalError;
            response_body = "Data request failed";
        }
    }

    // ### Appointments ###

    else if (method == "POST" && path == "/getAppointments")
    {

        try
        {
            // Create a MySQL connection
            sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
            con->setSchema("medbuddy");
            // Prepare the SQL statement
            std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM appointment"));

            sql::ResultSet *res = pstmt->executeQuery();

            if (res->next())
            {
                do
                {
                    int id = res->getInt("id");
                    std::string active = res->getString("active");
                    std::string accepted = res->getString("accepted");
                    std::string patientID = res->getString("patientID");
                    std::string doctorID = res->getString("doctorID");
                    std::string date = res->getString("date");
                    std::string location = res->getString("location");
                    std::string specialty = res->getString("specialty");

                    // Build the response body with appointment details
                    std::ostringstream response_aux;
                    response_aux << "id=" << id << std::endl;
                    response_aux << "active=" << active << std::endl;
                    response_aux << "accepted=" << accepted << std::endl;
                    response_aux << "patientID=" << patientID << std::endl;
                    response_aux << "doctorID=" << doctorID << std::endl;
                    response_aux << "date=" << date << std::endl;
                    response_aux << "location=" << location << std::endl;
                    response_aux << "specialty=" << specialty << "&" << std::endl;

                    response_header = success;
                    response_body += response_aux.str();
                } while (res->next());
            }
            else
            {
                response_header += unauthorized;
                response_body = "Invalid request";
            }

            delete res;
            delete con;
        }
        catch (std::exception &e)
        {
            std::cerr << "Appointments request failed: " << e.what() << std::endl;
            response_header += internalError;
            response_body = "Appointments request failed";
        }
    }

    // ### Create Appointment ###

    else if (method == "POST" && path == "/createAppointment")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t patientIDStart = decoded_line.find("patientID=") + 10;                               // Find the start position of the patientID and add the length of "patientID="
        size_t patientIDEnd = decoded_line.find("&", patientIDStart);                               // Find the position of the "&" delimiter starting from patientIDStart
        std::string patientID = decoded_line.substr(patientIDStart, patientIDEnd - patientIDStart); // Extract the patientID substring

        size_t specialtyStart = decoded_line.find("specialty=") + 10;                               // Find the start position of the specialty and add the length of "specialty="
        size_t specialtyEnd = decoded_line.find("&", specialtyStart);                               // Find the position of the "&" delimiter starting from specialtyStart
        std::string specialty = decoded_line.substr(specialtyStart, specialtyEnd - specialtyStart); // Extract the specialty substring

        if (!patientID.empty() && !specialty.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("INSERT INTO appointment (patientID, specialty) VALUES (?, ?)"));
                pstmt->setString(1, patientID);
                pstmt->setString(2, specialty);

                // Execute the SQL statement
                pstmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Appointment created";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Appointment creation failed: " << e.what() << std::endl;
                response_body = "Appointment creation failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid request create appointment";
        }
    }

    // ### Accept Appointment ###

    else if (method == "POST" && path == "/acceptAppointment")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t requestIDStart = decoded_line.find("requestID=") + 10;                               // Find the start position of the requestID and add the length of "requestID="
        size_t requestIDEnd = decoded_line.find("&");                                               // Find the position of the "&" delimiter
        std::string requestID = decoded_line.substr(requestIDStart, requestIDEnd - requestIDStart); // Extract the requestID

        size_t dateStart = decoded_line.find("date=") + 5;                      // Find the start position of the date and add the length of "date="
        size_t dateEnd = decoded_line.find("&", dateStart);                     // Find the position of the "&" delimiter
        std::string date = decoded_line.substr(dateStart, dateEnd - dateStart); // Extract the date substring

        size_t locationStart = decoded_line.find("location=") + 9;                              // Find the start position of the location and add the length of "location="
        size_t locationEnd = decoded_line.find("&", locationStart);                             // Find the position of the "&" delimiter starting from location
        std::string location = decoded_line.substr(locationStart, locationEnd - locationStart); // Extract the location substring

        size_t doctorIDStart = decoded_line.find("doctorID=") + 9; // Find the start position of the doctorID and add the length of "doctorID="
        std::string doctorID = decoded_line.substr(doctorIDStart); // Extract the doctorID substring

        if (!requestID.empty() && !date.empty() && !location.empty() && !doctorID.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE appointment SET location = ?, date = ?, doctorID = ?, accepted = '1' WHERE id = ?"));
                stmt->setString(1, location);
                stmt->setString(2, date);
                stmt->setString(3, doctorID);
                stmt->setString(4, requestID);
                stmt->executeQuery();

                // Build the response body
                response_header += success;
                response_body = "Accepted appointment successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Accept failed: " << e.what() << std::endl;
                response_body = "Accept request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid accept request";
        }
    }

    // ### Delete Appointment ###

    else if (method == "POST" && path == "/deleteAppointment")
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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE appointment SET active = '0' WHERE id = ?"));
                stmt->setString(1, id);

                stmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Deleted appointment successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Delete appointment failed: " << e.what() << std::endl;
                response_body = "Delete appointment failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid delete appointment request";
        }
    }

    // ### GET NAME ###

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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT fullName FROM users WHERE id = ?"));
                pstmt->setString(1, id);

                sql::ResultSet *res = pstmt->executeQuery();

                if (res->next())
                {
                    do
                    {
                        std::string fullName = res->getString("fullName");

                        response_header = success;
                        response_body = "fullName=" + fullName + "\n";
                    } while (res->next());
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid id";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Name request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Name request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid name request";
        }
    }

    // ### CREATE MEDICAL REQUEST ###

    else if (method == "POST" && path == "/createRequest")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t patientIDStart = decoded_line.find("patientID=") + 10;                               // Find the start position of the patientID and add the length of "patientID="
        size_t patientIDEnd = decoded_line.find("&", patientIDStart);                               // Find the position of the "&" delimiter starting from patientIDStart
        std::string patientID = decoded_line.substr(patientIDStart, patientIDEnd - patientIDStart); // Extract the patientID substring

        size_t symptomStart = decoded_line.find("symptom=") + 8;                            // Find the start position of the symptom and add the length of "symptom="
        size_t symptomEnd = decoded_line.find("&", symptomStart);                           // Find the position of the "&" delimiter starting from symptomStart
        std::string symptom = decoded_line.substr(symptomStart, symptomEnd - symptomStart); // Extract the symptom substring

        size_t specialtyStart = decoded_line.find("specialty=") + 10;                               // Find the start position of the specialty and add the length of "specialty="
        size_t specialtyEnd = decoded_line.find("&", specialtyStart);                               // Find the position of the "&" delimiter starting from specialtyStart
        std::string specialty = decoded_line.substr(specialtyStart, specialtyEnd - specialtyStart); // Extract the specialty substring

        if (!patientID.empty() && !symptom.empty() && !specialty.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("INSERT INTO medical_records (patientID, symptom, specialty) VALUES (?, ?, ?)"));
                pstmt->setString(1, patientID);
                pstmt->setString(2, symptom);
                pstmt->setString(3, specialty);

                // Execute the SQL statement
                pstmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Request created";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Request creation failed: " << e.what() << std::endl;
                response_body = "Request creation failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid request create request";
        }
    }

        // ### Accept request ###

    else if (method == "POST" && path == "/acceptRequest")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t requestIDStart = decoded_line.find("requestID=") + 10;                               // Find the start position of the requestID and add the length of "requestID="
        size_t requestIDEnd = decoded_line.find("&");                                               // Find the position of the "&" delimiter
        std::string requestID = decoded_line.substr(requestIDStart, requestIDEnd - requestIDStart); // Extract the requestID

        size_t diagnosticStart = decoded_line.find("diagnostic=") + 11;                                 // Find the start position of the diagnostic and add the length of "diagnostic="
        size_t diagnosticEnd = decoded_line.find("&", diagnosticStart);                                 // Find the position of the "&" delimiter
        std::string diagnostic = decoded_line.substr(diagnosticStart, diagnosticEnd - diagnosticStart); // Extract the fullName substring

        size_t medicationStart = decoded_line.find("medication=") + 11;                                 // Find the start position of the medication and add the length of "medication="
        size_t medicationEnd = decoded_line.find("&", medicationStart);                                 // Find the position of the "&" delimiter starting from phoneNumberStart
        std::string medication = decoded_line.substr(medicationStart, medicationEnd - medicationStart); // Extract the phoneNumber substring

        size_t doctorIDStart = decoded_line.find("doctorID=") + 9; // Find the start position of the doctorID and add the length of "doctorID="
        std::string doctorID = decoded_line.substr(doctorIDStart); // Extract the doctorID substring

        if (!requestID.empty() && !diagnostic.empty() && !medication.empty() && !doctorID.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE medical_records SET diagnostic = ?, medication = ?, doctorID = ?, accepted = '1' WHERE id = ?"));
                stmt->setString(1, diagnostic);
                stmt->setString(2, medication);
                stmt->setString(3, doctorID);
                stmt->setString(4, requestID);

                stmt->executeQuery();

                // Build the response body
                response_header += success;
                response_body = "Accepted request successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Accept failed: " << e.what() << std::endl;
                response_body = "Accept request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid accept request";
        }
    }

    // ### Decline request ###

    else if (method == "POST" && path == "/declineRequest")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t requestIDStart = decoded_line.find("requestID=") + 10; // Find the start position of the requestID and add the length of "requestID="
        std::string requestID = decoded_line.substr(requestIDStart);  // Extract the requestID substring

        if (!requestID.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE medical_records SET active = '0' WHERE id = ?"));
                stmt->setString(1, requestID);
                stmt->executeQuery();

                // Build the response body
                response_header += success;
                response_body = "Declined request successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Decline failed: " << e.what() << std::endl;
                response_body = "Decline request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid decline request";
        }
    }

    // ### Send Message ###

    else if (method == "POST" && path == "/sendMessage")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t roomIDStart = decoded_line.find("roomID=") + 7;                          // Find the start position of the roomID and add the length of "roomID="
        size_t roomIDEnd = decoded_line.find("&", roomIDStart);                         // Find the position of the "&" delimiter starting from roomIDStart
        std::string roomID = decoded_line.substr(roomIDStart, roomIDEnd - roomIDStart); // Extract the roomID substring

        size_t senderIDStart = decoded_line.find("senderID=") + 9;                              // Find the start position of the symptom and add the length of "senderID="
        size_t senderIDEnd = decoded_line.find("&", senderIDStart);                             // Find the position of the "&" delimiter starting from senderIDStart
        std::string senderID = decoded_line.substr(senderIDStart, senderIDEnd - senderIDStart); // Extract the senderID substring

        size_t receiverIDStart = decoded_line.find("receiverID=") + 11;                                 // Find the start position of the receiverID and add the length of "receiverID="
        size_t receiverIDEnd = decoded_line.find("&", receiverIDStart);                                 // Find the position of the "&" delimiter starting from receiverIDStart
        std::string receiverID = decoded_line.substr(receiverIDStart, receiverIDEnd - receiverIDStart); // Extract the receiverID substring

        size_t messageStart = decoded_line.find("message=") + 8;                            // Find the start position of the message and add the length of "message="
        size_t messageEnd = decoded_line.find("&", messageStart);                           // Find the position of the "&" delimiter starting from messageStart
        std::string message = decoded_line.substr(messageStart, messageEnd - messageStart); // Extract the message substring

        if (!roomID.empty() && !senderID.empty() && !receiverID.empty() && !message.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("INSERT INTO messages (roomID, senderID, receiverID, message) VALUES (?, ?, ?, ?)"));
                pstmt->setString(1, roomID);
                pstmt->setString(2, senderID);
                pstmt->setString(3, receiverID);
                pstmt->setString(4, message);

                // Execute the SQL statement
                pstmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Request created";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Send message failed: " << e.what() << std::endl;
                response_body = "Send message failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid request send message";
        }
    }

    // ### Get Messages ###

    else if (method == "POST" && path == "/getMessages")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }
        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t roomIDStart = decoded_line.find("roomID=") + 7;                          // Find the start position of the roomID and add the length of "roomID="
        size_t roomIDEnd = decoded_line.find("&", roomIDStart);                         // Find the position of the "&" delimiter starting from roomIDStart
        std::string roomID = decoded_line.substr(roomIDStart, roomIDEnd - roomIDStart); // Extract the roomID substring

        if (!roomID.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM messages WHERE roomID = ?"));
                pstmt->setString(1, roomID);

                sql::ResultSet *res = pstmt->executeQuery();

                if (res->next())
                {
                    do
                    {
                        std::string senderID = res->getString("senderID");
                        std::string receiverID = res->getString("receiverID");
                        std::string message = res->getString("message");

                        // Build the response body
                        std::ostringstream response_aux;
                        response_aux << "roomID=" << roomID << std::endl;
                        response_aux << "receiverID=" << receiverID << std::endl;
                        response_aux << "senderID=" << senderID << std::endl;
                        response_aux << "message=" << message << "&" << std::endl;

                        response_header = success;
                        response_body += response_aux.str();
                    } while (res->next());
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid roomID";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Messages request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Messages request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid messages request";
        }
    }

    // ### End medication ###

    else if (method == "POST" && path == "/endMedication")
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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE medical_records SET active = '0' WHERE id = ?"));
                stmt->setString(1, id);
                stmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Ended medication successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "End medication failed: " << e.what() << std::endl;
                response_body = "End medication failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid end medication request";
        }
    }

    // ### Edit Medication ###

    else if (method == "POST" && path == "/editMedication")
    {
        std::string line;

        while (std::getline(request_stream, line))
        {
            // Reaching the last line which contains the request body
        }

        // URL decode the request body
        std::string decoded_line = urlDecode(line);

        size_t requestIDStart = decoded_line.find("requestID=") + 10;                               // Find the start position of the requestID and add the length of "requestID="
        size_t requestIDEnd = decoded_line.find("&");                                               // Find the position of the "&" delimiter
        std::string requestID = decoded_line.substr(requestIDStart, requestIDEnd - requestIDStart); // Extract the requestID

        size_t medicationStart = decoded_line.find("medication=") + 11; // Find the start position of the medication and add the length of "medication="
        std::string medication = decoded_line.substr(medicationStart);  // Extract the phoneNumber substring

        if (!requestID.empty() && !medication.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");
                std::unique_ptr<sql::PreparedStatement> stmt(con->prepareStatement("UPDATE medical_records SET medication = ? WHERE id = ?"));
                stmt->setString(1, medication);
                stmt->setString(2, requestID);
                stmt->execute();

                // Build the response body
                response_header += success;
                response_body = "Medication changed successfully";

                delete con;
            }
            catch (std::exception &e)
            {
                response_header += internalError;
                std::cerr << "Medication change failed: " << e.what() << std::endl;
                response_body = "Medication change failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid medication change request";
        }
    }

    // ### Get Patient Details ###

    else if (method == "POST" && path == "/getPatientDetails")
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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM userDetails WHERE userId = ?"));
                pstmt->setString(1, id);

                sql::ResultSet *res = pstmt->executeQuery();

                if (res->next())
                {
                    std::string age = res->getString("age");
                    std::string weight = res->getString("weight");
                    std::string gender = res->getString("gender");

                    // Build the response body
                    std::ostringstream response_aux;
                    response_aux << "age=" << age << std::endl;
                    response_aux << "weight=" << weight << std::endl;
                    response_aux << "gender=" << gender << std::endl;

                    response_header += success;
                    response_body = response_aux.str();
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid id";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Details request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Details request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid details request";
        }
    }

    // ### Get Specialty ###

    else if (method == "POST" && path == "/getSpecialty")
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

        if (!id.empty())
        {
            try
            {
                // Create a MySQL connection
                sql::Connection *con = driver->connect("tcp://medbuddy-db:3306", "admin", "admin");
                con->setSchema("medbuddy");

                // Prepare the SQL statement
                std::unique_ptr<sql::PreparedStatement> pstmt(con->prepareStatement("SELECT * FROM doctorSpecialty WHERE doctorID = ?"));
                pstmt->setString(1, id);

                sql::ResultSet *res = pstmt->executeQuery();

                // Check if a matching user was found
                if (res->next())
                {
                    std::string doctorSpecialty = res->getString("doctorSpecialty");

                    response_header += success;
                    response_body = "doctorSpecialty=" + doctorSpecialty + "\n";
                }
                else
                {
                    response_header += unauthorized;
                    response_body = "Invalid id";
                }

                delete res;
                delete con;
            }
            catch (std::exception &e)
            {
                std::cerr << "Specialty request failed: " << e.what() << std::endl;
                response_header += internalError;
                response_body = "Specialty request failed";
            }
        }
        else
        {
            response_header += badRequest;
            response_body = "Invalid specialty request";
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
void startServer(asio::io_context &io_context, short port, sql::mysql::MySQL_Driver *driver)
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
        sql::mysql::MySQL_Driver *driver;
        driver = sql::mysql::get_mysql_driver_instance();

        // Create an ASIO io_context
        asio::io_context io_context;

        // Start the server on port 8080
        startServer(io_context, 8080, driver);
    }
    catch (std::exception &e)
    {
        std::cerr << "Server error: " << e.what() << std::endl;
    }

    return 0;
}