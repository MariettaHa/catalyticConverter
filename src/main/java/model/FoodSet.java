package model;

import java.util.*;

public class FoodSet<S> extends HashSet<Species> {

    HashSet<String> speciesIds = new HashSet<>();

    public FoodSet() {
    }

    /**
     * creates food set based on collection of species
     *
     * @param c species collection
     */
    public FoodSet(Collection<? extends Species> c) {
        for (Species cx : c) {
            addFood(cx);
        }
    }

    /**
     * add species to this set
     *
     * @param species species instance
     */
    public void addFood(Species species) {
        if (!speciesIds.contains(species.getSpeciesId())) {
            add(species);
            speciesIds.add(species.getSpeciesId());
        } else {
            System.err.println("duplicate food item with species id: " + species.getSpeciesId());
        }
    }

    /**
     * add species to this set
     *
     * @param speciesId id of species
     */
    public void addFood(String speciesId) {
        if (!speciesIds.contains(speciesId)) {
            add(new Species(speciesId));
            speciesIds.add(speciesId);
        } else {
            System.err.println("duplicate food item with species id: " + speciesId);
        }
    }

    /**
     * remove species from this set
     *
     * @param species species instance
     */
    public void removeFood(Species species) {
        remove(species);
        speciesIds.remove(species.getSpeciesId());
    }

    /**
     * remove species from this set
     *
     * @param speciesId id of species
     */
    public void removeFood(String speciesId) {
        Species s = null;

        for (Species species : this) {
            if (species.getSpeciesId().equals(speciesId)) {
                s = species;
            }
        }

        removeFood(s);
        speciesIds.remove(speciesId);
    }

    /**
     * creates sorted arraylist from this set
     *
     * @return sorted arraylist
     */
    public ArrayList<Species> getSortedSet() {
        TreeMap<String, Species> sortedIds = new TreeMap<>();

        for (Species species : this) {
            sortedIds.put(species.getSpeciesId(), species);
        }

        ArrayList<String> sortedKeysList = new ArrayList<String>(sortedIds.keySet());
        Collections.sort(sortedKeysList);

        ArrayList<Species> sortedSet = new ArrayList<>();
        for (String s : sortedIds.keySet()) {
            sortedSet.add(sortedIds.get(s));
        }

        return sortedSet;
    }
}
