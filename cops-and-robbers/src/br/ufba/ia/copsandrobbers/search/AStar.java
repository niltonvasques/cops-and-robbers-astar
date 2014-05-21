
package br.ufba.ia.copsandrobbers.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class AStar {

	//Declare constants
	public static final int mapWidth = 80, mapHeight = 60, tileSize = 20, numberPeople = 3;
	public static final int notfinished = 0, notStarted = 0; // constantes relacionadas ao caminho
	public static final int found = 1, nonexistent = 2; 
	public static final int walkable = 0, unwalkable = 1;    // constantes referente a habilidade de andar.
	public int onClosedList = 10;

	//Create needed arrays
	public char[][] walkability = new char[mapWidth][mapHeight];
	public int[] openList = new int[mapWidth*mapHeight+2]; //array de 1 dimensão que segunda uma lista aberta de items
	public int[][] whichList = new int[mapWidth+1][mapHeight+1];  //array de 2 dimensões usado para gravar  
	// 		se uma célula está na lista aberta ou na lista fechada.
	public int[] openX = new int[mapWidth*mapHeight+2]; //array de 1 dimensão armazenando a posição x de um item na lista aberta.
	public int[] openY = new int[mapWidth*mapHeight+2]; //array de 1 dimensão armazenando a posição y de um item na lista aberta.
	public int[][] parentX = new int[mapWidth+1][mapHeight+1]; //array de 2 dimensões para armazenar o pai de cada célular x
	public int[][] parentY = new int[mapWidth+1][mapHeight+1]; //array de 2 dimensões para armazenar o pai de cada célula y
	public int[] Fcost = new int[mapWidth*mapHeight+2];	//array de 1 dimensão para armazenar o custo F de cada célula.
	public int[][] Gcost = new int[mapWidth+1][mapHeight+1]; 	//array de 2 dimensões para armazenar o custo G para cada célula
	public int[] Hcost = new int[mapWidth*mapHeight+2];	//array de 1 dimensão para armazenar o custo H de cada célula na lista aberta
	public int[] pathLength = new int[numberPeople+1];     //armazena o tamanho do caminho encontrado para a criatura
	public int[] pathLocation = new int[numberPeople+1];   //armazena a posição atual ao longo do caminho escolhido para a criatura		
	//int* pathBank [numberPeople+1];
	public List<Integer> pathBank[] = new List[numberPeople+1];
	public List<Integer[]> pathBank2 = new ArrayList<Integer[]>();

	//Path reading variables
	public int[] pathStatus = new int[numberPeople+1];
	public int[] xPath = new int[numberPeople+1];
	public int[] yPath = new int[numberPeople+1];

	public void InitializePathfinder ()
	{
		for (int x = 0; x < numberPeople+1; x++){
			pathBank[x] = new Vector<Integer>();
			pathBank2.add(new Integer[4]);
		}
	}

	public void  EndPathfinder ()
	{
		for (int x = 0; x < numberPeople+1; x++){
			pathBank[x].clear();
			pathBank2.set(x, null);
		}
	}

	public int FindPath (int pathfinderID, int startingX, int startingY,
			int targetX, int targetY)
	{
		int onOpenList=0, parentXval=0, parentYval=0, a=0, b=0, m=0, u=0, v=0, temp=0, corner=0, numberOfOpenListItems=0, addedGCost=0, tempGcost = 0, path = 0, tempx, pathX, pathY, cellPosition, newOpenListItemID=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de walkability.
		int startX = startingX/tileSize;
		int startY = startingY/tileSize;	
		targetX = targetX/tileSize;
		targetY = targetY/tileSize;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] > 0)
			return found;
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] == 0)
			return nonexistent;

		//Se o quadrado alvo é unwalkable(não andável), retorne que o caminho é inexistente.
		if (walkability[targetX][targetY] == unwalkable)
		{
			xPath[pathfinderID] = startingX;
			yPath[pathfinderID] = startingY;
			return nonexistent;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (onClosedList > 1000000) //Resetando whichList ocasionalmente
		{
			for (int x = 0; x < mapWidth; x++) {
				for (int y = 0; y < mapHeight; y++)
					whichList [x][y] = 0;
			}
			onClosedList = 10;	
		}
		onClosedList = onClosedList+2; //alterando os valores da openList(lista aberta) e onClosed list é mais rapida do que redimming whichList() array;
		onOpenList = onClosedList-1;
		pathLength [pathfinderID] = notStarted;//i.e, = 0
		pathLocation [pathfinderID] = notStarted;//i.e, = 0
		Gcost[startX][startY] = 0; //resetando o quadrado inicial com o valor de G para 0

		//4. Adicionando a posição inicial openList de quadrados para serem verificados.
		numberOfOpenListItems = 1;
		openList[1] = 1;		//colocando este como o item do topo(e atualmente somente) da openList, que é mantida como uma heap binaria.

		openX[1] = startX;
		openY[1] = startY;

		//5. Faça o seguinte até que um caminho é encontrador ou ele não exista.
		do
		{

			//6. Se a openList não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na openList.
			if (numberOfOpenListItems != 0)
			{

				//7. Remova o primeiro item da openList.
				parentXval = openX[openList[1]];
				parentYval = openY[openList[1]]; //Grave as coordenadas da celula do item
				whichList[parentXval][parentYval] = onClosedList;//adicione o item para a closedList

				//OpenList = Heap binária: Delete este item da openList, que é mantido como uma heap binária. Para mais informações veja:
				// http://www.policyalmanac.org/games/binaryHeaps.htm
				numberOfOpenListItems = numberOfOpenListItems - 1;//reduzindo o numero de items da openList em 1	

				// Delete o item do topo na heap binaria, e reordene a heap, com o item de menor custo da função F indo para o topo.
				openList[1] = openList[numberOfOpenListItems+1];//mova o ultimo item na heap a cima para o slot #1
				v = 1;

				//Repita o seguinte até que o novo item no slot1 caia para a sua própria posição na heap.
				do
				{
					u = v;		
					if (2*u+1 <= numberOfOpenListItems) //Se ambos os filhos existirem
					{
						//Verifique se o custo de F do pai é maior do que cada filho.
						//Selecione o menor dos dois filhos.
						if (Fcost[openList[u]] >= Fcost[openList[2*u]]) 
							v = 2*u;
						if (Fcost[openList[v]] >= Fcost[openList[2*u+1]]) 
							v = 2*u+1;		
					}
					else
					{
						if (2*u <= numberOfOpenListItems) //Se somente o filho 1 existe
						{
							//Verifique se o custo de F do pai é maior do que o filho 1.
							if (Fcost[openList[u]] >= Fcost[openList[2*u]]) 
								v = 2*u;
						}
					}

					if (u != v) //Se o custo de F do pai é > do um dos filhos troque eles.
					{
						temp = openList[u];
						openList[u] = openList[v];
						openList[v] = temp;			
					}
					else
						break; //de outro modo, saia do loop

				}
				while (!(Gdx.input.isKeyPressed(Keys.ESCAPE)));  //Tentei isso, mas n�o sei como criar a vari�vel actualkey.

				//7. Verifique os quadrados adjacentes. (Estes "filhos" -- aquele caminho dos filhos são similares,
				//conceitualmente, para a heap binaria mencionada a cima, mas não confuda eles. Eles são diferentes.
				//O caminho dos filhos são descritos no Demo 1 com pontos cinzas a frente dos pais.) Adicione aqueles 
				//quadrados dos filhos adjacens para a openList para posterior consideração se apropriado. (ver vários blocos abaixo).
				for (b = parentYval-1; b <= parentYval+1; b++){
					for (a = parentXval-1; a <= parentXval+1; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != mapWidth && b != mapHeight){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (whichList[a][b] != onClosedList) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (walkability [a][b] != unwalkable) { 

									//		Não corte as bordas cruzadas.
									corner = walkable;	
									if (a == parentXval-1) 
									{
										if (b == parentYval-1)
										{
											if (walkability[parentXval-1][parentYval] == unwalkable || walkability[parentXval][parentYval-1] == unwalkable)  corner = unwalkable;
										}
										else if (b == parentYval+1)
										{
											if (walkability[parentXval][parentYval+1] == unwalkable
													|| walkability[parentXval-1][parentYval] == unwalkable) 
												corner = unwalkable; 
										}
									}
									else if (a == parentXval+1)
									{
										if (b == parentYval-1)
										{
											if (walkability[parentXval][parentYval-1] == unwalkable 
													|| walkability[parentXval+1][parentYval] == unwalkable) 
												corner = unwalkable;
										}
										else if (b == parentYval+1)
										{
											if (walkability[parentXval+1][parentYval] == unwalkable 
													|| walkability[parentXval][parentYval+1] == unwalkable)
												corner = unwalkable; 
										}
									}	
									if (corner == walkable) {

										//		Se não já está na openList, adicione este para a openlist.			
										if (whichList[a][b] != onOpenList) 
										{	

											//Cria um item novo na openList na heap binaria.
											newOpenListItemID = newOpenListItemID + 1; //Cada novo item tem um ID unico.
											m = numberOfOpenListItems+1;
											openList[m] = newOpenListItemID;// Coloque o novo item da openList(atualmente ID#) na base da heap.
											openX[newOpenListItemID] = a;
											openY[newOpenListItemID] = b;//grave suas coordenadas x e y do novo item

											//Calculando o custo de G
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//custo de ir pelas diagonais dos quadrados;	
											else	
												addedGCost = 10;//custo de ir em não diagonais.		
											Gcost[a][b] = Gcost[parentXval][parentYval] + addedGCost;

											//Calcular os custos H e F e o pai
											Hcost[openList[m]] = AStar.tileSize*(Math.abs(a - targetX) + Math.abs(b - targetY));
											Fcost[openList[m]] = Gcost[a][b] + Hcost[openList[m]];
											parentX[a][b] = parentXval ; parentY[a][b] = parentYval;	

											//Mover o novo item da openList para o seu pŕoprio lugar na heap binária.
											//Iniciando da base, sucessivamente comparar items pais, 
											//trocando quando necessário até que o item encontre seu lugar na heap.
											//ou borbulhando todos os caminhos para o topo (se este tem o menor custo de F).
											while (m != 1) //Enquanto o item não tem sido borbulhado para o topo(m=1)	
											{
												//Verifique se o custo F do filho é < o custo F do pai. Se for, troque-os.
												if (Fcost[openList[m]] <= Fcost[openList[m/2]])
												{
													temp = openList[m/2];
													openList[m/2] = openList[m];
													openList[m] = temp;
													m = m/2;
												}
												else
													break;
											}
											numberOfOpenListItems = numberOfOpenListItems+1;//Adicione um para o número de items na heap

											//Troque whichList para mostrar que o novo item está na openList.
											whichList[a][b] = onOpenList;
										}

										//8.If adjacent cell is already on the open list, check to see if this
										//8. Se a célula adjacente já está na openList, verifique para ver se este
										//		caminho para a aquela célula da posição inicial, é um melhor.
										//		Se for, troque o pai da célula e seus custos G e F.	
										else //Se whichList(a,b) = onOpenList
										{

											//Calcular o custo G deste possível caminho novo.
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//Custo de ir pelas diagonais	
											else	
												addedGCost = 10;//Custo de ir por não diagonais.				
											tempGcost = Gcost[parentXval][parentYval] + addedGCost;

											//Se este caminho é curto ( custo de G é baixo) então troque
											//a célula pai, custo de G e custo de F.
											if (tempGcost < Gcost[a][b]) //Se o custo de G é menor,
											{
												parentX[a][b] = parentXval; //troque o quadrado pai
												parentY[a][b] = parentYval;
												Gcost[a][b] = tempGcost;//troque o custo de G			

												//Porque trocando o custo de G também muda o custo de F, se 
												//o item está na openList nós precisamos alterar o custo F 
												//gravado no item e sua posição na openList para ter certeza 
												//que nós mantemos uma openList corretamente ordenada.
												for (int x = 1; x <= numberOfOpenListItems; x++) //olho para o item na openList
												{
													if (openX[openList[x]] == a && openY[openList[x]] == b) //item encontrado
													{
														Fcost[openList[x]] = Gcost[a][b] + Hcost[openList[x]];//troque o custo F

														//Veja se alterando o bubbles score de F do item a cima da sua localização corrente na heap.
														m = x;
														while (m != 1) //Enquanto o item não foi borbulhado para o topo (m = 1).	
														{
															//Verifique se o filho é < pai. Se for, troque-os.
															if (Fcost[openList[m]] < Fcost[openList[m/2]])
															{
																temp = openList[m/2];
																openList[m/2] = openList[m];
																openList[m] = temp;
																m = m/2;
															}
															else
																break;
														} 
														break; //saia para x = loop
													} //Se openX(openList(x)) = a
												} //For x = 1 To numberOfOpenListItems
											}//If tempGcost < Gcost(a,b)

										}//else If whichList(a,b) = onOpenList	
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
					}//for (a = parentXval-1; a <= parentXval+1; a++){
				}//for (b = parentYval-1; b <= parentYval+1; b++){

			}//if (numberOfOpenListItems != 0)

			//9. Se a openList está vazia então não existe um caminho.
			else
			{
				path = nonexistent; break;
			}  

			//Se o alvo é adicionado a openList, então o caminho foi encontrado.
			if (whichList[targetX][targetY] == onOpenList)
			{
				path = found; break;
			}

		}
		while (true);//Faça o seguinte até que o caminho seja encontrado ou ele não exista.

		//10. Salve o caminho se ele não exista.
		if (path == found)
		{

			//a. Trabalhando para trás do alvo para a posição inicial, verificando 
			//		cada célula pai, calcular o tamanho do caminho.
			pathX = targetX; pathY = targetY;
			do
			{
				//Visitar o pai da célula corrente.
				tempx = parentX[pathX][pathY];		
				pathY = parentY[pathX][pathY];
				pathX = tempx;

				//Calcular o tamanho do caminho.
				pathLength[pathfinderID] = pathLength[pathfinderID] + 1;
			}
			while (pathX != startX || pathY != startY);

			//b.Redimensione o dataBank para o correto tamanho em bytes.
			Integer[] arr = pathBank2.get(pathfinderID);
			pathBank2.set(pathfinderID, Arrays.copyOf(arr, pathLength[pathfinderID]*8));

			//c. Agora copie as informações do caminho sobre o databank. Desde que 
			//		nós estamos trabahando atrás do alvo para da posição inicial, nós
			//		copiamos a informação para o data bank na ordem reversa. O resultado é
			//		um conjunto corretamente ordenado de dados do caminho, para o primeiro passo
			//		até o último.
			pathX = targetX ; pathY = targetY;
			cellPosition = pathLength[pathfinderID]*2;//Inicie do final	
			do
			{
				cellPosition = cellPosition - 2;//trabalhe 2 inteiros para trás
				pathBank2.get(pathfinderID)[cellPosition] = pathX;
				pathBank2.get(pathfinderID)[cellPosition+1] = pathY;
	
				//d. Visite o pai da célula atual.
				tempx = parentX[pathX][pathY];		
				pathY = parentY[pathX][pathY];
				pathX = tempx;

				//e. Se nós temos encontrado o quadrado incial, saia do loop.
			}
			while (pathX != startX || pathY != startY);	

			//11. Leia o primeiro passo dentro dos arrays xPath/yPath. 
			ReadPath(pathfinderID,startingX,startingY,1);

		}
		return path;
	}

	public void ReadPath (int pathfinderID,int currentX,int currentY, int pixelsPerFrame)
	{
		int ID = pathfinderID; //redundante, mas faz que o seguinte seja mais fácil para ler.
		//Se um caminho tem sido encontrado para o pathfinder ...
		if (pathStatus[ID] == found)
		{

			//Se path finder está apenas iniciando um novo vaminho ou tenha alcançado o
			//centro do caminho atual ( e o final do caminho não tenha sido encontrado), visite o próximo quadrado do caminho.
			if (pathLocation[ID] < pathLength[ID])
			{
				//Se apenas começando ou se está fechado para o centro do quadrado
				if (pathLocation[ID] == 0 || 
						(Math.abs(currentX - xPath[ID]) < pixelsPerFrame && Math.abs(currentY - yPath[ID]) < pixelsPerFrame))
					pathLocation[ID] = pathLocation[ID] + 1;
			}

			//Leia o dado do caminho.
			xPath[ID] = ReadPathX(ID,pathLocation[ID]);
			yPath[ID] = ReadPathY(ID,pathLocation[ID]);

			//Se o centro do último quadrado caminho no caminho tem sido alcançado resete-o.
			if (pathLocation[ID] == pathLength[ID]) 
			{
				if (Math.abs(currentX - xPath[ID]) < pixelsPerFrame 
						&& Math.abs(currentY - yPath[ID]) < pixelsPerFrame) //Se perto o suficiente do quadrado do centro
					pathStatus[ID] = notStarted; 
			}
		}

		//Se não tem caminho para este pathfinder, simplismente fique na posição inicial.
		else
		{	
			xPath[ID] = currentX;
			yPath[ID] = currentY;
		}
	}

	//The following two functions read the raw path data from the pathBank.
	//You can call these functions directly and skip the readPath function
	//above if you want. Make sure you know what your current pathLocation
	//is.
	
	//As seguintes duas funções leem um raw path data do path pathBank.
	//Você pode chamar estas funções diretamente e pular a função readPath
	// a cima se você quiser. Tenha certeza que você sabe qual é a sua posição
	//atual.

	//-----------------------------------------------------------------------------
	// Name: ReadPathX
	// Desc: Le a coordenada x do próximo passo do caminho
	//-----------------------------------------------------------------------------
	int ReadPathX(int pathfinderID,int pathLocation)
	{
		int x = 0;
		if (pathLocation <= pathLength[pathfinderID])
		{

			//Le a coordenada X do pathPathBank
			x = pathBank2.get(pathfinderID)[pathLocation*2-2];

			//Ajusta a coordenada para ela ficar alinhada ao inicio do quadrado. 
			//Assumindo que estamos usando sprites que não são centralizados..
			x = (int) (tileSize*x);

		}

		return x;
	}	


	//-----------------------------------------------------------------------------
	// Name: ReadPathY
	// Desc: Le a coordenada y do próximo passo do caminho
	//-----------------------------------------------------------------------------
	int ReadPathY(int pathfinderID,int pathLocation)
	{
		int y = 0;
		if (pathLocation <= pathLength[pathfinderID])
		{

			//Le as coordenadas do pathBank.
			//		y = pathBank[pathfinderID].get(pathLocation*2-1);
			y = pathBank2.get(pathfinderID)[pathLocation*2-1];

			//Ajusta a coordenada para ela ficar alinhada ao inicio do quadrado. 
			//Assumindo que estamos usando sprites que não são centralizados..
			y = (int) ( tileSize*y);

		}
		return y;
	}
	
	public int FindPathBuscaCega (int pathfinderID, int startingX, int startingY,
			int targetX, int targetY)
	{
		int onOpenList=0, parentXval=0, parentYval=0, a=0, b=0, m=0, u=0, v=0, temp=0, corner=0, numberOfOpenListItems=0, addedGCost=0, tempGcost = 0, path = 0, tempx, pathX, pathY, cellPosition, newOpenListItemID=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de walkability.
		int startX = startingX/tileSize;
		int startY = startingY/tileSize;	
		targetX = targetX/tileSize;
		targetY = targetY/tileSize;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] > 0)
			return found;
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] == 0)
			return nonexistent;

		//Se o quadrado alvo é unwalkable(não andável), retorne que o caminho é inexistente.
		if (walkability[targetX][targetY] == unwalkable)
		{
			xPath[pathfinderID] = startingX;
			yPath[pathfinderID] = startingY;
			return nonexistent;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (onClosedList > 1000000) //Resetando whichList ocasionalmente
		{
			for (int x = 0; x < mapWidth; x++) {
				for (int y = 0; y < mapHeight; y++)
					whichList [x][y] = 0;
			}
			onClosedList = 10;	
		}
		onClosedList = onClosedList+2; //alterando os valores da openList(lista aberta) e onClosed list é mais rapida do que redimming whichList() array;
		onOpenList = onClosedList-1;
		pathLength [pathfinderID] = notStarted;//i.e, = 0
		pathLocation [pathfinderID] = notStarted;//i.e, = 0
		Gcost[startX][startY] = 0; //resetando o quadrado inicial com o valor de G para 0

		//4. Adicionando a posição inicial openList de quadrados para serem verificados.
		numberOfOpenListItems = 1;
		openList[1] = 1;		//colocando este como o item do topo(e atualmente somente) da openList, que é mantida como uma heap binaria.

		openX[1] = startX;
		openY[1] = startY;

		//5. Faça o seguinte até que um caminho é encontrador ou ele não exista.
		do
		{

			//6. Se a openList não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na openList.
			if (numberOfOpenListItems != 0)
			{

				//7. Remova o primeiro item da openList.
				parentXval = openX[openList[1]];
				parentYval = openY[openList[1]]; //Grave as coordenadas da celula do item
				whichList[parentXval][parentYval] = onClosedList;//adicione o item para a closedList

				//OpenList = Heap binária: Delete este item da openList, que é mantido como uma heap binária. Para mais informações veja:
				// http://www.policyalmanac.org/games/binaryHeaps.htm
				numberOfOpenListItems = numberOfOpenListItems - 1;//reduzindo o numero de items da openList em 1	

				// Delete o item do topo na heap binaria, e reordene a heap, com o item de menor custo da função F indo para o topo.
				openList[1] = openList[numberOfOpenListItems+1];//mova o ultimo item na heap a cima para o slot #1
				v = 1;

				//Repita o seguinte até que o novo item no slot1 caia para a sua própria posição na heap.
				do
				{
					u = v;		
					if (2*u+1 <= numberOfOpenListItems) //Se ambos os filhos existirem
					{
						//Verifique se o custo de F do pai é maior do que cada filho.
						//Selecione o menor dos dois filhos.
						if (Fcost[openList[u]] >= Fcost[openList[2*u]]) 
							v = 2*u;
						if (Fcost[openList[v]] >= Fcost[openList[2*u+1]]) 
							v = 2*u+1;		
					}
					else
					{
						if (2*u <= numberOfOpenListItems) //Se somente o filho 1 existe
						{
							//Verifique se o custo de F do pai é maior do que o filho 1.
							if (Fcost[openList[u]] >= Fcost[openList[2*u]]) 
								v = 2*u;
						}
					}

					if (u != v) //Se o custo de F do pai é > do um dos filhos troque eles.
					{
						temp = openList[u];
						openList[u] = openList[v];
						openList[v] = temp;			
					}
					else
						break; //de outro modo, saia do loop

				}
				while (!(Gdx.input.isKeyPressed(Keys.ESCAPE)));  //Tentei isso, mas n�o sei como criar a vari�vel actualkey.

				//7. Verifique os quadrados adjacentes. (Estes "filhos" -- aquele caminho dos filhos são similares,
				//conceitualmente, para a heap binaria mencionada a cima, mas não confuda eles. Eles são diferentes.
				//O caminho dos filhos são descritos no Demo 1 com pontos cinzas a frente dos pais.) Adicione aqueles 
				//quadrados dos filhos adjacens para a openList para posterior consideração se apropriado. (ver vários blocos abaixo).
				for (b = parentYval-1; b <= parentYval+1; b++){
					for (a = parentXval-1; a <= parentXval+1; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != mapWidth && b != mapHeight){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (whichList[a][b] != onClosedList) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (walkability [a][b] != unwalkable) { 

									//		Não corte as bordas cruzadas.
									corner = walkable;	
									if (a == parentXval-1) 
									{
										if (b == parentYval-1)
										{
											if (walkability[parentXval-1][parentYval] == unwalkable || walkability[parentXval][parentYval-1] == unwalkable)  corner = unwalkable;
										}
										else if (b == parentYval+1)
										{
											if (walkability[parentXval][parentYval+1] == unwalkable
													|| walkability[parentXval-1][parentYval] == unwalkable) 
												corner = unwalkable; 
										}
									}
									else if (a == parentXval+1)
									{
										if (b == parentYval-1)
										{
											if (walkability[parentXval][parentYval-1] == unwalkable 
													|| walkability[parentXval+1][parentYval] == unwalkable) 
												corner = unwalkable;
										}
										else if (b == parentYval+1)
										{
											if (walkability[parentXval+1][parentYval] == unwalkable 
													|| walkability[parentXval][parentYval+1] == unwalkable)
												corner = unwalkable; 
										}
									}	
									if (corner == walkable) {

										//		Se não já está na openList, adicione este para a openlist.			
										if (whichList[a][b] != onOpenList) 
										{	

											//Cria um item novo na openList na heap binaria.
											newOpenListItemID = newOpenListItemID + 1; //Cada novo item tem um ID unico.
											m = numberOfOpenListItems+1;
											openList[m] = newOpenListItemID;// Coloque o novo item da openList(atualmente ID#) na base da heap.
											openX[newOpenListItemID] = a;
											openY[newOpenListItemID] = b;//grave suas coordenadas x e y do novo item

											//Calculando o custo de G
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//custo de ir pelas diagonais dos quadrados;	
											else	
												addedGCost = 10;//custo de ir em não diagonais.		
											Gcost[a][b] = 1;

											//Calcular os custos H e F e o pai
											Hcost[openList[m]] = 1;
											Fcost[openList[m]] = Gcost[a][b] + Hcost[openList[m]];
											parentX[a][b] = parentXval ; parentY[a][b] = parentYval;	

											//Mover o novo item da openList para o seu pŕoprio lugar na heap binária.
											//Iniciando da base, sucessivamente comparar items pais, 
											//trocando quando necessário até que o item encontre seu lugar na heap.
											//ou borbulhando todos os caminhos para o topo (se este tem o menor custo de F).
											while (m != 1) //Enquanto o item não tem sido borbulhado para o topo(m=1)	
											{
												//Verifique se o custo F do filho é < o custo F do pai. Se for, troque-os.
												if (Fcost[openList[m]] <= Fcost[openList[m/2]])
												{
													temp = openList[m/2];
													openList[m/2] = openList[m];
													openList[m] = temp;
													m = m/2;
												}
												else
													break;
											}
											numberOfOpenListItems = numberOfOpenListItems+1;//Adicione um para o número de items na heap

											//Troque whichList para mostrar que o novo item está na openList.
											whichList[a][b] = onOpenList;
										}

										//8.If adjacent cell is already on the open list, check to see if this
										//8. Se a célula adjacente já está na openList, verifique para ver se este
										//		caminho para a aquela célula da posição inicial, é um melhor.
										//		Se for, troque o pai da célula e seus custos G e F.	
										else //Se whichList(a,b) = onOpenList
										{

											//Calcular o custo G deste possível caminho novo.
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//Custo de ir pelas diagonais	
											else	
												addedGCost = 10;//Custo de ir por não diagonais.				
											tempGcost = Gcost[parentXval][parentYval];

											//Se este caminho é curto ( custo de G é baixo) então troque
											//a célula pai, custo de G e custo de F.
											if (tempGcost < Gcost[a][b]) //Se o custo de G é menor,
											{
												parentX[a][b] = parentXval; //troque o quadrado pai
												parentY[a][b] = parentYval;
												Gcost[a][b] = tempGcost;//troque o custo de G			

												//Porque trocando o custo de G também muda o custo de F, se 
												//o item está na openList nós precisamos alterar o custo F 
												//gravado no item e sua posição na openList para ter certeza 
												//que nós mantemos uma openList corretamente ordenada.
												for (int x = 1; x <= numberOfOpenListItems; x++) //olho para o item na openList
												{
													if (openX[openList[x]] == a && openY[openList[x]] == b) //item encontrado
													{
														Fcost[openList[x]] = Gcost[a][b] + Hcost[openList[x]];//troque o custo F

														//Veja se alterando o bubbles score de F do item a cima da sua localização corrente na heap.
														m = x;
														while (m != 1) //Enquanto o item não foi borbulhado para o topo (m = 1).	
														{
															//Verifique se o filho é < pai. Se for, troque-os.
															if (Fcost[openList[m]] < Fcost[openList[m/2]])
															{
																temp = openList[m/2];
																openList[m/2] = openList[m];
																openList[m] = temp;
																m = m/2;
															}
															else
																break;
														} 
														break; //saia para x = loop
													} //Se openX(openList(x)) = a
												} //For x = 1 To numberOfOpenListItems
											}//If tempGcost < Gcost(a,b)

										}//else If whichList(a,b) = onOpenList	
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
					}//for (a = parentXval-1; a <= parentXval+1; a++){
				}//for (b = parentYval-1; b <= parentYval+1; b++){

			}//if (numberOfOpenListItems != 0)

			//9. Se a openList está vazia então não existe um caminho.
			else
			{
				path = nonexistent; break;
			}  

			//Se o alvo é adicionado a openList, então o caminho foi encontrado.
			if (whichList[targetX][targetY] == onOpenList)
			{
				path = found; break;
			}

		}
		while (true);//Faça o seguinte até que o caminho seja encontrado ou ele não exista.

		//10. Salve o caminho se ele não exista.
		if (path == found)
		{

			//a. Trabalhando para trás do alvo para a posição inicial, verificando 
			//		cada célula pai, calcular o tamanho do caminho.
			pathX = targetX; pathY = targetY;
			do
			{
				//Visitar o pai da célula corrente.
				tempx = parentX[pathX][pathY];		
				pathY = parentY[pathX][pathY];
				pathX = tempx;

				//Calcular o tamanho do caminho.
				pathLength[pathfinderID] = pathLength[pathfinderID] + 1;
			}
			while (pathX != startX || pathY != startY);

			//b.Redimensione o dataBank para o correto tamanho em bytes.
			Integer[] arr = pathBank2.get(pathfinderID);
			pathBank2.set(pathfinderID, Arrays.copyOf(arr, pathLength[pathfinderID]*8));

			//c. Agora copie as informações do caminho sobre o databank. Desde que 
			//		nós estamos trabahando atrás do alvo para da posição inicial, nós
			//		copiamos a informação para o data bank na ordem reversa. O resultado é
			//		um conjunto corretamente ordenado de dados do caminho, para o primeiro passo
			//		até o último.
			pathX = targetX ; pathY = targetY;
			cellPosition = pathLength[pathfinderID]*2;//Inicie do final	
			do
			{
				cellPosition = cellPosition - 2;//trabalhe 2 inteiros para trás
				pathBank2.get(pathfinderID)[cellPosition] = pathX;
				pathBank2.get(pathfinderID)[cellPosition+1] = pathY;
	
				//d. Visite o pai da célula atual.
				tempx = parentX[pathX][pathY];		
				pathY = parentY[pathX][pathY];
				pathX = tempx;

				//e. Se nós temos encontrado o quadrado incial, saia do loop.
			}
			while (pathX != startX || pathY != startY);	

			//11. Leia o primeiro passo dentro dos arrays xPath/yPath. 
			ReadPath(pathfinderID,startingX,startingY,1);

		}
		return path;
	}

}