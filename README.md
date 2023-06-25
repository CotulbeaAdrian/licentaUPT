--- RUN SERVER ---
docker-compose up

--- LOAD DATA DUMP ---
mysql -h 127.0.0.1 -P 3306 -u root -p medbuddy < demo.sql

--- SQL DATA DUMP ---
mysqldump --user=root --password=password --databases medbuddy > demo.sql


Am realizat un dump de date SQL, "demo.sql", care contine 3 conturi de doctor si 3 conturi de pacient:

1. email = patientone@example.com 
2. email = patienttwo@example.com
3. email = patientthree@example.com
4. email = doctorone@example.com 
5. email = doctortwo@example.com 
6. email = doctorthree@example.com 

The password for all the accounts above is "pass123".


Change the IP the server is running in api/ApiServiceBuilder to the IPv4 IP of your connexion.
As an example, mine was: "http://192.168.0.115:8080"
