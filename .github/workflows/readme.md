# Tâche 3 - Documentation

### Esteban Maries & Tai Foster-Knappe

## Choix implementation du workflow

Au début du travail, une erreur revenait souvent, faisant que le `mutationCoverage` ne fonctionnait pas correctement, et produisait l'erreur suivante:

```
org.pitest:pitest-maven:1.20.6:mutationCoverage failed: No mutations found. This probably means there is an issue with either the supplied classpath or filters.
```

Durant la résolution de ce problème, on a eu plusieurs idées sans succès, mais on a fini par trouver la solution `-DfailWhenNoMutations=false` qui permet de modifier le comportement de base de `pitest` qui retourne une erreur s'il n'y a pas de mutations, mais qui, avec ce false flag, ne traitera pas ça comme un fail et continuera le flow.  
Pour récupérer le score, on utilise la commande suivante :
```
SCORE=$(grep -o '<div class="coverage_percentage">[^<]*' core/target/pit-reports/index.html \
      | sed -E 's/.*>([^<]*)/\1/' \
      | awk 'NR==2' \
      | tr -d '%')
```
Où on se déplace dans le dossier `core` pour récupérer le rapport sur les mutations, puisqu'on a seulement modifié les tests de ce module. Ensuite, on prend tous les `coverage_percentage` div, on récupère leurs valeurs avec `sed` et on prend la 3e instance, qui correspond au `mutationCoverage` pour tout le module. On enlève le pourcentage pour pouvoir comparer les valeurs.

Ensuite, pour pouvoir comparer le score de mutation entre le current build et le précédent, on compare le current build coverage (CBC) avec le previous build coverage (PBC). On cherche le score sauvegarder durant le dernier build, et on le download. Si on n'a pas encore de PBC, alors on l'initialise à 0. Ensuite, on sauvegarde la nouvelle valeur de PBC par celle de CBC, si le score du CBC est meilleur. Pour ce faire, on écrit le résultat dans un fichier texte et on le sauvegarde dans un artifact sur GitHub que l'on pourra extraire au prochain build.


## Documentation détaillée des tests

### Classe : GHUtility $\rightarrow$ GHUtilityTest

#### Utilitaires
- `Mockgraphitervalue(...)` est une fonction réutilisable pour initialiser un mock graph de `numNodes` nodes et un `iterValue` pour `iter.getAdjNode` qui dictera le comportement souhaité.

## Classes simulées et motivation
- `Graph`, nécessaire pour pouvoir contrôler les itérateurs et explorateur du graph présent dans `getProblems`.
- `NodeAccess`, simule des nœuds valides pour isoler la logique de vérification des arêtes.
- `EdgeExplorer`, nécessaire pour modifier le comportement du `while(iter.next())`.
- `EdgeIterator`, permet de simuler des arêtes invalides.

#### Test 1 : `testGetProblems_withInvalidAdjNodes()`

**Intention du test**

Vérifier que `getProblems()` détecte correctement les arêtes invalides dont le nœud adjacent (qui ne sont pas accessibles sans mock) :
- est supérieur ou égal au nombre total de nœuds `>= nodes`
- ou est négatif `< 0`

**Mocks utilisés**
- `Graph`, retourne 3 nœuds et un `EdgeExplorer` simulé.
- `NodeAccess`, renvoie des lat/lon valides pour ne pas influencer le résultat.
- `EdgeExplorer`, renvoie un `EdgeIterator` mocké.
- `EdgeIterator`, renvoie successivement `true` puis `false` pour `next()` afin de simuler une unique arête, et renvoie une valeur d’`adjNode` invalide.

**Valeurs choisies**
- `adjNode = 3` $\rightarrow$ déclenche `"greater or equal to getNodes"`.
- `adjNode = -1` $\rightarrow$ déclenche `"has a negative node"`.

**Oracle**
- La liste retournée n’est pas vide `assertFalse(problems.isEmpty())`.
- Le premier message contient la chaîne correspondant à l’erreur attendue :
  - `"greater or equal to getNodes"` pour le premier cas.
  - `"has a negative node"` pour le second cas.
---

#### Test 2 : `testGetProblems_withInvalidAdjNodes()`

**Intention du test**

Vérifier que `getProblems()` déclenche la bonne erreur face à une erreur interne du graphe.

**Mocks utilises**
- `Graph`, mocké pour lancer une exception lorsqu’on appelle `createEdgeExplorer()`.
- `NodeAccess`, renvoyé par `getNodeAccess()` pour éviter `NullPointerException`.

**Valeurs choisies**
- `getNodes() = 3`
- `createEdgeExplorer()` $\rightarrow$ `throw new IllegalStateException("Uh oh... seems like the graph broke down.")`.

**Oracle**
- Une `RuntimeException` est levée `assertThrows(RuntimeException.class)`.
- Le message contient la chaîne `"problem with node"`, confirmant que le bloc `catch` a bien été exécuté.
---

