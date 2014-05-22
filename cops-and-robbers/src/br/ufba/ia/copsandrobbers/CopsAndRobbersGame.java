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

public class CopsAndRobbersGame implements ApplicationListener {
	
	private AStar aStar = new AStar();
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	
	private TextureAtlas atlas;
	private TextureRegion wallImage, mapImage;
	private TextureRegion smiley, chaser;
	
	//-----------------------------------------------------------------------------
	// Global variables and constants
	//-----------------------------------------------------------------------------
	
	private int smileyActivated = 1;
	private int xLoc[] = new int[4], yLoc[] = new int[4], speed[] = new int[4];
	private long searchTime, g_showDirections=0; 
	
	
	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(w, h);
		camera.position.set(w/2, h/2, 0);
		camera.update();
		
		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		LoadMapData();
		LoadUnitData();
		LoadGraphics();
		aStar.inicializarBuscadorDeCaminho();
	}

	@Override
	public void dispose() {
		batch.dispose();
		atlas.dispose();
		SaveMapData();
		
//		EndPathfinder();
//		EndGraphics();
	}
	
	//-----------------------------------------------------------------------------
	// Name: GameMain
	// Desc: Launch and run game.
	//-----------------------------------------------------------------------------
	//Main game loop
	@Override
	public void render() {		
		
		
//	    while (!KeyDown(27)) //While escape key not pressed
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) Gdx.app.exit();
		CheckUserInput();
		//Move smiley
		if (smileyActivated == 1) MoveSmiley();
//	
		//Move chasers
		if (smileyActivated == 1)  
			for (int ID = 2; ID <= 3; ID++) 
				MoveChaser(ID);

		
		RenderScreen(); //draw stuff on screen
		
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
	
	
	
	//-----------------------------------------------------------------------------
	// Function Prototypes: where necessary
	//-----------------------------------------------------------------------------
	

	//-----------------------------------------------------------------------------
	// Name: CheckUserInput
	// Desc: Process key and mouse presses.
	//-----------------------------------------------------------------------------
	private void CheckUserInput (){
		if (Gdx.input.isKeyPressed(Keys.ENTER))
		{
			smileyActivated = 1 - smileyActivated;
			if (smileyActivated == 1) CreateMapImage();
		}
		if (smileyActivated == 0) EditMap();

		//Show/hide directions by pressing space bar.
		if (Gdx.input.isKeyPressed(Keys.SPACE)) g_showDirections = 1-g_showDirections;
	}
	
	//-----------------------------------------------------------------------------
	// Name: CreateMapImage
	// Desc: Creates the map image
	//-----------------------------------------------------------------------------
	void CreateMapImage(){
		Pixmap pixmap = new Pixmap(1024, 1024, Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		pixmap.setColor(Color.GRAY);
//		Color(0,0,255);//set default color to blue
		for (int x = 0; x <= 79; x++){
			for (int y = 0; y <= 59; y++){
			//Draw blue walls
			if (aStar.possibilidadeDeCaminhada[x][y] == aStar.caminhoBloqueado) {
				pixmap.fillRectangle(x*AStar.tamanhoPixel,y*AStar.tamanhoPixel,AStar.tamanhoPixel,AStar.tamanhoPixel);
			}
		}}		
		Texture map = new Texture(pixmap);
		mapImage = new TextureRegion(map, 800, 600);
		mapImage.flip(false, true);
	}
	
	void CreateWallImage(){
		Pixmap pixmap = new Pixmap(16, 16, Format.RGB888);
		pixmap.setColor(Color.BLUE);
		pixmap.drawRectangle(0,0,10,10);
		Texture map = new Texture(pixmap);
		wallImage = new TextureRegion(map, 10,10);
	}
	
	void DrawMap(SpriteBatch batch){
		if (smileyActivated == 1) 
			batch.draw(mapImage,0,0);
		else
		{
			for (int x = 0; x <= 79; x++){
			for (int y = 0; y <= 59; y++){

				//Draw blue walls
//				if (aStar.walkability[x][y] == AStar.caminhoBloqueado) 
//					batch.draw(wallImage,x*AStar.tamanhoPixel,y*AStar.tamanhoPixel,AStar.tamanhoPixel,AStar.tamanhoPixel);
			}}
		}
	}
	
	void EditMap(){
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
	
	void LoadGraphics(){
		atlas = new TextureAtlas("data/textures.pack");
//		SetFont("Arial",14);
		CreateWallImage();
		CreateMapImage();

		smiley = atlas.findRegion("smiley x 10");

		chaser = atlas.findRegion("ghost x 10");
	}
	
	//-----------------------------------------------------------------------------
	// Name: LoadMapData
	// Desc: Load any pre-existing map when launching the program.
	//-----------------------------------------------------------------------------
	void LoadMapData(){
		
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
			else //initialize the map to completely walkable
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
	

	//-----------------------------------------------------------------------------
	// Name: LoadUnitData
	// Desc: Initialize unit-related data
	//-----------------------------------------------------------------------------
	void LoadUnitData(){
		xLoc[1] = 80 ; yLoc[1] = 160; //initial smiley location
		xLoc[2] = 320 ; yLoc[2] = 320; //initial chaser location
		xLoc[3] = 160 ; yLoc[3] = 320; //initial chaser location	
		speed[1] = 3;//smiley speed
		speed[2] = 2;//chaser
		speed[3] = 1;//chaser
	}
	
	//-----------------------------------------------------------------------------
	// Name: MoveChaser
	// Desc: This subroutine moves the chasers/ghosts around on the screen.
	//		In this case the encontrarCaminho function is accessed automatically when
	//		a chaser reaches the end of his current path. The path info
	//		is also updated occasionally.
	//-----------------------------------------------------------------------------
	void MoveChaser(int ID){
		int targetID = 1; //ID of target (the smiley)

		//1. Find Path: If smiley and chaser are not at the same location on the 
		//			screen and no path is currently active, find a new path.
		if (xLoc[ID] != xLoc[targetID] || yLoc[ID] != yLoc[targetID]) 
		{
		//If no path has been generated, generate one. Update it when
		//the chaser reaches its fifth step on the current path.	
		if (aStar.statusDoCaminho[ID] == AStar.naoComecou|| aStar.localizacaoDoCaminho[ID] == 10)
		{
			//Generate a new path. Enter coordinates of smiley sprite (xLoc(1)/
			//yLoc(1)) as the target.
			if(ID == 2){
				aStar.statusDoCaminho[ID] = aStar.encontrarCaminho(ID,xLoc[ID],yLoc[ID],
						xLoc[targetID],yLoc[targetID]);
			}else{
				aStar.statusDoCaminho[ID] = aStar.encontrarCaminhoBuscaCega(ID,xLoc[ID],yLoc[ID],
					xLoc[targetID],yLoc[targetID]);
			}
			
		}} 
		
		
		if(ID == 3){
			System.out.println("POLICIAL 2");
		}

	//2.Move chaser.
		if (aStar.statusDoCaminho[ID] == AStar.encontrado) MoveSprite(ID);
	}
	
	void MoveSmiley(){
		int ID = 1; //ID of smiley sprite

		//1.Find Path: If smiley is active, any left or right click 
//			on the map will find a path and make him go there.
			if (Gdx.input.isButtonPressed(Buttons.LEFT) || Gdx.input.isButtonPressed(Buttons.RIGHT))
			{	
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);
				
				//Call the encontrarCaminho function.
				long time1 = TimeUtils.millis();
				aStar.statusDoCaminho[ID] = aStar.encontrarCaminho(ID,xLoc[ID],yLoc[ID],(int)touchPos.x,(int)touchPos.y);
				long time2 = TimeUtils.millis();
				searchTime = time2-time1;
			}

		//2.Move smiley.
			if (aStar.statusDoCaminho[ID] == AStar.encontrado) MoveSprite(ID);
	}
	
	void MoveSprite(int ID){
		//1.Read path information
		aStar.leituraDoCaminho(ID,xLoc[ID],yLoc[ID],speed[ID]);

	//2.Move sprite. xLoc/yLoc = current location of sprite. caminhoX and
//		caminhoY = coordinates of next step on the path that were/are
//		read using the readPath function.
		if (xLoc[ID] > aStar.caminhoX[ID]) xLoc[ID] = xLoc[ID] - speed[ID];
		if (xLoc[ID] < aStar.caminhoX[ID]) xLoc[ID] = xLoc[ID] + speed[ID];
		if (yLoc[ID] > aStar.caminhoY[ID]) yLoc[ID] = yLoc[ID] - speed[ID];		
		if (yLoc[ID] < aStar.caminhoY[ID]) yLoc[ID] = yLoc[ID] + speed[ID];
		
	//3.When sprite reaches the end location square	(end of its current
//		path) ...		
		if (aStar.localizacaoDoCaminho[ID] == aStar.tamanhoDoCaminho[ID]) 
		{
//			Center the chaser in the square (not really necessary, but 
//			it looks a little better for the chaser, which moves in 3 pixel
//			increments and thus isn't always centered when it reaches its
//			target).
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
			
			batch.draw(smiley,xLoc[1],yLoc[1],AStar.tamanhoPixel,AStar.tamanhoPixel);
			for (char ID = 2; ID <= 3; ID++)
				batch.draw(chaser,xLoc[ID],yLoc[ID],AStar.tamanhoPixel,AStar.tamanhoPixel);
	
			//Show directions
			if (g_showDirections != 0) 
				ShowDirections();
			else
			{
	//			SetFont("Arial",14);
	//			Text(0,0,"Press space bar for directions");
			}
	
			if (smileyActivated == 0) 
			{
	//			SetFontColor (255,0,0);
	//			Text (0,20,"Map Edit Mode");
	//			SetFontColor (255,255,255);
			}
	//		else
	//			Text (0,20,combine "Search Time = " + searchTime + " ms");
	//		DrawImage (mousePointer,MouseX(),MouseY());
	//		Flip();
		
		batch.end();

		
	}
	
	void SaveMapData(){
		
	}
	
	void ShowDirections(){
		
	}


}
