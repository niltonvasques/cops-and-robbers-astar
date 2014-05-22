
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
	public char[][] possibilidadeDeCaminhada = new char[larguraTela][alturaTela];
	public int[] listaAberta = new int[larguraTela*alturaTela+2]; //array de 1 dimensão que segunda uma lista aberta de items
	public int[][] qualLista = new int[larguraTela+1][alturaTela+1];  //array de 2 dimensões usado para gravar  
	// 		se uma célula está na lista aberta ou na lista fechada.
	public int[] xAberta = new int[larguraTela*alturaTela+2]; //array de 1 dimensão armazenando a posição x de um item na lista aberta.
	public int[] yAberta = new int[larguraTela*alturaTela+2]; //array de 1 dimensão armazenando a posição y de um item na lista aberta.
	public int[][] xPai = new int[larguraTela+1][alturaTela+1]; //array de 2 dimensões para armazenar o pai de cada célular x
	public int[][] yPai = new int[larguraTela+1][alturaTela+1]; //array de 2 dimensões para armazenar o pai de cada célula y
	public int[] custoF = new int[larguraTela*alturaTela+2];	//array de 1 dimensão para armazenar o custo F de cada célula.
	public int[][] custoG = new int[larguraTela+1][alturaTela+1]; 	//array de 2 dimensões para armazenar o custo G para cada célula
	public int[] custoH = new int[larguraTela*alturaTela+2];	//array de 1 dimensão para armazenar o custo H de cada célula na lista aberta
	public int[] tamanhoDoCaminho = new int[qtdeAgentes+1];     //armazena o tamanho do caminho encontrado para a criatura
	public int[] localizacaoDoCaminho = new int[qtdeAgentes+1];   //armazena a posição atual ao longo do caminho escolhido para a criatura		
	//int* arquivaCaminho [qtdeAgentes+1];
	public List<Integer> arquivaCaminho[] = new List[qtdeAgentes+1];
	public List<Integer[]> arquivaCaminho2 = new ArrayList<Integer[]>();

	//Path reading variables
	public int[] statusDoCaminho = new int[qtdeAgentes+1];
	public int[] caminhoX = new int[qtdeAgentes+1];
	public int[] caminhoY = new int[qtdeAgentes+1];

	public void inicializarBuscadorDeCaminho()
	{
		for (int x = 0; x < qtdeAgentes+1; x++){
			arquivaCaminho[x] = new Vector<Integer>();
			arquivaCaminho2.add(new Integer[4]);
		}
	}

	public void  finalizarBuscadorDeCaminho()
	{
		for (int x = 0; x < qtdeAgentes+1; x++){
			arquivaCaminho[x].clear();
			arquivaCaminho2.set(x, null);
		}
	}

	public int encontrarCaminho(int caminhoBuscadorID, int origemX, int origemY,
			int destinoX, int destinoY)
	{
		int naListaAberta=0, valorXPai=0, valorYPai=0, a=0, b=0, m=0, u=0, v=0, temp=0, diagonal=0, numItemsListaAberta=0, adicionadoCustoG=0, custoGTemp = 0, path = 0, tempx, tragetoriaX, tragetoriaY, posicaoDaCelula, novoIDItemListaAberta=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de espacoDeCaminhada.
		int inicioX = origemX/tamanhoPixel;
		int inicioY = origemY/tamanhoPixel;	
		destinoX = destinoX/tamanhoPixel;
		destinoY = destinoY/tamanhoPixel;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (inicioX == destinoX && inicioY == destinoY && localizacaoDoCaminho[caminhoBuscadorID] > 0)
			return encontrado;
		if (inicioX == destinoX && inicioY == destinoY && localizacaoDoCaminho[caminhoBuscadorID] == 0)
			return naoExiste;

		//Se o quadrado alvo é caminhoBloqueado(não andável), retorne que o caminho é inexistente.
		if (possibilidadeDeCaminhada[destinoX][destinoY] == caminhoBloqueado)
		{
			caminhoX[caminhoBuscadorID] = origemX;
			caminhoY[caminhoBuscadorID] = origemY;
			return naoExiste;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (naListaFechada > 1000000) //Resetando queLista ocasionalmente
		{
			for (int x = 0; x < larguraTela; x++) {
				for (int y = 0; y < alturaTela; y++)
					qualLista [x][y] = 0;
			}
			naListaFechada = 10;	
		}
		naListaFechada = naListaFechada+2; //alterando os valores da listaAberta(lista aberta) e onClosed list eh mais rapida do que redimensionar queLista() array;
		naListaAberta = naListaFechada-1;
		tamanhoDoCaminho [caminhoBuscadorID] = naoComecou;//i.e, = 0
		localizacaoDoCaminho [caminhoBuscadorID] = naoComecou;//i.e, = 0
		custoG[inicioX][inicioY] = 0; //resetando o quadrado inicial com o valor de G para 0

		//4. Adicionando a posicao inicial listaAberta de quadrados para serem verificados.
		numItemsListaAberta = 1;
		listaAberta[1] = 1;		//colocando este como o item do topo(e atualmente somente) da listaAberta, que eh mantida como uma heap binaria.

		xAberta[1] = inicioX;
		yAberta[1] = inicioY;

		//5. Faca o seguinte ate que um caminho eh encontrador ou ele nao exista.
		do
		{

			//6. Se a listaAberta não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na listaAberta.
			if (numItemsListaAberta != 0)
			{

				//7. Remova o primeiro item da listaAberta.
				valorXPai = xAberta[listaAberta[1]];
				valorYPai = yAberta[listaAberta[1]]; //Grave as coordenadas da celula do item
				qualLista[valorXPai][valorYPai] = naListaFechada;//adicione o item para a closedList

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
						if (custoF[listaAberta[u]] >= custoF[listaAberta[2*u]]) 
							v = 2*u;
						if (custoF[listaAberta[v]] >= custoF[listaAberta[2*u+1]]) 
							v = 2*u+1;		
					}
					else
					{
						if (2*u <= numItemsListaAberta) //Se somente o filho 1 existe
						{
							//Verifique se o custo de F do pai é maior do que o filho 1.
							if (custoF[listaAberta[u]] >= custoF[listaAberta[2*u]]) 
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
				for (b = valorYPai-1; b <= valorYPai+1; b++){
					for (a = valorXPai-1; a <= valorXPai+1; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != larguraTela && b != alturaTela){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (qualLista[a][b] != naListaFechada) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (possibilidadeDeCaminhada [a][b] != caminhoBloqueado) { 

									//		Não corte as bordas cruzadas.
									diagonal = caminhoPassavel;	
									if (a == valorXPai-1) 
									{
										if (b == valorYPai-1)
										{
											if (possibilidadeDeCaminhada[valorXPai-1][valorYPai] == caminhoBloqueado || possibilidadeDeCaminhada[valorXPai][valorYPai-1] == caminhoBloqueado)  diagonal = caminhoBloqueado;
										}
										else if (b == valorYPai+1)
										{
											if (possibilidadeDeCaminhada[valorXPai][valorYPai+1] == caminhoBloqueado
													|| possibilidadeDeCaminhada[valorXPai-1][valorYPai] == caminhoBloqueado) 
												diagonal = caminhoBloqueado; 
										}
									}
									else if (a == valorXPai+1)
									{
										if (b == valorYPai-1)
										{
											if (possibilidadeDeCaminhada[valorXPai][valorYPai-1] == caminhoBloqueado 
													|| possibilidadeDeCaminhada[valorXPai+1][valorYPai] == caminhoBloqueado) 
												diagonal = caminhoBloqueado;
										}
										else if (b == valorYPai+1)
										{
											if (possibilidadeDeCaminhada[valorXPai+1][valorYPai] == caminhoBloqueado 
													|| possibilidadeDeCaminhada[valorXPai][valorYPai+1] == caminhoBloqueado)
												diagonal = caminhoBloqueado; 
										}
									}	
									if (diagonal == caminhoPassavel) {

										//		Se não já está na listaAberta, adicione este para a listaAberta.			
										if (qualLista[a][b] != naListaAberta) 
										{	

											//Cria um item novo na listaAberta na heap binaria.
											novoIDItemListaAberta = novoIDItemListaAberta + 1; //Cada novo item tem um ID unico.
											m = numItemsListaAberta+1;
											listaAberta[m] = novoIDItemListaAberta;// Coloque o novo item da listaAberta(atualmente ID#) na base da heap.
											xAberta[novoIDItemListaAberta] = a;
											yAberta[novoIDItemListaAberta] = b;//grave suas coordenadas x e y do novo item

											//Calculando o custo de G
											if (Math.abs(a-valorXPai) == 1 && Math.abs(b-valorYPai) == 1)
												adicionadoCustoG = 14;//custo de ir pelas diagonais dos quadrados;	
											else	
												adicionadoCustoG = 10;//custo de ir em não diagonais.		
											custoG[a][b] = custoG[valorXPai][valorYPai] + adicionadoCustoG;

											//Calcular os custos H e F e o pai
											custoH[listaAberta[m]] = AStar.tamanhoPixel*(Math.abs(a - destinoX) + Math.abs(b - destinoY));
											custoF[listaAberta[m]] = custoG[a][b] + custoH[listaAberta[m]];
											xPai[a][b] = valorXPai ; yPai[a][b] = valorYPai;	

											//Mover o novo item da listaAberta para o seu pŕoprio lugar na heap binária.
											//Iniciando da base, sucessivamente comparar items pais, 
											//trocando quando necessário até que o item encontre seu lugar na heap.
											//ou borbulhando todos os caminhos para o topo (se este tem o menor custo de F).
											while (m != 1) //Enquanto o item não tem sido borbulhado para o topo(m=1)	
											{
												//Verifique se o custo F do filho é < o custo F do pai. Se for, troque-os.
												if (custoF[listaAberta[m]] <= custoF[listaAberta[m/2]])
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

											//Troque queLista para mostrar que o novo item está na listaAberta.
											qualLista[a][b] = naListaAberta;
										}

										//8.If adjacent cell is already on the open list, check to see if this
										//8. Se a célula adjacente já está na listaAberta, verifique para ver se este
										//		caminho para a aquela célula da posição inicial, é um melhor.
										//		Se for, troque o pai da célula e seus custos G e F.	
										else //Se queLista(a,b) = naListaAberta
										{

											//Calcular o custo G deste possível caminho novo.
											if (Math.abs(a-valorXPai) == 1 && Math.abs(b-valorYPai) == 1)
												adicionadoCustoG = 14;//Custo de ir pelas diagonais	
											else	
												adicionadoCustoG = 10;//Custo de ir por não diagonais.				
											custoGTemp = custoG[valorXPai][valorYPai] + adicionadoCustoG;

											//Se este caminho é curto ( custo de G é baixo) então troque
											//a célula pai, custo de G e custo de F.
											if (custoGTemp < custoG[a][b]) //Se o custo de G é menor,
											{
												xPai[a][b] = valorXPai; //troque o quadrado pai
												yPai[a][b] = valorYPai;
												custoG[a][b] = custoGTemp;//troque o custo de G			

												//Porque trocando o custo de G também muda o custo de F, se 
												//o item está na listaAberta nós precisamos alterar o custo F 
												//gravado no item e sua posição na listaAberta para ter certeza 
												//que nós mantemos uma listaAberta corretamente ordenada.
												for (int x = 1; x <= numItemsListaAberta; x++) //olho para o item na listaAberta
												{
													if (xAberta[listaAberta[x]] == a && yAberta[listaAberta[x]] == b) //item encontrado
													{
														custoF[listaAberta[x]] = custoG[a][b] + custoH[listaAberta[x]];//troque o custo F

														//Veja se alterando o bubbles score de F do item a cima da sua localização corrente na heap.
														m = x;
														while (m != 1) //Enquanto o item não foi borbulhado para o topo (m = 1).	
														{
															//Verifique se o filho é < pai. Se for, troque-os.
															if (custoF[listaAberta[m]] < custoF[listaAberta[m/2]])
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
													} //Se xAberta(listaAberta(x)) = a
												} //For x = 1 To numItemsListaAberta
											}//If custoGTemp < custoG(a,b)

										}//else If queLista(a,b) = naListaAberta	
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
					}//for (a = valorXPai-1; a <= valorXPai+1; a++){
				}//for (b = valorYPai-1; b <= valorYPai+1; b++){

			}//if (numItemsListaAberta != 0)

			//9. Se a listaAberta está vazia então não existe um caminho.
			else
			{
				path = naoExiste; break;
			}  

			//Se o alvo é adicionado a listaAberta, então o caminho foi encontrado.
			if (qualLista[destinoX][destinoY] == naListaAberta)
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
			tragetoriaX = destinoX; tragetoriaY = destinoY;
			do
			{
				//Visitar o pai da célula corrente.
				tempx = xPai[tragetoriaX][tragetoriaY];		
				tragetoriaY = yPai[tragetoriaX][tragetoriaY];
				tragetoriaX = tempx;

				//Calcular o tamanho do caminho.
				tamanhoDoCaminho[caminhoBuscadorID] = tamanhoDoCaminho[caminhoBuscadorID] + 1;
			}
			while (tragetoriaX != inicioX || tragetoriaY != inicioY);

			//b.Redimensione o dataBank para o correto tamanho em bytes.
			Integer[] arr = arquivaCaminho2.get(caminhoBuscadorID);
			arquivaCaminho2.set(caminhoBuscadorID, Arrays.copyOf(arr, tamanhoDoCaminho[caminhoBuscadorID]*8));

			//c. Agora copie as informações do caminho sobre o databank. Desde que 
			//		nós estamos trabahando atrás do alvo para da posição inicial, nós
			//		copiamos a informação para o data bank na ordem reversa. O resultado é
			//		um conjunto corretamente ordenado de dados do caminho, para o primeiro passo
			//		até o último.
			tragetoriaX = destinoX ; tragetoriaY = destinoY;
			posicaoDaCelula = tamanhoDoCaminho[caminhoBuscadorID]*2;//Inicie do final	
			do
			{
				posicaoDaCelula = posicaoDaCelula - 2;//trabalhe 2 inteiros para trás
				arquivaCaminho2.get(caminhoBuscadorID)[posicaoDaCelula] = tragetoriaX;
				arquivaCaminho2.get(caminhoBuscadorID)[posicaoDaCelula+1] = tragetoriaY;
	
				//d. Visite o pai da célula atual.
				tempx = xPai[tragetoriaX][tragetoriaY];		
				tragetoriaY = yPai[tragetoriaX][tragetoriaY];
				tragetoriaX = tempx;

				//e. Se nós temos encontrado o quadrado incial, saia do loop.
			}
			while (tragetoriaX != inicioX || tragetoriaY != inicioY);	

			//11. Leia o primeiro passo dentro dos arrays caminhoX/caminhoY. 
			leituraDoCaminho(caminhoBuscadorID,origemX,origemY,1);

		}
		return path;
	}

	public void leituraDoCaminho(int caminhoBuscadorID,int correnteX,int correnteY, int pixelPorFrame)
	{
		int ID = caminhoBuscadorID; //redundante, mas faz que o seguinte seja mais fácil para ler.
		//Se um caminho tem sido encontrado para o pathfinder ...
		if (statusDoCaminho[ID] == encontrado)
		{

			//Se path finder está apenas iniciando um novo vaminho ou tenha alcançado o
			//centro do caminho atual ( e o final do caminho não tenha sido encontrado), visite o próximo quadrado do caminho.
			if (localizacaoDoCaminho[ID] < tamanhoDoCaminho[ID])
			{
				//Se apenas começando ou se está fechado para o centro do quadrado
				if (localizacaoDoCaminho[ID] == 0 || 
						(Math.abs(correnteX - caminhoX[ID]) < pixelPorFrame && Math.abs(correnteY - caminhoY[ID]) < pixelPorFrame))
					localizacaoDoCaminho[ID] = localizacaoDoCaminho[ID] + 1;
			}

			//Leia o dado do caminho.
			caminhoX[ID] = leituraCaminhoX(ID,localizacaoDoCaminho[ID]);
			caminhoY[ID] = leituraCaminhoY(ID,localizacaoDoCaminho[ID]);

			//Se o centro do último quadrado caminho no caminho tem sido alcançado resete-o.
			if (localizacaoDoCaminho[ID] == tamanhoDoCaminho[ID]) 
			{
				if (Math.abs(correnteX - caminhoX[ID]) < pixelPorFrame 
						&& Math.abs(correnteY - caminhoY[ID]) < pixelPorFrame) //Se perto o suficiente do quadrado do centro
					statusDoCaminho[ID] = naoComecou; 
			}
		}

		//Se não tem caminho para este pathfinder, simplismente fique na posição inicial.
		else
		{	
			caminhoX[ID] = correnteX;
			caminhoY[ID] = correnteY;
		}
	}

	//The following two functions read the raw path data from the arquivaCaminho.
	//You can call these functions directly and skip the readPath function
	//above if you want. Make sure you know what your current localizacaoDoCaminho
	//is.
	
	//As seguintes duas funções leem um raw path data do path arquivaCaminho.
	//Você pode chamar estas funções diretamente e pular a função readPath
	// a cima se você quiser. Tenha certeza que você sabe qual é a sua posição
	//atual.

	//-----------------------------------------------------------------------------
	// Name: ReadPathX
	// Desc: Le a coordenada x do próximo passo do caminho
	//-----------------------------------------------------------------------------
	int leituraCaminhoX(int caminhoBuscadorID,int localizacaoDoCaminho)
	{
		int x = 0;
		if (localizacaoDoCaminho <= tamanhoDoCaminho[caminhoBuscadorID])
		{

			//Le a coordenada X do patharquivaCaminho
			x = arquivaCaminho2.get(caminhoBuscadorID)[localizacaoDoCaminho*2-2];

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
	int leituraCaminhoY(int caminhoBuscadorID,int localizacaoDoCaminho)
	{
		int y = 0;
		if (localizacaoDoCaminho <= tamanhoDoCaminho[caminhoBuscadorID])
		{

			//Le as coordenadas do arquivaCaminho.
			//		y = arquivaCaminho[pathfinderID].get(localizacaoDoCaminho*2-1);
			y = arquivaCaminho2.get(caminhoBuscadorID)[localizacaoDoCaminho*2-1];

			//Ajusta a coordenada para ela ficar alinhada ao inicio do quadrado. 
			//Assumindo que estamos usando sprites que não são centralizados..
			y = (int) ( tamanhoPixel*y);

		}
		return y;
	}
	
	private Stack<Integer> pilha = new Stack<Integer>();
	
	public int encontrarCaminhoBuscaCega(int caminhoBuscadorID, int origemX, int origemY,
			int destinoX, int destinoY)
	{
		int naListaAberta=0, valorXPai=0, valorYPai=0, a=0, b=0, m=0, u=0, v=0, temp=0, diagonal=0, numItemsListaAberta=0, adicionadoCustoG=0, custoGTemp = 0, caminho = 0, tempx, tragetoriaX, tragetoriaY, posicaoDaCelula, novoIDItemListaAberta=0;

		//1. Converta os dados da localização ( em pixels ) para as cordenadas do array de espacoDeCaminhada.
		int inicioX = origemX/tamanhoPixel;
		int inicioY = origemY/tamanhoPixel;	
		destinoX = destinoX/tamanhoPixel;
		destinoY = destinoY/tamanhoPixel;

		//2. Caminhos rápidos: Sobre certas circunstâncias, nenhum caminho é necessário.

		// Se a posição inicial e o destino estão na mesmo lugar
		if (inicioX == destinoX && inicioY == destinoY && localizacaoDoCaminho[caminhoBuscadorID] > 0)
			return encontrado;
		if (inicioX == destinoX && inicioY == destinoY && localizacaoDoCaminho[caminhoBuscadorID] == 0)
			return naoExiste;

		//Se o quadrado alvo é caminhoBloqueado(não andável), retorne que o caminho é inexistente.
		if (possibilidadeDeCaminhada[destinoX][destinoY] == caminhoBloqueado)
		{
			caminhoX[caminhoBuscadorID] = origemX;
			caminhoY[caminhoBuscadorID] = origemY;
			return naoExiste;
		}

		//3. Resetando algumas variáveis que precisam ser limpas
		if (naListaFechada > 1000000) //Resetando queLista ocasionalmente
		{
			for (int x = 0; x < larguraTela; x++) {
				for (int y = 0; y < alturaTela; y++)
					qualLista [x][y] = 0;
			}
			naListaFechada = 10;	
		}
		naListaFechada = naListaFechada+2; //alterando os valores da listaAberta(lista aberta) e onClosed list é mais rapida do que redimming queLista() array;
		naListaAberta = naListaFechada-1;
		tamanhoDoCaminho [caminhoBuscadorID] = naoComecou;//i.e, = 0
		localizacaoDoCaminho [caminhoBuscadorID] = naoComecou;//i.e, = 0

		//4. Adicionando a posição inicial listaAberta de quadrados para serem verificados.
		pilha.push(1);

		xAberta[1] = inicioX;
		yAberta[1] = inicioY;

		//5. Faça o seguinte até que um caminho é encontrador ou ele não exista.
		do
		{

			//6. Se a listaAberta não é vazia, pegue a primeira célula da lista.
			//	Esta possui o menor custo da função F na listaAberta.
			if (!pilha.isEmpty())
			{

				//7. Remova o primeiro item da listaAberta.
				valorXPai = xAberta[listaAberta[1]];
				valorYPai = yAberta[listaAberta[1]]; //Grave as coordenadas da celula do item

				//7. Verifique os quadrados adjacentes. (Estes "filhos" -- aquele caminho dos filhos são similares,
				//conceitualmente, para a heap binaria mencionada a cima, mas não confuda eles. Eles são diferentes.
				//O caminho dos filhos são descritos no Demo 1 com pontos cinzas a frente dos pais.) Adicione aqueles 
				//quadrados dos filhos adjacens para a listaAberta para posterior consideração se apropriado. (ver vários blocos abaixo).
				boolean vizinhoDisponivel = false;
				for (b = valorYPai-1; b <= valorYPai+1 && !vizinhoDisponivel; b++){
					for (a = valorXPai-1; a <= valorXPai+1 && !vizinhoDisponivel; a++){

						// Se não sair do mapa (faça isto primeiro para evitar erros de ArrayIndexOutOfBounds).
						if (a != -1 && b != -1 && a != larguraTela && b != alturaTela){

							//		Se não já está no closedList (items na closedList são items que já foram considerados e podem ser ignorados).
							if (qualLista[a][b] != naListaFechada) { 

								// 		Se não é um quadrado parede/obstaculo;
								if (possibilidadeDeCaminhada [a][b] != caminhoBloqueado) { 

									//		Não corte as bordas cruzadas.
									diagonal = caminhoPassavel;	
									if (a == valorXPai-1) 
									{
										if (b == valorYPai-1)
										{
											if (possibilidadeDeCaminhada[valorXPai-1][valorYPai] == caminhoBloqueado || possibilidadeDeCaminhada[valorXPai][valorYPai-1] == caminhoBloqueado)  diagonal = caminhoBloqueado;
										}
										else if (b == valorYPai+1)
										{
											if (possibilidadeDeCaminhada[valorXPai][valorYPai+1] == caminhoBloqueado
													|| possibilidadeDeCaminhada[valorXPai-1][valorYPai] == caminhoBloqueado) 
												diagonal = caminhoBloqueado; 
										}
									}
									else if (a == valorXPai+1)
									{
										if (b == valorYPai-1)
										{
											if (possibilidadeDeCaminhada[valorXPai][valorYPai-1] == caminhoBloqueado 
													|| possibilidadeDeCaminhada[valorXPai+1][valorYPai] == caminhoBloqueado) 
												diagonal = caminhoBloqueado;
										}
										else if (b == valorYPai+1)
										{
											if (possibilidadeDeCaminhada[valorXPai+1][valorYPai] == caminhoBloqueado 
													|| possibilidadeDeCaminhada[valorXPai][valorYPai+1] == caminhoBloqueado)
												diagonal = caminhoBloqueado; 
										}
									}	
									if (diagonal == caminhoPassavel) {

										//		Se não já está na listaAberta, adicione este para a listaAberta.			
										if (qualLista[a][b] != naListaAberta) 
										{	

 											//Cria um item novo na listaAberta na heap binaria.

 											novoIDItemListaAberta = novoIDItemListaAberta + 1; //Cada novo item tem um ID unico.
 											
 											pilha.push(novoIDItemListaAberta);

											listaAberta[m] = novoIDItemListaAberta;// Coloque o novo item da listaAberta(atualmente ID#) na base da heap.

 											xAberta[novoIDItemListaAberta] = a;
 											yAberta[novoIDItemListaAberta] = b;//grave suas coordenadas x e y do novo item
 											
 											xPai[a][b] = valorXPai; 
 											yPai[a][b] = valorYPai;
 											
 											//Troque whichList para mostrar que o novo item está na listaAberta.
 											qualLista[a][b] = naListaAberta;
 											
											vizinhoDisponivel = true;
										}
										
									}//If não cortando um canto
								}//If não um quadrado parede.
							}//If não já está na closedList 
						}//If não está fora do mapa.
						if( a == valorXPai+1 && b == valorYPai+1 && !vizinhoDisponivel){
							int item = pilha.pop();
							qualLista[valorXPai][valorYPai] = naListaFechada;
						}
					}//for (a = valorXPai-1; a <= valorXPai+1; a++){
				}//for (b = valorYPai-1; b <= valorYPai+1; b++){

			}//if (numItemsListaAberta != 0)

			//9. Se a listaAberta está vazia então não existe um caminho.
			else
			{
				caminho = naoExiste; break;
			}  

			//Se o alvo é adicionado a listaAberta, então o caminho foi encontrado.
			if (qualLista[destinoX][destinoY] == naListaAberta)
			{
				caminho = encontrado; break;
			}

		}
		while (true);//Faça o seguinte até que o caminho seja encontrado ou ele não exista.

		//10. Salve o caminho se ele não exista.
		if (caminho == encontrado)
		{

			tamanhoDoCaminho[caminhoBuscadorID] = pilha.size();

			//b.Redimensione o dataBank para o correto tamanho em bytes.
			Integer[] arr = arquivaCaminho2.get(caminhoBuscadorID);
			arquivaCaminho2.set(caminhoBuscadorID, Arrays.copyOf(arr, tamanhoDoCaminho[caminhoBuscadorID]*8));

			//c. Agora copie as informações do caminho sobre o databank. Desde que 
			//		nós estamos trabahando atrás do alvo para da posição inicial, nós
			//		copiamos a informação para o data bank na ordem reversa. O resultado é
			//		um conjunto corretamente ordenado de dados do caminho, para o primeiro passo
			//		até o último.
			int itemID = pilha.pop();
			tragetoriaX = xAberta[itemID]; 
			tragetoriaY = yAberta[itemID];
			posicaoDaCelula = tamanhoDoCaminho[caminhoBuscadorID]*2;//Inicie do final	
			do
			{
				posicaoDaCelula = posicaoDaCelula - 2;//trabalhe 2 inteiros para trás
				arquivaCaminho2.get(caminhoBuscadorID)[posicaoDaCelula] = tragetoriaX;
				arquivaCaminho2.get(caminhoBuscadorID)[posicaoDaCelula+1] = tragetoriaY;
	
				//d. Visite o pai da célula atual.
				itemID = pilha.pop();
				tragetoriaX = xAberta[itemID]; 
				tragetoriaY = yAberta[itemID];
				
//				tempx = xPai[tragetoriaX][tragetoriaY];		
//				tragetoriaY = yPai[tragetoriaX][tragetoriaY];
//				tragetoriaX = tempx;

				//e. Se nós temos encontrado o quadrado incial, saia do loop.
			}
			while(!pilha.isEmpty());
//			while (tragetoriaX != inicioX || tragetoriaY != inicioY);	

			//11. Leia o primeiro passo dentro dos arrays caminhoX/caminhoY. 
			leituraDoCaminho(caminhoBuscadorID,origemX,origemY,1);

		}
		return caminho;
	}

}