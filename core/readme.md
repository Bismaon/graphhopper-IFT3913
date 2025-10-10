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

## Pitest

### Vue d'ensemble globale

| Métrique              | Avant      | Après        | Évolution |
|-----------------------|------------|--------------|-----------|
| **Line Coverage**     | 1% (4/355) | 24% (85/355) | + 23%     |
| **Mutation Coverage** | 2% (4/264) | 21% (56/264) | + 19%     |

#### `com.graphhopper.util`

| Métrique              | Avant      | Après        | Évolution |
|-----------------------|------------|--------------|-----------|
| **Line Coverage**     | 1% (4/355) | 24% (85/355) | + 23%     |
| **Mutation Coverage** | 2% (4/264) | 21% (56/264) | + 19%     |

#### `com.graphhopper.xxx`

| Métrique              | Avant    | Après    | Évolution |
|-----------------------|----------|----------|-----------|
| **Line Coverage**     | 0% (0/0) | 0% (0/0) | + 0%      |
| **Mutation Coverage** | 0% (0/0) | 0% (0/0) | + 0%      |



---

### Mutants

## Java Faker
