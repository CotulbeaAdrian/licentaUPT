# Use the official gcc image as the base
FROM gcc:latest

# Copy the server code and any necessary files to the Docker image
COPY ../server.cpp /app/

# Set the working directory
WORKDIR /app

# Install any dependencies required by your server (adjust accordingly)
RUN apt-get update && apt-get install -y libasio-dev libmysqlcppconn-dev
# Build the server
RUN g++ -o server server.cpp -lmysqlcppconn

# Expose the port on which the server will listen
EXPOSE 8080

# Define the command to run the server
CMD ["./server"]
