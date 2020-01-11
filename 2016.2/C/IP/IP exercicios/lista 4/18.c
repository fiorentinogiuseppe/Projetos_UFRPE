#include<stdio.h>
#include<stdlib.h>

main()
{
	int m[4][4];
	
	int i,j;
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<4;j++)
		{
			printf("Digite o valor de m[%i][%i]", i,j);
			scanf("%i",&m[i][j]);
		}
	}
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<4;j++)
		{
			printf("%i\t", m[i][j]);
		}
		printf("\n");
	}
	
	system("pause");
	system("cls");
	
	for(i=0;i<4;i++)
	{
		for(j=0;j<3;j++)
		{
			printf("%i\t", m[j][i]);
		}
		printf("\n");
	}
}
