#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<time.h>

main()
{
	//Menu
	int ini;
	printf("BEM VINDO AO JOGO DA MEMORIA EM C\n\n\n\n\n");
	system("pause");
	system("cls");
	printf("----------------------------------------------\n");	
	do
	{
	
		printf("Escolha o tamanho da tabela de 4-6:");
		scanf("%i", &ini);
		printf("----------------------------------------------\n");
		if(ini<4 || ini>6)
		{
			printf("Desculpe valor invalido.\n\n");
			system("pause");
			system("cls");
		}
	}
	while(ini<4 || ini>6);
	system("pause");
	system("cls");
	printf("----------------------------------------------\n");
	printf("Instrucoes:\n");
	printf("Voce tera que escolher uma das coordenadas\nde 0-9 na vertical e depois na horizontal.\nA escolha da coordenada segue o este modelo\n(vertical-horizontal). Exemplo (0-1).\n\n");
	printf("\n----------------------------------------------\n\n\n");
	system("pause");
	system("cls");
	
	
	int i,j,v1,v2,v3,v4,vida=18,pontos=0,k;
	int tabela[ini][ini];
	int valores[ini][ini];
	int resposta[ini][ini];
	int coordenada_1[2][2];
	int coordenada_2[1][1];
	
	//Dados da tabela
	srand(time(NULL));
	for(i=0;i<ini;i++)
	{
		
		for(j=0;j<ini;j++)
		{	
			valores[i][j]=1+rand()%6;
		}
	}
	
	//tabela inicial
	
	for(i=0;i<ini;i++)
	{
		
		printf("\t%i",i);
	}
	printf("\n\n");
	for(i=0;i<ini;i++)
	{
		printf("%i\t",i);
		for(j=0;j<ini;j++)
		{
			printf("%i\t", valores[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	
	//tabela limpa
	
	for(i=0;i<ini;i++)
	{
		
		printf("\t%i",i);
	}
	printf("\n\n");
	for(i=0;i<ini;i++)
	{
		printf("%i\t",i);
		for(j=0;j<ini;j++)
		{
			tabela[i][j]=0;
			printf("%i\t", tabela[i][j]);
		}
		printf("\n");
	}
	
	//Jogada
	
	do
	{
		//Dados
		printf("Vidas: %i\n",vida);
		printf("Pontos: %i\n",pontos);
		
		//Recebimento das coordenadas
		
			printf("--------------------------------------------------");
			printf("\nDigite o 1o valor da coluna e Da linha(C-L): ");
			scanf("%i-%i", &v1,&v2);
			printf("--------------------------------------------------");
			printf("\nDigite o 2o valor da coluna e Da linha(C-L): ");
			scanf("%i-%i", &v3,&v4);
			
			if(tabela[v1][v2]!=0)
			{
				printf("valor ja escolhido\n");
			}
			
			
			coordenada_1[v1][v2]=valores[v1][v2];
			coordenada_2[v3][v4]=valores[v3][v4];		
			system("pause");
			system("cls");
			
		//pontuação
		
		if(tabela[v1][v2]==0)
		{
					
			if(coordenada_1[v1][v2]!=coordenada_2[v3][v4])
			{
				vida--;
			}
			else
			{
	
				pontos++;
			}
		}
	
		//inserindo valores na tabela
	
	
		for(i=0;i<ini;i++)
		{
			
			printf("\t%i",i);
		}
		printf("\n\n");
		for(i=0;i<ini;i++)
		{
			printf("%i\t",i);
			for(j=0;j<ini;j++)
			{
				
				tabela[v1][v2]=valores[v1][v2];
				tabela[v3][v4]=valores[v3][v4];
				printf("%i\t",tabela[i][j]);
		
			}
			printf("\n");
		}
		
		
		
		//saida da vitoria ou da derrota
		if(vida==0)
		{
			break;
		}
		else if(pontos==18)
		{
			break;
		}

	}
	while(vida!=0 || pontos!=18);
	
	//vitoria
	if(vida==0)
	{
		printf("voce perdeu! Tente novamente");
	}
	else if(pontos==18)
	{
		printf("VOCE VENCEU!!!");
	}

}
