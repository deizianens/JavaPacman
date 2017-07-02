package map;

 //Testa se um dado ponto do mapa colidiu algo
public interface CollisionTest {

     //Checa QUALQUER colisão que tenha ocorrido no mapa
    public boolean checkAnyCollision(int you_x, int you_y);
   
    //checa se o objeto colidiu com um objeto ESPECIFICO no mapa.
    public <T> boolean checkCollision(int you_x, int you_y, T object);

    public enum NextDirection{
        UP, RIGHT, DOWN, LEFT;

        //Retorna direção oposta
        public NextDirection opposite(){
            switch (this){
                case DOWN:
                    return UP;
                case RIGHT:
                    return LEFT;
                case LEFT:
                    return RIGHT;
                case UP:
                    return DOWN;
                default:
                    throw new IllegalStateException("Não há posição oposta a "+this+" disponível");
            }
        }
    }

    //checa colisão de um tipo especifico de objeto com o proximo evento de colisão possivel no mapa
    public <T> boolean checkNextCollision(int you_x, int you_y, T object, NextDirection next);

}