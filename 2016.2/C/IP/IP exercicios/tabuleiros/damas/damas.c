#include<stdio.h>
#include<time.h>
#include<stdlib.h>

main()
{
	int tabuleiro[8][8] ;
	int tabuleiro2[8][8];
	int a,i,j, quantidade, linha,coluna, ponto4=0, ponto5=0;
	
	//coordenadas
	for(i=0;i<8;i++)
	{
		printf("\t%i", i);
	}
	printf("\n\n");
	
	//tabuleiro
	for(i=0;i<8;i++)
	{
		for(j=0; j<8;j++)
		{
			if(i%2==0 && j%2==0)
			{
				tabuleiro[i][j]=1;
			}
			else if(i%2!=0 && j%2!=0)
			{
				tabuleiro[i][j]=1;
			}
			else
			{
				tabuleiro[i][j]=0;
			}
		}
		
	}
	
		
	for(i=0;i<8;i++)
	{
		printf("%i\t",i);
		for(j=0; j<8;j++)
		{
			printf("%i\t", tabuleiro[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	
	//coordenadas
	for(i=0;i<8;i++)
	{
		printf("\t%i", i);
	}
	printf("\n\n");
	
	//tabuleiro
	srand(time(NULL));
	a= rand()%8;
	int pecas[a];
	
	for(i=0;i<rand()%11;i++)
	{
		for(j=0;j<rand()%11;j++)
		{
			if(i%2==0 && j%2==0)
			{
				tabuleiro[i][j]=4;
				ponto4++;
			}
			else if(i%2!=0 && j%2!=0)
			{
				tabuleiro[i][j]=4;
				ponto4++;
			}
			else
			{
				tabuleiro[i][j]=5;
				ponto5++;
			}
		}	
	}
	
		
	for(i=0;i<8;i++)
	{
		printf("%i\t",i);
		for(j=0; j<8;j++)
		{
			printf("%i\t", tabuleiro[i][j]);
		}
		printf("\n");
	}
	
	system("pause");
	system("cls");
	if(ponto4>ponto5)
	{
		printf("4 vencendo");
	}
	else if(ponto5>ponto4)
	{
		printf("5 vencendo");
	}
	else if( ponto5==ponto4)
	{
		printf("empatando");
	}
}
