package map;

//evento disparado pelo GameLoop para checar qualquer colisão com outro objeto no mapa
//checa apenas colisões com objetos, não com players ou figuras
public interface CollisionEvent {

    public void detectCollision(CollisionTest tester);
}