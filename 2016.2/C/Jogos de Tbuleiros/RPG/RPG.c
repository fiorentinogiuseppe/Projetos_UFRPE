
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
    //Introdução

    system("pause");
    system("cls");
	srand(time(NULL));
	//jogador 1
	Tinfo jogador1[5];
	int contador=5;
	int i;
	int dado1,dado2;
	int vitoria,rodada=0;


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
		//ataque

		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].ataque[i]=dado1+dado2+6;
		//defesa
		dado1=1+rand()%6;
		dado2=1+rand()%6;
		jogador1[i].defesa[i]=dado1+dado2+12;
		printf("|=============================|\n");
		printf("|Vida do jogador %s: %.0f     |\n",jogador1[i].nome, jogador1[i].vida[i]);
		printf("|-----------------------------|\n");
		printf("|Ataque do jogador %s: %.0f   |\n",jogador1[i].nome, jogador1[i].ataque[i]);
		printf("|-----------------------------|\n");
		printf("|Defesa do jogador %s: %.0f   |\n",jogador1[i].nome, jogador1[i].defesa[i]);
		printf("|=============================|\n");
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
		printf("|=============================|\n");
		printf("|Vida do jogador %s: %.0f     |\n",jogador2[i].nome, jogador2[i].vida[i]);
		printf("|-----------------------------|\n");
		printf("|Ataque do jogador %s: %.0f   |\n",jogador2[i].nome, jogador2[i].ataque[i]);
		printf("|-----------------------------|\n");
		printf("|Defesa do jogador %s: %.0f   |\n",jogador2[i].nome, jogador2[i].defesa[i]);
		printf("|=============================|\n");
		system("pause");
		system("cls");

	}

	//inicio

	int personagens[5], personagens1[5],auxiliar,saida;
	float aleatorio, precisao_do_lutador_1,precisao_do_lutador_2;
	int defesa;
	int verifi[5];
	int aux,j,k;

	do
	{
		printf("\n\n\n\nJogador 1\n\n\n\n");

		for(i=1;i<=4;i++)
		{
			verifi[i]=0;
		}

		for(i=1;i<=4;i++)
		{
			//defesa
			defesa=1+rand()%4;
			auxiliar=defesa;
			do
			{
				if(jogador2[defesa].vida[defesa]<=0)
				{
					defesa=1+rand()%4;
					if(auxiliar==defesa)
					{
						saida=0;
					}
					else if (auxiliar!=defesa)
					{
						saida=1;
					}
				}
				else if (jogador2[defesa].vida[defesa]!=0)
				{
					if(verifi[defesa]<=0)
					{
						verifi[defesa]=1;
						break;
					}
					else
					{
						defesa=1+rand()%4;
					}
				}
			}while(saida!=1);
			int aux;
			//ataque
			if(jogador1[i].vida[i]<=0)
			{
				for(j=i;j<=4;j++)
				{
					for(k=i+1;k<=4;k++)
					{
						aux = personagens[k];
                		personagens[k] = personagens[k+1];
                		if(i<3)
                		personagens[k+1] = personagens[k+2];
                		else if(i>=3 && i<=4)
                		{
                			aux = personagens[k];
                			personagens[k] = personagens[k+1];
                			personagens[k+1] = personagens[1];
						}
						else if (i=4)
						{
							aux = personagens[k];
                			personagens[k] = personagens[1];
                			personagens[1] = personagens[2];
						}

					}
				}

			}
			else if (jogador1[i].vida[i]!=0)
			{
				//precisao
				precisao_do_lutador_1 = 1 - (((jogador1[i].vida[i]) * (jogador1[i].ataque[i]))/1000);
				aleatorio=rand()%101;
				aleatorio=aleatorio/100;
				/*
				printf("Aleatorio= %f\n", aleatorio);
				printf("Precisao= %f\n", precisao_do_lutador_1);
				*/
				if(aleatorio>precisao_do_lutador_1)
				{

					printf("Ataque de %s do jogador 1 em %s do jogador 2\n", jogador1[i].nome,jogador2[defesa].nome);
					jogador2[defesa].vida[defesa]=jogador2[defesa].vida[defesa] - (jogador2[defesa].ataque[defesa])/ (jogador2[defesa].defesa[defesa]/10);
					printf("Vida jogador 2: %.3f\nVida jogador 1: %.3f\n",jogador2[defesa].vida[defesa], jogador1[i].vida[i]);
				}
				else
				{
					printf(" %s do jogador 1 errooww o %s do jogador 2\n", jogador1[i].nome,jogador2[defesa].nome);
					printf("Vida jogador 2: %.3f\nVida jogador 1: %.3f\n",jogador2[defesa].vida[defesa], jogador1[i].vida[i]);
				}

			}


		}

		printf("\n\n\n\nJogador 2\n\n\n\n");
		int verifi1[5];

		//jogador 2
		for(i=1;i<=4;i++)
		{
			verifi1[i]=0;
		}

		for(i=1;i<=4;i++)
		{
			//defesa
			defesa=1+rand()%4;
			auxiliar=defesa;
			do
			{
				if(jogador1[defesa].vida[defesa]<=0)
				{
					defesa=1+rand()%4;
					if(auxiliar==defesa)
					{
						saida=0;
					}
					else if (auxiliar!=defesa)
					{
						saida=1;
					}
				}
				else
				{
						if(verifi1[defesa]==0)
					{
						verifi1[defesa]=1;
						break;
					}
					else
					{
						defesa=1+rand()%4;
					}
				}
			}while(saida!=1);

			int aux1;
			//ataque
				if(jogador2[i].vida[i]<=0)
			{
				for(j=i;j<=4;j++)
				{
					for(k=i+1;k<=4;k++)
					{
						aux1 = personagens1[k];
                		personagens1[k] = personagens1[k+1];
                		if(i<3)
                		personagens1[k+1] = personagens1[k+2];
                		else if(i>=3 && i<=4)
                		{
                			aux = personagens1[k];
                			personagens1[k] = personagens1[k+1];
                			personagens1[k+1] = personagens1[1];
						}
						else if (i=4)
						{
							aux = personagens1[k];
                			personagens1[k] = personagens1[1];
                			personagens1[1] = personagens1[2];
						}

					}
				}

			}
			else
			{
				//precisao
				precisao_do_lutador_2 = 1 - (((jogador2[i].vida[i]) * (jogador2[i].ataque[i]))/1000);
				aleatorio=rand()%101;
				aleatorio=aleatorio/100;
				printf("Aleatorio= %f\n", aleatorio);
				printf("Precisao= %f\n", precisao_do_lutador_2);
				if(aleatorio>precisao_do_lutador_2)
				{

					printf("Ataque de %s do jogador 2 em %s do jogador 1\n", jogador2[i].nome,jogador1[defesa].nome);
					jogador1[defesa].vida[defesa]=jogador1[defesa].vida[defesa] - (jogador1[defesa].ataque[defesa])/ (jogador1[defesa].defesa[defesa]/10);
					printf("Vida jogador 1: %.3f\nVida jogador 2: %.3f\n",jogador1[defesa].vida[defesa], jogador2[i].vida[i]);
				}
				else
				{
					printf(" %s do jogador 2 errooww o %s do jogador 1\n", jogador2[i].nome,jogador1[defesa].nome);
					printf("Vida jogador 1: %.3f\nVida jogador 2: %.3f\n",jogador1[defesa].vida[defesa], jogador2[i].vida[i]);
				}

			}
		}


		for(i=1;i<=4;i++)
		{
			if(jogador1[i].vida[i]<=0)
			{
				printf("\n\nPersonagem %s do jogador 1 morreu ", jogador1[i].nome);
			}
			if(jogador2[i].vida[i]<=0)
			{
				printf("\n\nPersongaem %s do jogador 2 morreu", jogador2[i].nome);
			}
		}
		if(jogador1[1].vida[1]<=0 && jogador1[2].vida[2]<=0 && jogador1[3].vida[3]<=0 && jogador1[4].vida[4]<=0 )
		{
			vitoria=2;
			break;

		}
		if(jogador2[1].vida[1]<=0 && jogador2[2].vida[2]<=0 && jogador2[3].vida[3]<=0 && jogador2[4].vida[4]<=0 )
		{
			vitoria=1;
			break;


		}



	}	while(contador!=0 );

	if(vitoria==2)
	{
	    system("cls");
		printf("\n\nJogador 1 morreu\nJogador 2 venceu!!\n\n");
	}
	else if(vitoria==1)
	{
        system("cls");
		printf("\n\nJogador 2 morreu\nJogador 1 Venceuu!!\n\n");
	}


    system("pause");
    return 0;


}
