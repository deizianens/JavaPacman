package map;

/**
 * Representa o mapa do jogo. 
 * Também é responsável por checar se há colisao de objetos no mapa.
 * This interface is used to represent a Map, on which the Game takes
 */
public interface Map {

    public CollisionTest getCollisionTest();
}