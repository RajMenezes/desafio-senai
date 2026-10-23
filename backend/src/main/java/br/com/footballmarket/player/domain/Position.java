package br.com.footballmarket.player.domain;

public enum Position {

    //  pace, shooting, passing, dribbling, defending, physical
    GOL(10, 0, 20, 0, 40, 30),
    ZAG(10, 0, 15, 0, 45, 30),
    LAT(25, 0, 20, 15, 25, 15),
    MEI(10, 15, 35, 25, 5, 10),
    ATA(20, 40, 10, 20, 0, 10);

    private final int paceWeight;
    private final int shootingWeight;
    private final int passingWeight;
    private final int dribblingWeight;
    private final int defendingWeight;
    private final int physicalWeight;

    Position(int paceWeight, int shootingWeight, int passingWeight,
             int dribblingWeight, int defendingWeight, int physicalWeight) {
        this.paceWeight = paceWeight;
        this.shootingWeight = shootingWeight;
        this.passingWeight = passingWeight;
        this.dribblingWeight = dribblingWeight;
        this.defendingWeight = defendingWeight;
        this.physicalWeight = physicalWeight;
    }


    public int weightedSum(Attributes attributes) {
        return attributes.pace() * paceWeight
                + attributes.shooting() * shootingWeight
                + attributes.passing() * passingWeight
                + attributes.dribbling() * dribblingWeight
                + attributes.defending() * defendingWeight
                + attributes.physical() * physicalWeight;
    }
}
