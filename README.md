# Examen Architecture logicielle - Coworking microservices

Stack : **Spring Boot 4**, **Spring Cloud** (Eureka, Config Server, Gateway), **Kafka**, **H2**.

## Demarrage rapide

1. `docker compose up -d` (Kafka)
2. Demarrer dans l'ordre : **discovery-server** (8761) -> **config-server** (8888) -> **member-service** (8082) -> **reservation-service** (8083) -> **room-service** (8081) -> **api-gateway** (8080)

- Eureka : `http://localhost:8761`

## Configuration

- Fichiers servis par le **Config Server** (profil `native`) : `config-server/src/main/resources/config/`
  - `room-service.yml`, `member-service.yml`, `reservation-service.yml`, `api-gateway.yml`

## API Gateway

- Port **8080**
- Routes : `/api/rooms/**` -> `room-service`, `/api/members/**` -> `member-service`, `/api/reservations/**` -> `reservation-service` (load-balanced via Eureka).

### Microservices derrière la gateway

| Service                 | Port | Rôle                                                                                                                                                                                                                                         |
| ----------------------- | ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **room-service**        | 8081 | Gestion des **salles** (CRUD), vérification de **disponibilité** d’un créneau en s’appuyant sur le _reservation-service_ (Feign). Publie des événements Kafka lors de la **suppression** d’une salle.                                        |
| **member-service**      | 8082 | Gestion des **membres** et de leur **abonnement** (BASIC / PRO / ENTERPRISE), **quotas** et **suspension** (API + événements Kafka). Consomme les événements liés aux salles / autres membres.                                               |
| **reservation-service** | 8083 | Gestion des **réservations** (création avec règles métier, annulation, complétion). Valide le membre et la salle via Feign, expose une API **interne** pour les conflits de créneaux. Consomme Kafka (suppression salle/membre, suspension). |

### Documentation API (springdoc-openapi)

Avec les services démarrés, ouvrir **Swagger UI** **directement** sur le port du microservice:

- Room : `http://localhost:8081/swagger-ui.html` (OpenAPI JSON : `http://localhost:8081/v3/api-docs`)
- Member : `http://localhost:8082/swagger-ui.html`
- Reservation : `http://localhost:8083/swagger-ui.html`

## Tests Postman

---

## Collection Postman

1. Postman : **Import** > **Upload Files** > selectionner :
   - `postman/Examen-Coworking.postman_collection.json`

**Dossier A - Via API Gateway** : tout passe par `http://localhost:8080` (routes `/api/rooms/**`, `/api/members/**`, `/api/reservations/**`).

**Dossier B - Acces direct** : tests de secours sur 8081 / 8082 / 8083.

---

## Scenarios de test

1. Creer plusieurs salles (POST dans le dossier A).
2. Creer membres BASIC / PRO / ENTERPRISE.
3. Creer une reservation puis **GET Creneau disponible** : doit repondre `false` si le creneau est pris.
4. **POST Echec chevauchement** : doit echouer (400).
5. **Quota BASIC** : deux reservations sur **deux salles** au meme creneau ; puis **GET Membre** : `suspended` = true apres Kafka.
6. **POST Annuler reservation** ; re-GET membre : `suspended` = false.
7. **DELETE Supprimer salle** : reservations CONFIRMED de cette salle passees en CANCELLED (Kafka).
8. **DELETE Supprimer membre** : reservations du membre supprimees (Kafka).

Attendre 1-2 s apres les actions si Kafka a un leger delai.

## Pattern de conception

Voir **DESIGN_PATTERN.md** (Builder dans `reservation-service`).

## Enonce

Voir **examen-architecture-microservices-coworking.md**
