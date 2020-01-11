#include<stdio.h>
#include<stdlib.h>
#include<time.h>

main()
{
	//geral
	int i,j;
	//tabuleiro inicial
	for(i=0;i<8;i++)
	{
		printf("\t%i",i);
	}
	printf("\n");
	int tabuleiro[8][8];
	
	for(i=0;i<8;i++)
	{
		for(j=0;j<8;j++)
		{
			tabuleiro[i][j]=0;
		}
	}
	
	for(i=0;i<8;i++)
	{
		printf("%i\t",i);

		for(j=0;j<8;j++)
		{
			printf("%i\t",tabuleiro[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	//adversario
	int adversario[8];
	for(i=0;i<8;i++)
	{
		adversario[i]=1;
	}
	
	//tabuleiro com o adversario

	int linha=0, coluna=0;
	
		for(j=0;j<8;j++)
		{
			linha= rand()%7;
			coluna= rand()%7;
			tabuleiro[linha][coluna]=adversario[j];
		}
	// cavalo
	int cavalo=2;
	linha=rand()%8;
	coluna= rand()%8;
	
	if(tabuleiro!=1)
	{
		tabuleiro[linha][coluna]=2;
	}
	// tabuleiro final
	for(i=0;i<8;i++)
	{
		printf("\t%i",i);
	}
	printf("\n\n");
	for(i=0;i<8;i++)
	{
		printf("%i\t",i);

		for(j=0;j<8;j++)
		{
			printf("%i\t",tabuleiro[i][j]);
		}
		printf("\n");
	}
	
	//Jogada
	int l,c;
	printf("Digite sua jogada: ");
	printf("\nLinha: ");
	scanf("%i", &l);
	printf("\nColuna: ");
	scanf("%i", &c);
	
	//casos de tabueiro
	if(l>7 && c>7 || l<0 && c<0)
	{
		printf(" jogada fora do tabuleiro");
	}
	else
	{
		if(tabuleiro[l][c]==1)
		{
			printf("Acerto mizeravi");
		}
		else
		{
			printf("Erooouu");
		}
	}
	
	
}
