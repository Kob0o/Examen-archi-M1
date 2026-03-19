# Design pattern — microservice `reservation-service`

## Pattern : **Builder** (créationnel)

Référence utile : [Builder sur Refactoring.Guru](https://refactoring.guru/design-patterns/builder) — l’idée centrale, c’est de **construire un objet complexe par étapes**, plutôt que de tout empiler dans un constructeur à dix paramètres ou de disperser la logique dans le code appelant. Le site parle aussi d’éviter le _telescoping constructor_ : dès qu’il y a beaucoup de champs, validations et branches, un seul gros constructeur devient vite illisible et fragile.

## Pourquoi ce pattern ici ?

Une réservation « prête à être enregistrée » n’est pas qu’un simple enregistrement en base : il faut un créneau cohérent, vérifier la salle (existence, dispo, pas de conflit via le room-service), le membre (pas suspendu, quota respecté), puis encore un contrôle local des chevauchements. C’est une **séquence d’étapes** qui doit aboutir à un `Reservation` **CONFIRMED** ou lever une erreur claire — typiquement le genre de scénario où le Builder aide à **isoler toute la construction / validation** dans un endroit, au lieu de laisser `ReservationService.create` grossir sans fin.

On n’a pas besoin d’un _Director_ au sens livre : l’ordre des étapes est fixé dans `build()`, et c’est suffisant pour notre cas (une seule « recette » de réservation confirmée).

## Ce qui est en place dans le code

Le package `com.example.reservation_service.builder` contient :

- **`ConfirmedReservationBuilderFactory`** : bean Spring qui sait instancier un builder avec les dépendances (clients Feign, dépôt).
- **`Builder` interne** : API fluent (`roomId`, `memberId`, `startDateTime`, `endDateTime`) puis **`build()`**, qui enchaîne validations + appels distants + règles métier et retourne une entité **non persistée**.

`ReservationService` garde l’orchestration « métier » (appel au builder, `save`, événements Kafka pour la suspension, etc.) sans réimplémenter les checks une par une. Si demain on ajoute une règle ou un champ obligatoire, la modification reste localisée dans le builder — ce qui colle aussi à l’argument _single responsibility_ souvent associé au pattern sur Refactoring.Guru.

## Lien avec l’énoncé

La consigne évoquait explicitement la construction d’une réservation avec **plusieurs validations** : le Builder est un choix naturel pour modéliser ça proprement, sans surcharger le service ni multiplier les constructeurs de `Reservation`.
