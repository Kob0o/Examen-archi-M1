# Examen Architecture logicielle - Coworking microservices

Stack : **Spring Boot 4**, **Spring Cloud** (Eureka, Config Server, Gateway), **Kafka**, **H2**.

## Demarrage rapide

1. `docker compose up -d` (Kafka)
2. Demarrer dans l'ordre : **discovery-server** (8761) -> **config-server** (8888) -> **member-service** (8082) -> **reservation-service** (8083) -> **room-service** (8081) -> **api-gateway** (8080)

Details, Postman et scenarios : voir [**ETAPE5-lancement-postman.md**](ETAPE5-lancement-postman.md).

## Configuration centralisee

- Fichiers servis par le **Config Server** (profil `native`) : `config-server/src/main/resources/config/`
  - `room-service.yml`, `member-service.yml`, `reservation-service.yml`, `api-gateway.yml`

## API Gateway

- Port **8080**
- Routes : `/api/rooms/**` -> `room-service`, `/api/members/**` -> `member-service`, `/api/reservations/**` -> `reservation-service` (load-balanced via Eureka).

## Tests Postman

Importer : `postman/Examen-Coworking.postman_collection.json`

## Pattern de conception

Voir **DESIGN_PATTERN.md** (Builder dans `reservation-service`).

## Enonce complet

Voir **examen-architecture-microservices-coworking.md**
