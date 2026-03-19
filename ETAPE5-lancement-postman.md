# Etape 5 - Lancement, Config Server, Gateway, Kafka, Postman

## 1. Kafka (Docker)

A la racine du depot :

```bash
docker compose up -d
```
---

## 2. Ordre de demarrage des services Spring

| Ordre | Service            | Port | Commande (depuis la racine du repo) |
|------:|--------------------|------|-------------------------------------|
| 1     | **discovery-server** | 8761 | `cd discovery-server && ./mvnw spring-boot:run` |
| 2     | **config-server**    | 8888 | `cd config-server && ./mvnw spring-boot:run` |
| 3     | **member-service**   | 8082 | `cd member-service && ./mvnw spring-boot:run` |
| 4     | **reservation-service** | 8083 | `cd reservation-service && ./mvnw spring-boot:run` |
| 5     | **room-service**     | 8081 | `cd room-service && ./mvnw spring-boot:run` |
| 6     | **api-gateway**      | 8080 | `cd api-gateway && ./mvnw spring-boot:run` |

**Verifications utiles**

- Eureka : `http://localhost:8761`

---

## 3. Collection Postman (import sans erreur)

1. Postman : **Import** > **Upload Files** > selectionner :
   - `postman/Examen-Coworking.postman_collection.json`
2. Onglet **Variables** de la collection :
   - `gateway_host` = `localhost`
   - `gateway_port` = `8080`
   - `room_id`, `room_id_2`, `member_id`, `reservation_id` : mettre a jour apres les POST (souvent `1`, `2`, ... sur H2 vide).

**Dossier A - Via API Gateway** : tout passe par `http://localhost:8080` (routes `/api/rooms/**`, `/api/members/**`, `/api/reservations/**`).

**Dossier B - Acces direct** : tests de secours sur 8081 / 8082 / 8083.

---

## 4. Scenarios de test (rappel enonce)

1. Creer plusieurs salles (POST dans le dossier A).
2. Creer membres BASIC / PRO / ENTERPRISE.
3. Creer une reservation puis **GET Creneau disponible** : doit repondre `false` si le creneau est pris.
4. **POST Echec chevauchement** : doit echouer (400).
5. **Quota BASIC** : deux reservations sur **deux salles** au meme creneau ; puis **GET Membre** : `suspended` = true apres Kafka.
6. **POST Annuler reservation** ; re-GET membre : `suspended` = false.
7. **DELETE Supprimer salle** : reservations CONFIRMED de cette salle passees en CANCELLED (Kafka).
8. **DELETE Supprimer membre** : reservations du membre supprimees (Kafka).

Attendre 1-2 s apres les actions si Kafka a un leger delai.

---