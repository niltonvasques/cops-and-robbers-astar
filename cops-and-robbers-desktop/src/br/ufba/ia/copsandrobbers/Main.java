package br.ufba.ia.copsandrobbers;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "cops-and-robbers";
		cfg.useGL20 = false;
		cfg.width = 800;
		cfg.height = 600;
		
		new LwjglApplication(new PoliciaLadraoGame(), cfg);
	}
}
