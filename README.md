Link-ul catre repository-ul de GitHub unde a fost dezvoltata aplicatia este:
https://github.com/CotulbeaAdrian/licentaUPT

Pentru a porni serverul trebuie ca sistemul sa dispuna de Docker si de Docker-Compose, apoi, din directorul server, sa se apeleze comanda: 
docker-compose up

Pentru a incarca dump-ul de date SQL, se apeleaza comanda:
mysql -h 127.0.0.1 -P 3306 -u root -p medbuddy < demo.sql

Am realizat un dump de date SQL, "demo.sql", care contine 3 conturi de doctor si 3 conturi de pacient:

1. email = patientone@example.com 
2. email = patienttwo@example.com
3. email = patientthree@example.com
4. email = doctorone@example.com 
5. email = doctortwo@example.com 
6. email = doctorthree@example.com 

Parola pentru toate conturile de mai sus este "pass123".


Aplicatia se porneste intr-un emulator Android din cadrul mediului Android Studio, iar pentru ca aceasta sa se conecteze cu succes la server trebuie sa schimbati adresa IP,
in interiorul fisierului data/api/ApiServiceBuilder cu adresa IPv4 a dispozitivului dvs si portul '8080'.
Ca si exemplu, adresa mea a fost: "http://192.168.0.115:8080"
