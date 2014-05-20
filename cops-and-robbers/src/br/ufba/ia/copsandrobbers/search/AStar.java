package br.ufba.ia.copsandrobbers.search;

import java.util.List;

public class AStar {
	
	//Declare constants
	public static final int mapWidth = 80, mapHeight = 60, tileSize = 10, numberPeople = 3;
	public static final int notfinished = 0, notStarted = 0; // path-related constants
	public static final int found = 1, nonexistent = 2; 
	public static final int walkable = 0, unwalkable = 1;    // walkability array constants
	
	//Create needed arrays
	public char[][] walkability = new char[mapWidth][mapHeight];
	public int[] openList = new int[mapWidth*mapHeight+2]; //1 dimensional array holding ID# of open list items
	public int[][] whichList = new int[mapWidth+1][mapHeight+1];  //2 dimensional array used to record 
// 		whether a cell is on the open list or on the closed list.
	public int[] openX = new int[mapWidth*mapHeight+2]; //1d array stores the x location of an item on the open list
	public int[] openY = new int[mapWidth*mapHeight+2]; //1d array stores the y location of an item on the open list
	public int[][] parentX = new int[mapWidth+1][mapHeight+1]; //2d array to store parent of each cell (x)
	public int[][] parentY = new int[mapWidth+1][mapHeight+1]; //2d array to store parent of each cell (y)
	public int[] Fcost = new int[mapWidth*mapHeight+2];	//1d array to store F cost of a cell on the open list
	public int[][] Gcost = new int[mapWidth+1][mapHeight+1]; 	//2d array to store G cost for each cell.
	public int[] Hcost = new int[mapWidth*mapHeight+2];	//1d array to store H cost of a cell on the open list
	public int[] pathLength = new int[numberPeople+1];     //stores length of the found path for critter
	public int[] pathLocation = new int[numberPeople+1];   //stores current position along the chosen path for critter		
	//int* pathBank [numberPeople+1];
	public List<Integer> pathBank[] = new List[numberPeople+1];
	
	
	//Path reading variables
	public int[] pathStatus = new int[numberPeople+1];
	public int[] xPath = new int[numberPeople+1];
	public int[] yPath = new int[numberPeople+1];
	
	public int FindPath(int ID, int startX, int startY, int targetX, int targetY){
		return 1;
	}
	
	public void ReadPath(int pathfinderID,int currentX,int currentY,
			  int pixelsPerFrame){
		
	}

	/*
	 void InitializePathfinder (void)
{
	for (int x = 0; x < numberPeople+1; x++)
		pathBank [x] = (int*) malloc(4);
}
	 */
	
	public void InitializePathfinder ()
	{
//		for (int x = 0; x < numberPeople+1; x++)
//			pathBank[x] = new List(4);
	}
	
	
	public void  EndPathfinder ()
	{
		for (int x = 0; x < numberPeople+1; x++)
			pathBank[x].clear();
	}
	
	public int FindPath (int pathfinderID, int startingX, int startingY,
			  int targetX, int targetY)
	{
//		public int onOpenList=0, parentXval=0, parentYval=0, a=0, b=0, m=0, u=0, v=0, temp=0, corner=0, numberOfOpenListItems=0, addedGCost=0, tempGcost = 0, path = 0, tempx, pathX, pathY, cellPosition, newOpenListItemID=0;
		
	}
}