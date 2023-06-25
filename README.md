Link-ul catre repository-ul de GitHub unde a fost dezvoltata aplicatia este:
https://github.com/CotulbeaAdrian/licentaUPT

Link-ul catre repository-ul de UPT Gitlab unde a fost incarcata ulterior aplicatia este:
https://gitlab.upt.ro/adrian.cotulbea/licentacotulbeaadrian

Pentru a porni serverul trebuie ca sistemul sa dispuna de Docker si de Docker-Compose, apoi, din directorul server, sa se apeleze comanda: 
docker-compose up

Am realizat un dump de date SQL, "demo.sql", aflat in directorul server/db/, care contine 3 conturi de doctor si 3 conturi de pacient:

1. email = patientone@example.com 
2. email = patienttwo@example.com
3. email = patientthree@example.com
4. email = doctorone@example.com 
5. email = doctortwo@example.com 
6. email = doctorthree@example.com 

Parola pentru toate conturile de mai sus este "pass123".

Pentru a incarca dump-ul de date SQL, in interiorul directorului server/db/, se apeleaza comanda:
mysql -h 127.0.0.1 -P 3306 -u root -p medbuddy < demo.sql

Parola pentru utilizatorul 'root' este 'password'. Configurata in fisierul docker-compose.


Aplicatia se compileaza prin intermediul mediului de dezvoltare Android Studio, ca apoi sa fie rulata intr-un emulator de Android oferit de acest mediu, iar pentru ca aplicatia sa se conecteze cu succes la server trebuie sa schimbati adresa IP in interiorul fisierului data/api/ApiServiceBuilder cu adresa IPv4 a dispozitivului dvs si portul '8080'.
Ca si exemplu, adresa mea a fost: "http://192.168.0.115:8080"
