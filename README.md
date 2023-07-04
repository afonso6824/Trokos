# First step

Compile Project
---
compilar projeto : mvn clean package
---
mover ficheiro .jar : mv target/trokos-jar-with-dependencies.jar ./
---

# Second Step

# Run Server

#### java -cp trokos-jar-with-dependencies.jar trokosServer.TrokosServer PORT PASSWORD_CIFRA KEYSTORE PASSWORD_KEYSTORE

---

#### Example:

java -cp trokos-jar-with-dependencies.jar trokosServer.TrokosServer 45678 passwordcifra src/main/resources/security/server/keystore.server keystore

---

# Run Client

#### java -cp trokos-jar-with-dependencies.jar trokosClient.Trokos IPADDRESS TRUSTSTORE KEYSTORE PASSWORD_KEYSTORE USER 

---

#### Example:

---
java -cp trokos-jar-with-dependencies.jar trokosClient.Trokos 127.0.0.1:45678 src/main/resources/security/client/truststore.client src/main/resources/security/client/maria/keystore_maria keystore maria

---

java -cp trokos-jar-with-dependencies.jar trokosClient.Trokos 127.0.0.1:45678 src/main/resources/security/client/truststore.client src/main/resources/security/client/manel/keystore_manel keystore manel

---

### Created by

Michael Baptista 54478
Tiago Coelho 55401
Afonso Rosa 54395
