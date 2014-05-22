package br.ufba.ia.copsandrobbers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import br.ufba.ia.copsandrobbers.search.AStar;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

public class PoliciaLadraoGame implements ApplicationListener {

	private AStar aStar = new AStar();

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;

	private TextureAtlas atlas;
	private TextureRegion paredeImage, mapImage;
	private TextureRegion ladraoRegion, policiaRegion;

	private int ladraoAtivado = 1;
	private int xLoc[] = new int[4], yLoc[] = new int[4], speed[] = new int[4];
	private long tempoBusca; 


	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w, h);
		camera.position.set(w/2, h/2, 0);
		camera.update();

		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);

		carregarMapa();
		carregarUnidades();
		carregarGraficos();
		aStar.inicializarBuscadorDeCaminho();
	}

	@Override
	public void dispose() {
		batch.dispose();
		atlas.dispose();
		salvarMapa();

	}

	@Override
	public void render() {		


		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) Gdx.app.exit();
		verificarInputs();
		if (ladraoAtivado == 1) moveLadrao();
		if (ladraoAtivado == 1)  
			for (int ID = 2; ID <= 3; ID++) 
				movePolicia(ID);


		RenderScreen(); 

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}



	private void verificarInputs (){
		if (Gdx.input.isKeyPressed(Keys.ENTER))
		{
			ladraoAtivado = 1 - ladraoAtivado;
			if (ladraoAtivado == 1) criarMapaImagem();
		}
//		if (ladraoAtivado == 0) editarMapa();
	}

	void criarMapaImagem(){
		Pixmap pixmap = new Pixmap(1024, 1024, Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.GRAY);
		for (int x = 0; x <= 79; x++){
			for (int y = 0; y <= 59; y++){
				if (aStar.possibilidadeDeCaminhada[x][y] == aStar.caminhoBloqueado) {
					pixmap.fillRectangle(x*AStar.tamanhoPixel,y*AStar.tamanhoPixel,AStar.tamanhoPixel,AStar.tamanhoPixel);
				}
			}}		
		Texture map = new Texture(pixmap);
		mapImage = new TextureRegion(map, 800, 600);
		mapImage.flip(false, true);
	}

	void criarParedeImagem(){
		Pixmap pixmap = new Pixmap(16, 16, Format.RGB888);
		pixmap.setColor(Color.BLUE);
		pixmap.drawRectangle(0,0,10,10);
		Texture map = new Texture(pixmap);
		paredeImage = new TextureRegion(map, 10,10);
	}

	void DrawMap(SpriteBatch batch){
		if (ladraoAtivado == 1) 
			batch.draw(mapImage,0,0);
		else
		{
			for (int x = 0; x <= 79; x++){
				for (int y = 0; y <= 59; y++){

				}}
		}
	}

	void editarMapa(){
		if (Gdx.input.isTouched())
		{
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);

			int x = ((int)touchPos.x)/AStar.tamanhoPixel;
			int y = ((int)touchPos.y)/AStar.tamanhoPixel;
			aStar.possibilidadeDeCaminhada[x][y] = (char)(1-aStar.possibilidadeDeCaminhada[x][y]);
		}
	}

	void carregarGraficos(){
		atlas = new TextureAtlas("data/textures.pack");
		criarParedeImagem();
		criarMapaImagem();

		ladraoRegion = atlas.findRegion("smiley x 10");

		policiaRegion = atlas.findRegion("ghost x 10");
	}

	void carregarMapa(){

		FileReader reader;
		try {
			reader = new FileReader(Gdx.files.internal("bin/data/myTerrainData.dat").file());
			if (reader.ready())
			{
				for (int x = 0; x <= 79; x++){
					for (int y = 0; y <= 59; y++){
						aStar.possibilidadeDeCaminhada [x][y] = (char)reader.read();
						if (aStar.possibilidadeDeCaminhada [x][y] > 1) aStar.possibilidadeDeCaminhada [x][y] = 0;
					}}
				reader.close();
			}
			else 
			{
				for (int x = 0; x <= 79; x++){
					for (int y = 0; y <= 59; y++){
						aStar.possibilidadeDeCaminhada [x][y] = (char)aStar.caminhoPassavel;
					}}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	void carregarUnidades(){
		xLoc[1] = 80 ; yLoc[1] = 160; //posição inicial do ladrão
		xLoc[2] = 320 ; yLoc[2] = 320; //posição inicial do policial
		xLoc[3] = 160 ; yLoc[3] = 320; //posição inicial do policial
		speed[1] = 3;//velocidade do ladrão
		speed[2] = 2;//velocidade do policial
		speed[3] = 3;//velocidade do policial
	}

	void movePolicia(int ID){
		int targetID = 1; 

		//1. Procurar caminho: Se os agentes não estão na mesma posição procure um caminho
		if (xLoc[ID] != xLoc[targetID] || yLoc[ID] != yLoc[targetID]) 
		{
			//Se nenhum caminho foi gerado, gere um. Atualize-o quando o policial 1 alcançar 10 passos.
			if (aStar.statusDoCaminho[ID] == AStar.naoComecou|| aStar.localizacaoDoCaminho[ID] == 10)
			{
				if(ID == 2){
					aStar.statusDoCaminho[ID] = aStar.encontrarCaminho(ID,xLoc[ID],yLoc[ID],
							xLoc[targetID],yLoc[targetID]);
				}
			}
			//Se nenhum caminho foi gerado, gere um. Atualize-o quando o policial 2 alcançar 100 passos.
			if (aStar.statusDoCaminho[ID] == AStar.naoComecou|| aStar.localizacaoDoCaminho[ID] == 100){
				if(ID == 3){
					aStar.statusDoCaminho[ID] = aStar.encontrarCaminhoBuscaCega(ID,xLoc[ID],yLoc[ID],
							xLoc[targetID],yLoc[targetID]);
				}
			}
		} 

		if (aStar.statusDoCaminho[ID] == AStar.encontrado) MoveSprite(ID);
	}

	void moveLadrao(){
		int ID = 1; //ID do ladrão

		if (Gdx.input.isButtonPressed(Buttons.LEFT) || Gdx.input.isButtonPressed(Buttons.RIGHT))
		{	
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);

			long time1 = TimeUtils.millis();
			aStar.statusDoCaminho[ID] = aStar.encontrarCaminho(ID,xLoc[ID],yLoc[ID],(int)touchPos.x,(int)touchPos.y);
			long time2 = TimeUtils.millis();
			tempoBusca = time2-time1;
		}

		if (aStar.statusDoCaminho[ID] == AStar.encontrado) MoveSprite(ID);
	}

	void MoveSprite(int ID){
		aStar.leituraDoCaminho(ID,xLoc[ID],yLoc[ID],speed[ID]);

		if (xLoc[ID] > aStar.caminhoX[ID]) xLoc[ID] = xLoc[ID] - speed[ID];
		if (xLoc[ID] < aStar.caminhoX[ID]) xLoc[ID] = xLoc[ID] + speed[ID];
		if (yLoc[ID] > aStar.caminhoY[ID]) yLoc[ID] = yLoc[ID] - speed[ID];		
		if (yLoc[ID] < aStar.caminhoY[ID]) yLoc[ID] = yLoc[ID] + speed[ID];

		if (aStar.localizacaoDoCaminho[ID] == aStar.tamanhoDoCaminho[ID]) 
		{
			if (Math.abs(xLoc[ID] - aStar.caminhoX[ID]) < speed[ID]) xLoc[ID] = aStar.caminhoX[ID];
			if (Math.abs(yLoc[ID] - aStar.caminhoY[ID]) < speed[ID]) yLoc[ID] = aStar.caminhoY[ID];
		}
	}

	void RenderScreen(){
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

			batch.draw(mapImage, 0, 0);
	
			DrawMap(batch);
	
			batch.draw(ladraoRegion,xLoc[1],yLoc[1],AStar.tamanhoPixel,AStar.tamanhoPixel);
			for (char ID = 2; ID <= 3; ID++)
				batch.draw(policiaRegion,xLoc[ID],yLoc[ID],AStar.tamanhoPixel,AStar.tamanhoPixel);
	
		batch.end();

	}

	void salvarMapa(){

	}



}
