
package br.ufba.ia.copsandrobbers.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class AStar {

	//Declare constants
	public static final int larguraTela = 80, alturaTela = 60, tamanhoPixel = 20, qtdeAgentes = 3;
	
	// Constantes relacionadas ao caminho
	public static final int naoTerminou = 0 ;
	public static final int naoComecou = 0;
	public static final int encontrado = 1, naoExiste = 2; 
	public static final int caminhoPassavel = 0, caminhoBloqueado = 1;    // constantes referente a habilidade de andar.
	public int naListaFechada = 10;

	//Create needed arrays
	public char[][] espacoDeCaminhada = new char[larguraTela][alturaTela];
	public int[] listaAberta = new int[larguraTela*alturaTela+2]; //array de 1 dimensão que segunda uma lista aberta de items
	public int[][] whichList = new int[larguraTela+1][alturaTela+1];  //array de 2 dimensões usado para gravar  
	// 		se uma célula está na lista aberta ou na lista fechada.
	public int[] openX = new int[larguraTela*alturaTela+2]; //array de 1 dimensão armazenando a posição x de um item na lista aberta.
	public int[] openY = new int[larguraTela*alturaTela+2]; //array de 1 dimensão armazenando a posição y de um item na lista aberta.
	public int[][] parentX = new int[larguraTela+1][alturaTela+1]; //array de 2 dimensões para armazenar o pai de cada célular x
	public int[][] parentY = new int[larguraTela+1][alturaTela+1]; //array de 2 dimensões para armazenar o pai de cada célula y
	public int[] Fcost = new int[larguraTela*alturaTela+2];	//array de 1 dimensão para armazenar o custo F de cada célula.
	public int[][] Gcost = new int[larguraTela+1][alturaTela+1]; 	//array de 2 dimensões para armazenar o custo G para cada célula
	public int[] Hcost = new int[larguraTela*alturaTela+2];	//array de 1 dimensão para armazenar o custo H de cada célula na lista aberta
	public int[] pathLength = new int[qtdeAgentes+1];     //armazena o tamanho do caminho encontrado para a criatura
	public int[] pathLocation = new int[qtdeAgentes+1];   //armazena a posição atual ao longo do caminho escolhido para a criatura		
	//int* pathBank [qtdeAgentes+1];
	public List<Integer> pathBank[] = new List[qtdeAgentes+1];
	public List<Integer[]> pathBank2 = new ArrayList<Integer[]>();

	//Path reading variables
	public int[] pathStatus = new int[qtdeAgentes+1];
	public int[] xPath = new int[qtdeAgentes+1];
	public int[] yPath = new int[qtdeAgentes+1];

	public void InitializePathfinder ()
	{
		for (int x = 0; x < qtdeAgentes+1; x++){
			pathBank[x] = new Vector<Integer>();
			pathBank2.add(new Integer[4]);
		}
	}

	public void  EndPathfinder ()
	{
		for (int x = 0; x < qtdeAgentes+1; x++){
			pathBank[x].clear();
			pathBank2.set(x, null);
		}
	}

	public int FindPath (int pathfinderID, int startingX, int startingY,
			int targetX, int targetY)
	{
		int naListaAberta=0, parentXval=0, parentYval=0, a=0, b=0, m=0, u=0, v=0, temp=0, corner=0, numItemsListaAberta=0, addedGCost=0, tempGcost = 0, path = 0, tempx, pathX, pathY, cellPosition, novoIDItemListaAberta=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de espacoDeCaminhada.
		int startX = startingX/tamanhoPixel;
		int startY = startingY/tamanhoPixel;	
		targetX = targetX/tamanhoPixel;
		targetY = targetY/tamanhoPixel;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] > 0)
			return encontrado;
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] == 0)
			return naoExiste;

		//Se o quadrado alvo é caminhoBloqueado(não andável), retorne que o caminho é inexistente.
		if (espacoDeCaminhada[targetX][targetY] == caminhoBloqueado)
		{
			xPath[pathfinderID] = startingX;
			yPath[pathfinderID] = startingY;
			return naoExiste;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (naListaFechada > 1000000) //Resetando whichList ocasionalmente
		{
			for (int x = 0; x < larguraTela; x++) {
				for (int y = 0; y < alturaTela; y++)
					whichList [x][y] = 0;
			}
			naListaFechada = 10;	
		}
		naListaFechada = naListaFechada+2; //alterando os valores da listaAberta(lista aberta) e onClosed list eh mais rapida do que redimensionar whichList() array;
		naListaAberta = naListaFechada-1;
		pathLength [pathfinderID] = naoComecou;//i.e, = 0
		pathLocation [pathfinderID] = naoComecou;//i.e, = 0
		Gcost[startX][startY] = 0; //resetando o quadrado inicial com o valor de G para 0

		//4. Adicionando a posicao inicial listaAberta de quadrados para serem verificados.
		numItemsListaAberta = 1;
		listaAberta[1] = 1;		//colocando este como o item do topo(e atualmente somente) da listaAberta, que eh mantida como uma heap binaria.

		openX[1] = startX;
		openY[1] = startY;

		//5. Faca o seguinte ate que um caminho eh encontrador ou ele nao exista.
		do
		{

			//6. Se a listaAberta não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na listaAberta.
			if (numItemsListaAberta != 0)
			{

				//7. Remova o primeiro item da listaAberta.
				parentXval = openX[listaAberta[1]];
				parentYval = openY[listaAberta[1]]; //Grave as coordenadas da celula do item
				whichList[parentXval][parentYval] = naListaFechada;//adicione o item para a closedList

				//listaAberta = Heap binária: Delete este item da listaAberta, que é mantido como uma heap binária. Para mais informações veja:
				// http://www.policyalmanac.org/games/binaryHeaps.htm
				numItemsListaAberta = numItemsListaAberta - 1;//reduzindo o numero de items da listaAberta em 1	

				// Delete o item do topo na heap binaria, e reordene a heap, com o item de menor custo da função F indo para o topo.
				listaAberta[1] = listaAberta[numItemsListaAberta+1];//mova o ultimo item na heap a cima para o slot #1
				v = 1;

				//Repita o seguinte até que o novo item no slot1 caia para a sua própria posição na heap.
				do
				{
					u = v;		
					if (2*u+1 <= numItemsListaAberta) //Se ambos os filhos existirem
					{
						//Verifique se o custo de F do pai é maior do que cada filho.
						//Selecione o menor dos dois filhos.
						if (Fcost[listaAberta[u]] >= Fcost[listaAberta[2*u]]) 
							v = 2*u;
						if (Fcost[listaAberta[v]] >= Fcost[listaAberta[2*u+1]]) 
							v = 2*u+1;		
					}
					else
					{
						if (2*u <= numItemsListaAberta) //Se somente o filho 1 existe
						{
							//Verifique se o custo de F do pai é maior do que o filho 1.
							if (Fcost[listaAberta[u]] >= Fcost[listaAberta[2*u]]) 
								v = 2*u;
						}
					}

					if (u != v) //Se o custo de F do pai é > do um dos filhos troque eles.
					{
						temp = listaAberta[u];
						listaAberta[u] = listaAberta[v];
						listaAberta[v] = temp;			
					}
					else
						break; //de outro modo, saia do loop

				}
				while (!(Gdx.input.isKeyPressed(Keys.ESCAPE)));  //Tentei isso, mas n�o sei como criar a vari�vel actualkey.

				//7. Verifique os quadrados adjacentes. (Estes "filhos" -- aquele caminho dos filhos são similares,
				//conceitualmente, para a heap binaria mencionada a cima, mas não confuda eles. Eles são diferentes.
				//O caminho dos filhos são descritos no Demo 1 com pontos cinzas a frente dos pais.) Adicione aqueles 
				//quadrados dos filhos adjacens para a listaAberta para posterior consideração se apropriado. (ver vários blocos abaixo).
				for (b = parentYval-1; b <= parentYval+1; b++){
					for (a = parentXval-1; a <= parentXval+1; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != larguraTela && b != alturaTela){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (whichList[a][b] != naListaFechada) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (espacoDeCaminhada [a][b] != caminhoBloqueado) { 

									//		Não corte as bordas cruzadas.
									corner = caminhoPassavel;	
									if (a == parentXval-1) 
									{
										if (b == parentYval-1)
										{
											if (espacoDeCaminhada[parentXval-1][parentYval] == caminhoBloqueado || espacoDeCaminhada[parentXval][parentYval-1] == caminhoBloqueado)  corner = caminhoBloqueado;
										}
										else if (b == parentYval+1)
										{
											if (espacoDeCaminhada[parentXval][parentYval+1] == caminhoBloqueado
													|| espacoDeCaminhada[parentXval-1][parentYval] == caminhoBloqueado) 
												corner = caminhoBloqueado; 
										}
									}
									else if (a == parentXval+1)
									{
										if (b == parentYval-1)
										{
											if (espacoDeCaminhada[parentXval][parentYval-1] == caminhoBloqueado 
													|| espacoDeCaminhada[parentXval+1][parentYval] == caminhoBloqueado) 
												corner = caminhoBloqueado;
										}
										else if (b == parentYval+1)
										{
											if (espacoDeCaminhada[parentXval+1][parentYval] == caminhoBloqueado 
													|| espacoDeCaminhada[parentXval][parentYval+1] == caminhoBloqueado)
												corner = caminhoBloqueado; 
										}
									}	
									if (corner == caminhoPassavel) {

										//		Se não já está na listaAberta, adicione este para a listaAberta.			
										if (whichList[a][b] != naListaAberta) 
										{	

											//Cria um item novo na listaAberta na heap binaria.
											novoIDItemListaAberta = novoIDItemListaAberta + 1; //Cada novo item tem um ID unico.
											m = numItemsListaAberta+1;
											listaAberta[m] = novoIDItemListaAberta;// Coloque o novo item da listaAberta(atualmente ID#) na base da heap.
											openX[novoIDItemListaAberta] = a;
											openY[novoIDItemListaAberta] = b;//grave suas coordenadas x e y do novo item

											//Calculando o custo de G
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//custo de ir pelas diagonais dos quadrados;	
											else	
												addedGCost = 10;//custo de ir em não diagonais.		
											Gcost[a][b] = Gcost[parentXval][parentYval] + addedGCost;

											//Calcular os custos H e F e o pai
											Hcost[listaAberta[m]] = AStar.tamanhoPixel*(Math.abs(a - targetX) + Math.abs(b - targetY));
											Fcost[listaAberta[m]] = Gcost[a][b] + Hcost[listaAberta[m]];
											parentX[a][b] = parentXval ; parentY[a][b] = parentYval;	

											//Mover o novo item da listaAberta para o seu pŕoprio lugar na heap binária.
											//Iniciando da base, sucessivamente comparar items pais, 
											//trocando quando necessário até que o item encontre seu lugar na heap.
											//ou borbulhando todos os caminhos para o topo (se este tem o menor custo de F).
											while (m != 1) //Enquanto o item não tem sido borbulhado para o topo(m=1)	
											{
												//Verifique se o custo F do filho é < o custo F do pai. Se for, troque-os.
												if (Fcost[listaAberta[m]] <= Fcost[listaAberta[m/2]])
												{
													temp = listaAberta[m/2];
													listaAberta[m/2] = listaAberta[m];
													listaAberta[m] = temp;
													m = m/2;
												}
												else
													break;
											}
											numItemsListaAberta = numItemsListaAberta+1;//Adicione um para o número de items na heap

											//Troque whichList para mostrar que o novo item está na listaAberta.
											whichList[a][b] = naListaAberta;
										}

										//8.If adjacent cell is already on the open list, check to see if this
										//8. Se a célula adjacente já está na listaAberta, verifique para ver se este
										//		caminho para a aquela célula da posição inicial, é um melhor.
										//		Se for, troque o pai da célula e seus custos G e F.	
										else //Se whichList(a,b) = naListaAberta
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
												//o item está na listaAberta nós precisamos alterar o custo F 
												//gravado no item e sua posição na listaAberta para ter certeza 
												//que nós mantemos uma listaAberta corretamente ordenada.
												for (int x = 1; x <= numItemsListaAberta; x++) //olho para o item na listaAberta
												{
													if (openX[listaAberta[x]] == a && openY[listaAberta[x]] == b) //item encontrado
													{
														Fcost[listaAberta[x]] = Gcost[a][b] + Hcost[listaAberta[x]];//troque o custo F

														//Veja se alterando o bubbles score de F do item a cima da sua localização corrente na heap.
														m = x;
														while (m != 1) //Enquanto o item não foi borbulhado para o topo (m = 1).	
														{
															//Verifique se o filho é < pai. Se for, troque-os.
															if (Fcost[listaAberta[m]] < Fcost[listaAberta[m/2]])
															{
																temp = listaAberta[m/2];
																listaAberta[m/2] = listaAberta[m];
																listaAberta[m] = temp;
																m = m/2;
															}
															else
																break;
														} 
														break; //saia para x = loop
													} //Se openX(listaAberta(x)) = a
												} //For x = 1 To numItemsListaAberta
											}//If tempGcost < Gcost(a,b)

										}//else If whichList(a,b) = naListaAberta	
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
					}//for (a = parentXval-1; a <= parentXval+1; a++){
				}//for (b = parentYval-1; b <= parentYval+1; b++){

			}//if (numItemsListaAberta != 0)

			//9. Se a listaAberta está vazia então não existe um caminho.
			else
			{
				path = naoExiste; break;
			}  

			//Se o alvo é adicionado a listaAberta, então o caminho foi encontrado.
			if (whichList[targetX][targetY] == naListaAberta)
			{
				path = encontrado; break;
			}

		}
		while (true);//Faça o seguinte até que o caminho seja encontrado ou ele não exista.

		//10. Salve o caminho se ele não exista.
		if (path == encontrado)
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
		if (pathStatus[ID] == encontrado)
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
					pathStatus[ID] = naoComecou; 
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
			x = (int) (tamanhoPixel*x);

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
			y = (int) ( tamanhoPixel*y);

		}
		return y;
	}
	
	private Stack<Integer> pilha = new Stack<Integer>();
	
	public int FindPathBuscaCega (int pathfinderID, int startingX, int startingY,
			int targetX, int targetY)
	{
		int naListaAberta=0, parentXval=0, parentYval=0, a=0, b=0, m=0, u=0, v=0, temp=0, corner=0, numItemsListaAberta=0, addedGCost=0, tempGcost = 0, path = 0, tempx, pathX, pathY, cellPosition, novoIDItemListaAberta=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de espacoDeCaminhada.
		int startX = startingX/tamanhoPixel;
		int startY = startingY/tamanhoPixel;	
		targetX = targetX/tamanhoPixel;
		targetY = targetY/tamanhoPixel;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] > 0)
			return encontrado;
		if (startX == targetX && startY == targetY && pathLocation[pathfinderID] == 0)
			return naoExiste;

		//Se o quadrado alvo é caminhoBloqueado(não andável), retorne que o caminho é inexistente.
		if (espacoDeCaminhada[targetX][targetY] == caminhoBloqueado)
		{
			xPath[pathfinderID] = startingX;
			yPath[pathfinderID] = startingY;
			return naoExiste;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (naListaFechada > 1000000) //Resetando whichList ocasionalmente
		{
			for (int x = 0; x < larguraTela; x++) {
				for (int y = 0; y < alturaTela; y++)
					whichList [x][y] = 0;
			}
			naListaFechada = 10;	
		}
		naListaFechada = naListaFechada+2; //alterando os valores da listaAberta(lista aberta) e onClosed list é mais rapida do que redimming whichList() array;
		naListaAberta = naListaFechada-1;
		pathLength [pathfinderID] = naoComecou;//i.e, = 0
		pathLocation [pathfinderID] = naoComecou;//i.e, = 0
		Gcost[startX][startY] = 0; //resetando o quadrado inicial com o valor de G para 0

		//4. Adicionando a posição inicial openList de quadrados para serem verificados.
		pilha.push(1);

		openX[1] = startX;
		openY[1] = startY;

		//5. Faça o seguinte até que um caminho é encontrador ou ele não exista.
		do
		{

			//6. Se a openList não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na openList.
			if (!pilha.isEmpty())
			{

				//7. Remova o primeiro item da openList.
				parentXval = openX[pilha.peek()];
				parentYval = openY[pilha.peek()]; //Grave as coordenadas da celula do item

				//listaAberta = Heap binária: Delete este item da listaAberta, que é mantido como uma heap binária. Para mais informações veja:
				// http://www.policyalmanac.org/games/binaryHeaps.htm
				numItemsListaAberta = numItemsListaAberta - 1;//reduzindo o numero de items da listaAberta em 1	

				// Delete o item do topo na heap binaria, e reordene a heap, com o item de menor custo da função F indo para o topo.
				listaAberta[1] = listaAberta[numItemsListaAberta+1];//mova o ultimo item na heap a cima para o slot #1
				v = 1;

				//Repita o seguinte até que o novo item no slot1 caia para a sua própria posição na heap.
				do
				{
					u = v;		
					if (2*u+1 <= numItemsListaAberta) //Se ambos os filhos existirem
					{
						//Verifique se o custo de F do pai é maior do que cada filho.
						//Selecione o menor dos dois filhos.
						if (Fcost[listaAberta[u]] >= Fcost[listaAberta[2*u]]) 
							v = 2*u;
						if (Fcost[listaAberta[v]] >= Fcost[listaAberta[2*u+1]]) 
							v = 2*u+1;		
					}
					else
					{
						if (2*u <= numItemsListaAberta) //Se somente o filho 1 existe
						{
							//Verifique se o custo de F do pai é maior do que o filho 1.
							if (Fcost[listaAberta[u]] >= Fcost[listaAberta[2*u]]) 
								v = 2*u;
						}
					}

					if (u != v) //Se o custo de F do pai é > do um dos filhos troque eles.
					{
						temp = listaAberta[u];
						listaAberta[u] = listaAberta[v];
						listaAberta[v] = temp;			
					}
					else
						break; //de outro modo, saia do loop

				}
				while (!(Gdx.input.isKeyPressed(Keys.ESCAPE)));  //Tentei isso, mas n�o sei como criar a vari�vel actualkey.

				//7. Verifique os quadrados adjacentes. (Estes "filhos" -- aquele caminho dos filhos são similares,
				//conceitualmente, para a heap binaria mencionada a cima, mas não confuda eles. Eles são diferentes.
				//O caminho dos filhos são descritos no Demo 1 com pontos cinzas a frente dos pais.) Adicione aqueles 
				//quadrados dos filhos adjacens para a listaAberta para posterior consideração se apropriado. (ver vários blocos abaixo).
				for (b = parentYval-1; b <= parentYval+1; b++){
					for (a = parentXval-1; a <= parentXval+1; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != larguraTela && b != alturaTela){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (whichList[a][b] != naListaFechada) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (espacoDeCaminhada [a][b] != caminhoBloqueado) { 

									//		Não corte as bordas cruzadas.
									corner = caminhoPassavel;	
									if (a == parentXval-1) 
									{
										if (b == parentYval-1)
										{
											if (espacoDeCaminhada[parentXval-1][parentYval] == caminhoBloqueado || espacoDeCaminhada[parentXval][parentYval-1] == caminhoBloqueado)  corner = caminhoBloqueado;
										}
										else if (b == parentYval+1)
										{
											if (espacoDeCaminhada[parentXval][parentYval+1] == caminhoBloqueado
													|| espacoDeCaminhada[parentXval-1][parentYval] == caminhoBloqueado) 
												corner = caminhoBloqueado; 
										}
									}
									else if (a == parentXval+1)
									{
										if (b == parentYval-1)
										{
											if (espacoDeCaminhada[parentXval][parentYval-1] == caminhoBloqueado 
													|| espacoDeCaminhada[parentXval+1][parentYval] == caminhoBloqueado) 
												corner = caminhoBloqueado;
										}
										else if (b == parentYval+1)
										{
											if (espacoDeCaminhada[parentXval+1][parentYval] == caminhoBloqueado 
													|| espacoDeCaminhada[parentXval][parentYval+1] == caminhoBloqueado)
												corner = caminhoBloqueado; 
										}
									}	
									if (corner == caminhoPassavel) {

										//		Se não já está na listaAberta, adicione este para a listaAberta.			
										if (whichList[a][b] != naListaAberta) 
										{	

											//Cria um item novo na listaAberta na heap binaria.
											novoIDItemListaAberta = novoIDItemListaAberta + 1; //Cada novo item tem um ID unico.
											m = numItemsListaAberta+1;
											listaAberta[m] = novoIDItemListaAberta;// Coloque o novo item da listaAberta(atualmente ID#) na base da heap.
											openX[novoIDItemListaAberta] = a;
											openY[novoIDItemListaAberta] = b;//grave suas coordenadas x e y do novo item

											//Calculando o custo de G
											if (Math.abs(a-parentXval) == 1 && Math.abs(b-parentYval) == 1)
												addedGCost = 14;//custo de ir pelas diagonais dos quadrados;	
											else	
												addedGCost = 10;//custo de ir em não diagonais.		
											Gcost[a][b] = 1;

											//Calcular os custos H e F e o pai
											Hcost[listaAberta[m]] = 1;
											Fcost[listaAberta[m]] = Gcost[a][b] + Hcost[listaAberta[m]];
											parentX[a][b] = parentXval ; parentY[a][b] = parentYval;	

											//Mover o novo item da listaAberta para o seu pŕoprio lugar na heap binária.
											//Iniciando da base, sucessivamente comparar items pais, 
											//trocando quando necessário até que o item encontre seu lugar na heap.
											//ou borbulhando todos os caminhos para o topo (se este tem o menor custo de F).
											while (m != 1) //Enquanto o item não tem sido borbulhado para o topo(m=1)	
											{
												//Verifique se o custo F do filho é < o custo F do pai. Se for, troque-os.
												if (Fcost[listaAberta[m]] <= Fcost[listaAberta[m/2]])
												{
													temp = listaAberta[m/2];
													listaAberta[m/2] = listaAberta[m];
													listaAberta[m] = temp;
													m = m/2;
												}
												else
													break;
											}
											numItemsListaAberta = numItemsListaAberta+1;//Adicione um para o número de items na heap

											//Troque whichList para mostrar que o novo item está na listaAberta.
											whichList[a][b] = naListaAberta;
										}

										//8.If adjacent cell is already on the open list, check to see if this
										//8. Se a célula adjacente já está na listaAberta, verifique para ver se este
										//		caminho para a aquela célula da posição inicial, é um melhor.
										//		Se for, troque o pai da célula e seus custos G e F.	
										else //Se whichList(a,b) = naListaAberta
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
												//o item está na listaAberta nós precisamos alterar o custo F 
												//gravado no item e sua posição na listaAberta para ter certeza 
												//que nós mantemos uma listaAberta corretamente ordenada.
												for (int x = 1; x <= numItemsListaAberta; x++) //olho para o item na listaAberta
												{
													if (openX[listaAberta[x]] == a && openY[listaAberta[x]] == b) //item encontrado
													{
														Fcost[listaAberta[x]] = Gcost[a][b] + Hcost[listaAberta[x]];//troque o custo F

														//Veja se alterando o bubbles score de F do item a cima da sua localização corrente na heap.
														m = x;
														while (m != 1) //Enquanto o item não foi borbulhado para o topo (m = 1).	
														{
															//Verifique se o filho é < pai. Se for, troque-os.
															if (Fcost[listaAberta[m]] < Fcost[listaAberta[m/2]])
															{
																temp = listaAberta[m/2];
																listaAberta[m/2] = listaAberta[m];
																listaAberta[m] = temp;
																m = m/2;
															}
															else
																break;
														} 
														break; //saia para x = loop
													} //Se openX(listaAberta(x)) = a
												} //For x = 1 To numItemsListaAberta
											}//If tempGcost < Gcost(a,b)

										}//else If whichList(a,b) = naListaAberta	
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
					}//for (a = parentXval-1; a <= parentXval+1; a++){
				}//for (b = parentYval-1; b <= parentYval+1; b++){

			}//if (numItemsListaAberta != 0)

			//9. Se a listaAberta está vazia então não existe um caminho.
			else
			{
				path = naoExiste; break;
			}  

			//Se o alvo é adicionado a listaAberta, então o caminho foi encontrado.
			if (whichList[targetX][targetY] == naListaAberta)
			{
				path = encontrado; break;
			}

		}
		while (true);//Faça o seguinte até que o caminho seja encontrado ou ele não exista.

		//10. Salve o caminho se ele não exista.
		if (path == encontrado)
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