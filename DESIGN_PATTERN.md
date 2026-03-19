# Design pattern — microservice `reservation-service` (étape 4)

## Pattern retenu : **Builder** (créationnel, GoF)

### Problème

La **création** d’une réservation implique de nombreuses étapes : paramètres (salle, membre, créneau), validations temporelles, appels REST (disponibilité, suspension, quota) et contrôle des chevauchements en base. Sans structure dédiée, la méthode `create` du service devient un long enchaînement difficile à faire évoluer.

### Solution

Le package `com.example.reservation_service.builder` expose :

- **`ConfirmedReservationBuilderFactory`** (bean Spring) : fabrique des builders ;
- **`ConfirmedReservationBuilderFactory.Builder`** : API fluent (`roomId`, `memberId`, `startDateTime`, `endDateTime`) ;
- **`build()`** : exécute **toutes** les validations puis retourne une entité `Reservation` en statut **CONFIRMED** (non persistée).

`ReservationService.create` se contente d’enchaîner `newBuilder()…build()`, `save` et la logique Kafka (suspension), ce qui sépare **construction / validation** et **orchestration transactionnelle**.

### Intérêt

- **Lisibilité** : les paramètres et les règles de création sont regroupés dans un seul type dédié.
- **Évolutivité** : ajouter une étape de validation ou un champ obligatoire = modifier le Builder sans alourdir tout le service.
- **Alignement avec l’énoncé** : la piste _« construction d’une réservation avec ses validations multiples »_ correspond naturellement au pattern **Builder**.
