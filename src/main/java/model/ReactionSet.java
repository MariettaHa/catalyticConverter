package model;

import java.util.*;

public class ReactionSet<R> extends HashSet<Reaction> {

    HashMap<String, Reaction> idToReactionMap = new HashMap<>();

    public ReactionSet() {
    }

    /**
     * creates reaction set based on collection of reactions
     *
     * @param c species collection
     */
    public ReactionSet(Collection<? extends Reaction> c) {
        super(c);
    }

    /**
     * add reaction to this set
     *
     * @param reaction reaction instance
     */
    public void addReaction(Reaction reaction) {
        idToReactionMap.put(reaction.getReactionId(), reaction);
        add(reaction);
    }

    /**
     * add reaction to this set
     *
     * @param reactionId id of reaction
     */
    public void addReaction(String reactionId) {
        Reaction reaction = new Reaction(reactionId);

        idToReactionMap.put(reactionId, reaction);
        add(reaction);
    }

    /**
     * add reaction from this set
     *
     * @param reaction reaction instance
     */
    public void removeReaction(Reaction reaction) {
        if (contains(reaction)) {
            remove(reaction);
            idToReactionMap.remove(reaction);
        }
    }

    /**
     * add reaction from this set
     *
     * @param reactionId id of reaction
     */
    public void removeReaction(String reactionId) {
        if (idToReactionMap.containsKey(reactionId)) {
            Reaction reaction = idToReactionMap.get(reactionId);
            remove(reaction);
            idToReactionMap.remove(reaction);
        }
    }

    /**
     * creates sorted arraylist from this set
     *
     * @return sorted arraylist
     */
    public ArrayList<Reaction> getSortedList() {
        TreeMap<String, Reaction> sortedIds = new TreeMap<>();

        for (Reaction reaction : this) {
            sortedIds.put(reaction.getReactionId(), reaction);
        }

        ArrayList<String> sortedKeysList = new ArrayList<String>(sortedIds.keySet());
        Collections.sort(sortedKeysList);

        ArrayList<Reaction> sortedSet = new ArrayList<>();
        for (String s : sortedKeysList) {
            sortedSet.add(sortedIds.get(s));
        }

        return sortedSet;
    }

    public HashMap<String, Reaction> getIdToReactionMap() {
        return idToReactionMap;
    }

}
