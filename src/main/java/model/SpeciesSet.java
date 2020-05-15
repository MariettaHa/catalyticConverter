package model;

import java.util.*;

public class SpeciesSet<S> extends HashSet<Species> {

    HashMap<String, Species> idToSpeciesMap = new HashMap<>();

    public SpeciesSet(){}

    /**
     * creates species set based on collection of species
     *
     * @param c species collection
     */
    public SpeciesSet(Collection<? extends Species> c) {
        for (Species s : c){
            this.addSpecies(s);
        }
    }

    /**
     * creates species set based on hashset of species
     *
     * @param set species set
     */
    public SpeciesSet(HashSet<Species> set) {
        this.addAll(set);
    }

    /**
     * creates species set based on arraylist of species
     *
     * @param sortedSet sorted arraylist of species
     */
    public SpeciesSet(ArrayList<Species> sortedSet) {
        for (Species s : sortedSet){
            this.addSpecies(s);
        }
    }

    /**
     * add species to this set
     *
     * @param species species instance
     */
    public void addSpecies(Species species) {
        if (!idToSpeciesMap.containsKey(species.getSpeciesId())) {
            idToSpeciesMap.put(species.getSpeciesId(), species);
            add(species);
        } else {
            System.err.println("duplicate species with species id: "+species.getSpeciesId());
        }
    }

    /**
     * add species to this set
     *
     * @param speciesId id of species
     */
    public void addSpecies(String speciesId) {
        if (!idToSpeciesMap.containsKey(speciesId)) {
            Species species = new Species(speciesId);
            idToSpeciesMap.put(speciesId, species);
            add(species);
        } else {
            System.err.println("duplicate species with species id: "+speciesId);
        }
    }

    /**
     * remove species from this set
     *
     * @param species species instance
     */
    public void removeSpecies(Species species) {
        if (contains(species)) {
            remove(species);
            idToSpeciesMap.remove(species.getSpeciesId());
        }
    }

    /**
     * remove species from this set
     *
     * @param speciesId id of species
     */
    public void removeSpecies(String speciesId) {
        if (idToSpeciesMap.containsKey(speciesId)) {
            Species species = idToSpeciesMap.get(speciesId);
            remove(species);
            idToSpeciesMap.remove(speciesId);
        }
    }

    /**
     * remove species contained in set from this set
     *
     * @param species species set
     */
    public void removeAllSpecies(SpeciesSet<Species> species){
        for (Species s : species){
            this.removeSpecies(s);
        }
    }

    /**
     * remove species contained in set from this set
     *
     * @param species species set
     */
    public void removeAllSpecies(FoodSet<Species> species){
        for (Species s : species){
            this.removeSpecies(s);
        }
    }

    /**
     * creates sorted arraylist from this set
     *
     * @return sorted arraylist
     */
    public ArrayList<Species> getSortedSet(){
        TreeMap<String, Species> sortedIds = new TreeMap<>();
        for (Species species : this){
            sortedIds.put(species.getSpeciesId(), species);
        }

        ArrayList<String> sortedKeysList = new ArrayList<String>(sortedIds.keySet());
        Collections.sort(sortedKeysList);

        ArrayList<Species> sortedSet = new ArrayList<>();
        for (String s : sortedIds.keySet()){
            sortedSet.add(sortedIds.get(s));
        }

        return sortedSet;
    }

    public HashMap<String, Species> getIdToSpeciesMap() {
        return idToSpeciesMap;
    }

}
