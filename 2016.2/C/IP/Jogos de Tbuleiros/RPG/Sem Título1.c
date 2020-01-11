#include<stdio.h>
#include<stdlib.h>
#include<time.h>
#include<string.h>

typedef struct
{
	char nome[10];
	int defesa[4];
	int ataque[4]; 
	int vida[4];
	
}Tinfo;


main()
{
	strand(time(NULL));
	//jogador 1
	Tinfo jogador1[4];

	int i;
	int dado1,dado2;

	for(i=0;i<4;i++)
	{
		//nome
		printf("Digite o nome do personagem %i: ", i+1);
		gets(jogador1[i].nome);
		//Vida
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].vida[i]=100;
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Vida do jogador %s: %i",jogador1[i].nome, jogador1[i].vida[i]);
		//attaque
		
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].ataque[i]=dado1+dado2+6;
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Ataque do jogador %s: %i",jogador1[i].nome, jogador1[i].ataque[i]);
		//defesa
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].defesa[i]=dado1+dado2+12;
		printf("Dado 1 %i\n",dado1);
		printf("Dado 2 %i\n", dado2);
		printf("Defesa do jogador %s: %i",jogador1[i].nome, jogador1[i].defesa[i]);
		system("pause");
		system("cls");
	}
	
	
}
