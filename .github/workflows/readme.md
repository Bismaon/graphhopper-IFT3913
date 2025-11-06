# Tâche 3 - Documentation

## Esteban Maries & Tai Foster-Knappe

### Choix implementation du workflow

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
      
 
Où on se déplace dans le dossier `core` pour récupérer le rapport sur les mutations puisqu'on a seulement modifié les tests de ce module. Ensuite, on prend tous les `coverage_percentage` div, on récupère leurs valeurs avec `sed` et on prend la 3e instance, qui correspond au `mutationCoverage` pour tout le module. On enlève le pourcentage pour pouvoir comparer les valeurs.

Ensuite, pour pouvoir comparer le score de mutation entre le current build et le précédent, on compare le current build coverage (CBC) avec le previous build coverage (PBC). Si on n'a pas encore de PBC, alors on l'initialise à 0. Ensuite, on sauvegarde la nouvelle valeur de PBC par celle de CBC, si le score du CBC est meilleur. Pour ce faire, on écrit le résultat dans un fichier texte et on le sauvegarde dans un artifact sur GitHub que l'on pourra extraire au prochain build.
