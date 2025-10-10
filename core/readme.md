# Tâche 2 - Documentation

## Esteban Maries & Tai Foster-Knappe

## Choix des classes

Nous avons choisi le module `Core`, puisqu'il semblait etre le module contenant le plus intéressant, ainsi que par extension centrale au projet. En regardant le rapport `JaCoCo`, on a pu voir `com.graphhopper.util` qui avait une assez grande partie de couverture manquante. On a donc cherché et trouvé la classe `com.graphhopper.util.GHUtility` qui contenant moins de 50% de couverture et avons donc choisi d'ajouter des tests pour cette classe.

## Documentation détaillée des tests

### Classe : GHUtility $\rightarrow$ GHUtilityTest

#### Utilitaires

- `createGraph(...)` est une fonction réutilisable pour initialiser les graphes utilise dans les tests.
- `testPaths(...)` est une fonction réutilisable pour tester `pathsEqualExceptOneEdge` avec differents paramètres de graphes.

#### Test 1 : `testGetAdjnode()`

**Intention du test**

Vérifier que la méthode `getAdjNode()` fonctionne correctement et lance une `Exception` de manière approprié.

**Motivation des données de test choisies**

- Un `Edge` d'un `Graph`, afin de vérifier que la méthode détecte bien les deux `Node` de ce même `Edge`.
- Un `Edge` ne contenant pas le `Node` demandé afin de verifier que la méthode ne detecte pas de `Node` adjacent n'existant pas.

**Explication de l'Oracle**

La methode `getAdjNode()` parcours les `Edge`, de `Node` en `Node` afin de renvoyer le `Node` étant connecté au `adjNode` par le `Edge`. Si un `Edge` n'est pas valide alors, on renvoie simplement le `adjNode` donné.

- Lorsque le `adjNode` est sur le `Edge` donné alors la méthode renvoie le node connecté.
- Lorsque le `adjNode` n'est pas sur le `Edge` donné alors la méthode renvoie un `NullPointerException` car il ne peut pas trouver de `Node` valide.

---

#### Test 2 : `testPathsEqualExceptOneEdge()`

**Intention du test**

Vérifier que la méthode `pathsEqualExceptOneEdge()` compare correctement deux `Path` et verifie qu'ils font bien la même distance sinon qu'elle puisse noter les bonnes violations entre les deux `Path`.

**Motivation des données de test choisies**

- Deux `Path` qui sont identiques (temps, distance, poids) sauf pour une arête, pour tester la methode à distinguer cette différence.
- Les `Path` sont créés via `testPaths()` et `testPathsDifferentGraphs` .
-
**Explication de l'Oracle**

La methode `pathsEqualExceptOneEdge()` compare deux `Path` et génère la liste de violations dû aux différences entre les `Path`.

- Lorsque les `Path` sont identiques ou tres similaire (différence plus petite que les balises mise dans le code) alors la liste de violations est vide.
- Lorsque les `Path` sont différents alors la liste de violations est peuplé d'entrées correspondantes aux différences entre les deux `Path`.

---

#### Test 3 : `testGetCommonNodes()`

**Intention du test**

Vérifier que la méthode `getCommonNodes()` identifie correctement le `Node` commun entre deux `Edge` dans un `Graph`. Déclenche une exception lorsque les `Edge` ne partagent aucun `Node`, ou lorsqu'un `Edge` est compare à lui-meme.

**Motivation des données de test choisies**

- Les `Edge` sont créé apres la création du graphe, afin de former la forme suivante :
- 1 - 0
- |   /
- 2
- |
- 3
- Deux `Edge` qui partagent un `Node` commun pour vérifier que la méthode retourne bien ce `Node` (e.g. 1 et 0).
- Deux `Edge` qui ne partagent pas un `Node` commun pour vérifier que la méthode retourne `IllegalArgumentException`.
- Deux `Edge` qui sont pareils pour vérifier que la méthode retourne `IllegalArgumentException`.


**Explication de l'Oracle**

Le code de `getCommonNodes(...)` analyse deux `Edge` et détermine s'ils partagent un `Node` commun en comparant leurs `Node` de `source` et de `end`.

- Lorsque deux `Edge` ont un `Node` en commun, la méthode doit retourner l'identifiant de ce `Node`.
- Lorsque deux `Edge` ont aucun `Node` en commun ou forment une boucle, la méthode doit retourner une `IllegalArgumentException` pour signaler l'incohérence.

---

#### Test 4 : `testGetProblems()`

**Intention du test**

Vérifier que la méthode `getProblems()` ne signale aucun problème lorsque tous les `Node` ont des coordonnées valides (latitude et longitude). Ainsi qu'elle détecte correctement les erreurs lorsque certaines coordonnées sortent des plages valides.

**Motivation des données de test choisies**

- Les coordonnées respectent les plages valides et donc la liste de problème renvoyé devraient etre vide.
- Les coordonnées ne respectent pas les plages valides pour vérifier que `getProblems()` détecte bien ces erreurs.

**Explication de l'Oracle**

Le code de `getProblems(...)` doit détecter les différentes violations de plage valide pour les `Node` dans un `Graph`.

- Les coordonnées sont correctes et donc la liste de probleme est vide.
- Les coordonnées sont incorrectes et donc la liste de probleme contiennent les problèmes du `Graph` dans l'ordre dans des `Node`.

#### Test 5 : `testGetDistance()`

**Intention du test**

Vérifier que la méthode `getDistance()` retourne une distance pertinente entre 2 coordonnées valides et 0 (ou presque avec les erreurs avec les points flottants) si essaie la distance entre un point et lui-même.
Egalement vérifier que la méthode n'essaie pas de normaliser des coordonnées invalides.

**Motivation des données de test choisies**

- Les 2 premiers coordonnées sont valides et ont une distance entre eux d'environ 300-350km
- La 3eme paire de coordonnées est invalide et la distance entre lui et un autre point valide ne sera pas cohérent

**Explication de l'Oracle**

Le code de `getDistance(...)` doit retourner une distance cohérente et plus ou moin précis entre 2 points

- Les coordonnées sont valides et la distance est cohérente.
- Les coordonnées ne sont pas valides et la distance n'est pas cohérente


#### Test 6 : `testGetEdge()`

**Intention du test**


Vérifier que la méthode `getEdge()` ne retourne rien lorsqu'un edge n'existe pas, retourn quelque chose lorsqu'il y a exactement 1 et un IllegalArgumentException lorsque le graphe est invalide et contient 2 edges pour 2 mêmes nodes. 

**Motivation des données de test choisies**

- Au début le graph est parfaitment valide et lors des 2 premiers appels devrait fonctionner correctement
- Un deuxième edge est ajouté entre 0 et 1 pour vérifier que la méthode refuse bien la validité du graphe

**Explication de l'Oracle**

Le code de `getEdge(...)` doit retourner soit null, un edge ou une exception lorsqu'un graphe et 2 nodes sont donnés. 

- Le graphe et valide retourne soit null ou exactement 1 edge
- Le graphe n'est pas valide et retourne une exception

#### Test 7 : `testSetSpeed()`

**Intention du test**

Vérifier que la méthode `setSpeed()` configure correctement la vitesse et l'accès sur un edge dans les deux directions et que des vitesse invalides throw une exception.

**Motivation des données de test choisies**

- Un edge est créé entre les nodes 0 et 1 pour tester la modification de ses attributs.
- La vitesse avant et arrière sont testées pour vérifier que setSpeed gère correctement les directions.
- La vitesse nulle est testée pour s'assurer qu'une erreur IllegalStateException est thrown lorsqu'on tente de définir un edge accessible avec une 0 vitesse.

**Explication de l'Oracle**

Le code de `getEdge(...)` doit correctement gérer les vitesses et accès dans les 2 directions

- Lorsqu'on va en avant ou en arrière à une vitesse valide, l'edge doit être accessible dans le sens approprié et stocker la vitesse correspondante.
- La vitesse est 0 et donc le mouvement est invalide et throw une exception

## Pitest

### Vue d'ensemble globale

#### `com.graphhopper.util.GHUtility.java`

| Métrique              | Avant      | Après         | Évolution |
|-----------------------|------------|---------------|-----------|
| **Line Coverage**     | 1% (4/355) | 35% (123/355) | + 34%     |
| **Mutation Coverage** | 2% (4/264) | 30% (78/264)  | + 28%     |


---

### Mutants

## Java Faker

Pour choisir une bonne méthode pour utiliser Java Faker il a fallu trouver une méthode qui peut accepter des valeurs aléatoires comme des noms, addresses ou coordonnées.
On s'est donc décidé sur createCircle puisqu'elle est simple mais aussi importante et bien compatible avec Java Faker.
On lui donne un Id et aussi des coordonnées aléatoires puis on vérifie que le cercle est valide en regardant si il contient son centre supposé au bon endroit.
