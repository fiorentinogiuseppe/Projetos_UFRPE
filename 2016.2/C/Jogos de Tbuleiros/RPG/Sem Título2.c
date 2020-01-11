#include<stdio.h>
#include<stdlib.h>
#include<time.h>
#include<string.h>

typedef struct
{
	char nome[10];
	float defesa[4];
	float ataque[4]; 
	float vida[4];
	
}Tinfo;


main()
{
	srand(time(NULL));
	//jogador 1
	Tinfo jogador1[5];

	int i;
	int dado1,dado2;
	int vitoria,rodada;


	for(i=1;i<=4;i++)
	{
		//nome
		printf("Digite o nome do personagem %i do jogador 1: ", i);
		gets(jogador1[i].nome);
		system("cls");
		//Vida
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].vida[i]=100;
		//attaque
		
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].ataque[i]=dado1+dado2+6;
		//defesa
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].defesa[i]=dado1+dado2+12;
		printf("=====================================================================\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Vida do jogador %s: %.0f\n",jogador1[i].nome, jogador1[i].vida[i]);
		printf("---------------------------------------------------------------------\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Ataque do jogador %s: %.0f\n",jogador1[i].nome, jogador1[i].ataque[i]);
		printf("---------------------------------------------------------------------\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Defesa do jogador %s: %.0f\n",jogador1[i].nome, jogador1[i].defesa[i]);
		printf("=====================================================================\n");
		system("pause");
		system("cls");

	}
	//jogador 2
	Tinfo jogador2[5];

	
	for(i=1;i<=4;i++)
	{
		//nome
		printf("Digite o nome do personagem %i do jogador 2: ", i);
		gets(jogador2[i].nome);
		system("cls");
		//Vida
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador2[i].vida[i]=100;
		//attaque
		
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador2[i].ataque[i]=dado1+dado2+6;
		//defesa
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador2[i].defesa[i]=dado1+dado2+12;
		printf("=====================================================================\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Vida do jogador %s: %.0f\n",jogador2[i].nome, jogador2[i].vida[i]);
		printf("---------------------------------------------------------------------\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Ataque do jogador %s: %.0f\n",jogador2[i].nome, jogador2[i].ataque[i]);
		printf("---------------------------------------------------------------------\n");
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Defesa do jogador %s: %.0f\n",jogador2[i].nome, jogador2[i].defesa[i]);
		printf("=====================================================================\n");
		system("pause");
		system("cls");

	}
	
	//inicio
	int dadou[2];
	//jogador1
	dado1=1+rand()%6;
	dado2=1+rand()%6;
	dadou[0]=dado1+dado2;
	//jogaor2
	dado1=1+rand()%6;
	dado2=1+rand()%6;
	dadou[1]=dado1+dado2;
	
	int primeiro,  segundo;
	if(dadou[0]>dadou[1])
	{
		primeiro=1;
		segundo=2;	
	}
	else if(dadou[1]>dadou[0])
	{
		
	}
	//luta
		int atacante,defesa;
		float precisao_do_lutador_1,precisao_do_lutador_2,dadoa;
		float aleatorio;
		int verifi[5];
		char saida;
		int j;
		int contador=1;
		
	
	do
	{
		//ataque jogador 1
		system("pause");
		system("cls");
		printf("\n\nJogador 1!\n\n");
		system("pause");
		system("cls");
		
		for(i=1;i<=4;i++)
		{
			verifi[i]=0;
		}
		
		for(i=1;i<=4;i++)
		{
			atacante=i;
	
			do
			{
				defesa=1+rand()%4;
	
				if(jogador2[i].vida[i]==0)
				{
					defesa=1+rand()%4;

				}
				else
				{
					if(verifi[defesa]==0)
					{
						verifi[defesa]=1;
						break;
					}
					else
					{
						defesa=1+rand()%4;
					}
				}
				
			}while(saida!='okay');
	
				//precisao
				precisao_do_lutador_1 = 1 - (((jogador1[atacante].vida[atacante]) * (jogador1[atacante].ataque[atacante]))/1000);
				aleatorio=0+rand()%1;
				printf("aleatorio: %.4f\n",aleatorio);
				printf("Precisao: %.4f\n",precisao_do_lutador_1);
				if(aleatorio>precisao_do_lutador_1)
				{
					printf("Ataque de %s do jogador 1 em %s do jogador 2\n", jogador1[atacante].nome,jogador2[defesa].nome);
					jogador2[defesa].vida[defesa]=jogador2[defesa].vida[defesa] - (jogador2[defesa].ataque[defesa])/ (jogador2[defesa].defesa[defesa]/10);
					printf("Vida jogador 2: %.3f\nVida jogador 1: %.3f\n",jogador2[defesa].vida[defesa], jogador1[atacante].vida[atacante]);
				}
				else
				{
					printf(" %s do jogador 1 errooww o %s do jogador 2\n", jogador1[atacante].nome,jogador2[defesa].nome);
					printf("Vida jogador 2: %.3f\nVida jogador 1: %.3f\n",jogador2[defesa].vida[defesa], jogador1[atacante].vida[atacante]);
				}
		}
		
		system("pause");
		system("cls");
		printf("\n\nJogador 2!\n\n");
		system("pause");
		system("cls");
		
	}
	while(rodada==0 );
	
	if(vitoria==2)
	{
		printf("Jogador 1 morreu\nJogador 2 venceu!!");
	}
	else if(vitoria==1)
	{
		printf("Jogador 2 morreu\nJogador 1 Venceuu!!");
	}

}
